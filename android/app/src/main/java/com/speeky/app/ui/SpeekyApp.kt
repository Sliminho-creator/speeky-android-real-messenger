package com.speeky.app.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AssistChip
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.speeky.app.core.AppSnapshot
import com.speeky.app.core.AppUser
import com.speeky.app.core.ChatMessage
import com.speeky.app.core.LocalStore
import com.speeky.app.core.MessageKind
import com.speeky.app.core.ThemePreset
import com.speeky.app.core.UiScalePreset
import com.speeky.app.media.SoundFx
import com.speeky.app.media.VoiceRecorder
import com.speeky.app.ui.theme.SpeekyPalette
import com.speeky.app.ui.theme.SpeekyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private sealed interface AppScreen {
    data object Auth : AppScreen
    data object Chats : AppScreen
    data object Settings : AppScreen
    data object EditProfile : AppScreen
    data class Chat(val peerId: String) : AppScreen
    data class Contact(val peerId: String) : AppScreen
    data object Appearance : AppScreen
    data object Devices : AppScreen
    data class Detail(val title: String, val subtitle: String) : AppScreen
    data class Call(val peerId: String, val video: Boolean) : AppScreen
}

@Composable
fun SpeekyApp() {
    val context = LocalContext.current
    var snapshot by remember { mutableStateOf(LocalStore.load(context)) }
    var screen by remember {
        mutableStateOf<AppScreen>(
            if (snapshot.currentUserId == null) AppScreen.Auth else AppScreen.Chats
        )
    }
    var search by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val unreadCounters = remember { mutableStateMapOf<String, Int>() }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val recorder = remember { VoiceRecorder() }
    var isRecording by remember { mutableStateOf(false) }
    var recordingForChat by remember { mutableStateOf<String?>(null) }
    var recordingStartedAt by remember { mutableLongStateOf(0L) }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var requestStartRecordingForChat by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("Доступ к микрофону не выдан")
            }
            requestStartRecordingForChat = null
        }
    }

    LaunchedEffect(hasAudioPermission, requestStartRecordingForChat) {
        val targetChat = requestStartRecordingForChat
        if (hasAudioPermission && targetChat != null && !isRecording) {
            recorder.start(context)
            isRecording = true
            recordingForChat = targetChat
            recordingStartedAt = System.currentTimeMillis()
            requestStartRecordingForChat = null
            snackbarHostState.showSnackbar("Запись голосового началась")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            recorder.cancel()
        }
    }

    fun updateSnapshot(transform: (AppSnapshot) -> AppSnapshot) {
        snapshot = transform(snapshot)
        LocalStore.save(context, snapshot)
    }

    fun currentUser(): AppUser? = snapshot.users.firstOrNull { it.id == snapshot.currentUserId }

    fun userById(id: String): AppUser? = snapshot.users.firstOrNull { it.id == id }

    fun chatUsers(): List<AppUser> {
        val base = snapshot.users.filter { it.id != snapshot.currentUserId }
        val filtered = if (search.isBlank()) base else {
            base.filter {
                it.name.contains(search, ignoreCase = true) ||
                    it.username.contains(search, ignoreCase = true)
            }
        }
        return filtered.sortedByDescending { latestMessageTime(snapshot.messages, it.id) }
    }

    fun messagesFor(peerId: String): List<ChatMessage> = snapshot.messages.filter { it.peerId == peerId }.sortedBy { it.timestamp }

    fun addMessage(peerId: String, text: String = "", kind: MessageKind = MessageKind.TEXT, audioPath: String? = null, durationSeconds: Int = 0) {
        if (text.isBlank() && audioPath == null) return
        updateSnapshot {
            it.copy(
                messages = it.messages + ChatMessage(
                    peerId = peerId,
                    fromMe = true,
                    kind = kind,
                    text = text,
                    audioPath = audioPath,
                    durationSeconds = durationSeconds
                )
            )
        }
        unreadCounters[peerId] = 0
        SoundFx.tap()

        if (!userById(peerId).isSystem()) {
            scope.launch {
                delay(900)
                updateSnapshot {
                    it.copy(
                        messages = it.messages + ChatMessage(
                            peerId = peerId,
                            fromMe = false,
                            kind = MessageKind.TEXT,
                            text = when {
                                kind == MessageKind.VOICE -> "услышал, сейчас отвечу"
                                text.contains("привет", true) -> "привет 👋"
                                text.contains("как", true) -> "всё норм, на связи"
                                else -> "принял: ${text.ifBlank { "голосовое" }}"
                            }
                        )
                    )
                }
                if (screen != AppScreen.Chat(peerId)) {
                    unreadCounters[peerId] = (unreadCounters[peerId] ?: 0) + 1
                }
                snackbarHostState.showSnackbar("Новое сообщение от ${userById(peerId)?.name ?: peerId}")
                SoundFx.incoming()
            }
        }
    }

    fun openVoiceMessage(message: ChatMessage) {
        val path = message.audioPath ?: return
        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
        runCatching {
            val player = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
            }
            mediaPlayer = player
        }.onFailure {
            scope.launch {
                snackbarHostState.showSnackbar("Не удалось воспроизвести голосовое")
            }
        }
    }

    fun startOrStopRecording(peerId: String) {
        if (isRecording && recordingForChat == peerId) {
            val duration = ((System.currentTimeMillis() - recordingStartedAt) / 1000L).toInt().coerceAtLeast(1)
            val path = recorder.stop()
            isRecording = false
            recordingForChat = null
            recordingStartedAt = 0L
            if (path != null) {
                addMessage(
                    peerId = peerId,
                    kind = MessageKind.VOICE,
                    audioPath = path,
                    durationSeconds = duration
                )
            } else {
                scope.launch { snackbarHostState.showSnackbar("Запись оборвалась") }
            }
            return
        }

        if (!hasAudioPermission) {
            requestStartRecordingForChat = peerId
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        requestStartRecordingForChat = peerId
    }

    val baseDensity = LocalDensity.current
    val uiScaleFactor = when (snapshot.uiScale) {
        UiScalePreset.COMPACT -> 0.92f
        UiScalePreset.NORMAL -> 1.0f
        UiScalePreset.LARGE -> 1.10f
    }
    val palette = when (snapshot.selectedTheme) {
        ThemePreset.DARK -> SpeekyPalette.DARK
        ThemePreset.VIOLET -> SpeekyPalette.VIOLET
        ThemePreset.OCEAN -> SpeekyPalette.OCEAN
        ThemePreset.SAKURA -> SpeekyPalette.SAKURA
    }

    SpeekyTheme(palette = palette) {
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = baseDensity.density * uiScaleFactor,
                fontScale = baseDensity.fontScale * uiScaleFactor
            )
        ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            BackgroundShell(modifier = Modifier.padding(padding)) {
                Crossfade(
                    targetState = screen,
                    label = "screen"
                ) { target ->
                    when (target) {
                        AppScreen.Auth -> AuthScreen(
                            onLogin = { login, password ->
                                val match = snapshot.users.firstOrNull {
                                    !it.isSystem &&
                                        (it.email.equals(login.trim(), true) || it.username.equals(login.trim().removePrefix("@"), true)) &&
                                        it.password == password
                                }
                                if (match == null) {
                                    scope.launch { snackbarHostState.showSnackbar("Неверный логин или пароль") }
                                } else {
                                    updateSnapshot { it.copy(currentUserId = match.id) }
                                    screen = AppScreen.Chats
                                }
                            },
                            onRegister = { name, username, email, password ->
                                val normalizedUsername = username.trim().removePrefix("@")
                                if (snapshot.users.any { it.username.equals(normalizedUsername, true) }) {
                                    scope.launch { snackbarHostState.showSnackbar("Username уже занят") }
                                    return@AuthScreen
                                }
                                val newUser = AppUser(
                                    id = UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    username = normalizedUsername,
                                    email = email.trim(),
                                    password = password
                                )
                                updateSnapshot {
                                    it.copy(
                                        users = it.users + newUser,
                                        currentUserId = newUser.id
                                    )
                                }
                                screen = AppScreen.Chats
                                scope.launch { snackbarHostState.showSnackbar("Аккаунт создан") }
                            }
                        )

                        AppScreen.Chats -> ChatsScreen(
                            currentUser = currentUser(),
                            chats = chatUsers(),
                            search = search,
                            unread = unreadCounters,
                            messages = snapshot.messages,
                            onSearchChange = { search = it },
                            onOpenChat = { peerId ->
                                unreadCounters[peerId] = 0
                                screen = AppScreen.Chat(peerId)
                            },
                            onOpenSettings = { screen = AppScreen.Settings }
                        )

                        is AppScreen.Chat -> {
                            val peer = userById(target.peerId)
                            if (peer == null) {
                                screen = AppScreen.Chats
                            } else {
                                unreadCounters[peer.id] = 0
                                ChatScreen(
                                    peer = peer,
                                    messages = messagesFor(peer.id),
                                    isRecording = isRecording && recordingForChat == peer.id,
                                    onBack = { screen = AppScreen.Chats },
                                    onOpenProfile = { screen = AppScreen.Contact(peer.id) },
                                    onSend = { addMessage(peer.id, text = it) },
                                    onToggleRecording = { startOrStopRecording(peer.id) },
                                    onPlayVoice = { openVoiceMessage(it) },
                                    onStartCall = { video ->
                                                                        screen = AppScreen.Call(peer.id, video)
                                    }
                                )
                            }
                        }

                        is AppScreen.Contact -> {
                            val peer = userById(target.peerId)
                            if (peer == null) {
                                screen = AppScreen.Chats
                            } else {
                                ContactScreen(
                                    user = peer,
                                    onBack = {
                                        screen = if (currentUser()?.id == peer.id) AppScreen.Settings else AppScreen.Chat(peer.id)
                                    },
                                    onMessage = { screen = AppScreen.Chat(peer.id) },
                                    onCall = { video -> screen = AppScreen.Call(peer.id, video) },
                                    onOpenAppearance = { screen = AppScreen.Appearance }
                                )
                            }
                        }

                        AppScreen.Settings -> SettingsScreen(
                            user = currentUser(),
                            onBack = { screen = AppScreen.Chats },
                            onProfile = { screen = AppScreen.EditProfile },
                            onDevices = { screen = AppScreen.Devices },
                            onAppearance = { screen = AppScreen.Appearance },
                            onDetail = { title, subtitle -> screen = AppScreen.Detail(title, subtitle) },
                            onLogout = {
                                updateSnapshot { it.copy(currentUserId = null) }
                                screen = AppScreen.Auth
                            }
                        )

                        AppScreen.EditProfile -> {
                            val me = currentUser()
                            if (me == null) {
                                screen = AppScreen.Auth
                            } else {
                                EditProfileScreen(
                                    user = me,
                                    selectedScale = snapshot.uiScale,
                                    onBack = { screen = AppScreen.Settings },
                                    onSave = { name, username, bio, avatarUri, uiScale ->
                                        val normalizedUsername = username.trim().removePrefix("@")
                                        when {
                                            normalizedUsername.isBlank() -> scope.launch {
                                                snackbarHostState.showSnackbar("Username не может быть пустым")
                                            }
                                            snapshot.users.any { it.id != me.id && it.username.equals(normalizedUsername, true) } -> scope.launch {
                                                snackbarHostState.showSnackbar("Username уже занят")
                                            }
                                            else -> {
                                                updateSnapshot { snap ->
                                                    snap.copy(
                                                        users = snap.users.map { existing ->
                                                            if (existing.id == me.id) {
                                                                existing.copy(
                                                                    name = name.trim().ifBlank { existing.name },
                                                                    username = normalizedUsername,
                                                                    bio = bio.trim(),
                                                                    avatarUri = avatarUri
                                                                )
                                                            } else {
                                                                existing
                                                            }
                                                        },
                                                        uiScale = uiScale
                                                    )
                                                }
                                                SoundFx.tap()
                                                screen = AppScreen.Settings
                                                scope.launch { snackbarHostState.showSnackbar("Профиль обновлён") }
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        AppScreen.Appearance -> AppearanceScreen(
                            selected = snapshot.selectedTheme,
                            onBack = { screen = AppScreen.Settings },
                            onSelect = { preset ->
                                updateSnapshot { it.copy(selectedTheme = preset) }
                                SoundFx.tap()
                            }
                        )

                        AppScreen.Devices -> DevicesScreen(onBack = { screen = AppScreen.Settings })

                        is AppScreen.Detail -> DetailScreen(
                            title = target.title,
                            subtitle = target.subtitle,
                            onBack = { screen = AppScreen.Settings }
                        )

                        is AppScreen.Call -> {
                            val peer = userById(target.peerId)
                            if (peer == null) {
                                screen = AppScreen.Chats
                            } else {
                                CallScreen(
                                    user = peer,
                                    video = target.video,
                                    onBack = {
                                        SoundFx.tap()
                                        screen = AppScreen.Chat(peer.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun BackgroundShell(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
private fun AuthScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit
) {
    var registerMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("Sliminho") }
    var username by remember { mutableStateOf("sliminho") }
    var login by remember { mutableStateOf("hello@speeky.app") }
    var email by remember { mutableStateOf("hello@speeky.app") }
    var password by remember { mutableStateOf("password123") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Speeky",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (registerMode) "Регистрация занимает меньше минуты" else "Вход в твой Speeky",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = !registerMode,
                onClick = { registerMode = false },
                label = { Text("Вход") }
            )
            FilterChip(
                selected = registerMode,
                onClick = { registerMode = true },
                label = { Text("Регистрация") }
            )
        }

        Spacer(Modifier.height(18.dp))
        ElevatedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                if (registerMode) {
                    AppField(value = name, onValueChange = { name = it }, label = "Имя")
                    Spacer(Modifier.height(12.dp))
                    AppField(value = username, onValueChange = { username = it }, label = "Username")
                    Spacer(Modifier.height(12.dp))
                    AppField(value = email, onValueChange = { email = it }, label = "Email")
                } else {
                    AppField(value = login, onValueChange = { login = it }, label = "Email или username")
                }
                Spacer(Modifier.height(12.dp))
                AppField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль",
                    keyboardType = KeyboardType.Password
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        if (registerMode) onRegister(name, username, email, password) else onLogin(login, password)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(if (registerMode) "Создать аккаунт" else "Войти")
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        TextButton(onClick = { registerMode = !registerMode }) {
            Text(if (registerMode) "Уже есть аккаунт? Войти" else "Нет аккаунта? Зарегистрироваться")
        }
    }
}

@Composable
private fun ChatsScreen(
    currentUser: AppUser?,
    chats: List<AppUser>,
    search: String,
    unread: Map<String, Int>,
    messages: List<ChatMessage>,
    onSearchChange: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Rounded.Settings, contentDescription = "Настройки")
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Чаты", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = currentUser?.let { "@${it.username}" } ?: "@guest",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AvatarCircle(user = currentUser, fallbackSymbol = currentUser?.name?.firstOrNull()?.toString() ?: "S", highlight = true)
        }

        Spacer(Modifier.height(16.dp))
        AppField(
            value = search,
            onValueChange = onSearchChange,
            label = "Поиск",
            leading = {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        )
        Spacer(Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chats, key = { it.id }) { chat ->
                val lastMessage = messages.filter { it.peerId == chat.id }.maxByOrNull { it.timestamp }
                ElevatedCard(
                    onClick = { onOpenChat(chat.id) },
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarCircle(user = chat, fallbackSymbol = if (chat.isSystem) "★" else chat.name.take(1), highlight = !chat.isSystem)
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(chat.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = lastMessagePreview(lastMessage),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = lastMessage?.let { formatClock(it.timestamp) } ?: "",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val badge = unread[chat.id] ?: 0
                            if (badge > 0) {
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = badge.toString(),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatScreen(
    peer: AppUser,
    messages: List<ChatMessage>,
    isRecording: Boolean,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onSend: (String) -> Unit,
    onToggleRecording: () -> Unit,
    onPlayVoice: (ChatMessage) -> Unit,
    onStartCall: (Boolean) -> Unit
) {
    var draft by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenProfile() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(peer.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text(
                        text = if (peer.isSystem) "личное пространство" else "был(а) недавно",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AvatarCircle(user = peer, fallbackSymbol = if (peer.isSystem) "★" else peer.name.take(1), highlight = !peer.isSystem)
            }
            IconButton(onClick = { onStartCall(false) }) {
                Icon(Icons.Rounded.Call, contentDescription = "Звонок")
            }
            IconButton(onClick = { onStartCall(true) }) {
                Icon(Icons.Rounded.VideoCall, contentDescription = "Видео")
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages.reversed(), key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    onPlayVoice = { onPlayVoice(message) }
                )
            }
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Rounded.AttachFile, contentDescription = "Вложение")
                }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(if (isRecording) "Идёт запись…" else "Сообщение…") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                if (draft.isNotBlank()) {
                    IconButton(onClick = {
                        onSend(draft)
                        draft = ""
                    }) {
                        Icon(Icons.Rounded.Send, contentDescription = "Отправить")
                    }
                } else {
                    IconButton(onClick = onToggleRecording) {
                        Icon(
                            if (isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                            contentDescription = if (isRecording) "Стоп запись" else "Голосовое"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onPlayVoice: () -> Unit
) {
    val bubbleColor = if (message.fromMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (message.fromMe) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.fromMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = bubbleColor,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (message.kind == MessageKind.VOICE) {
                    Row(
                        modifier = Modifier.clickable { onPlayVoice() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            tint = textColor
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Голосовое • ${message.durationSeconds}с",
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(message.text, color = textColor, fontSize = 19.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = formatClock(message.timestamp),
                    color = textColor.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ContactScreen(
    user: AppUser,
    onBack: () -> Unit,
    onMessage: () -> Unit,
    onCall: (Boolean) -> Unit,
    onOpenAppearance: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(22.dp)
            ) {
                Text(user.name, fontSize = 38.sp, fontWeight = FontWeight.Black)
                Text(
                    if (user.isSystem) "служебный чат" else "был(а) 3 ч. назад",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 21.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            ElevatedCard(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.AlternateEmail, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("@${user.username}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Имя пользователя", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (user.bio.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Описание", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text(user.bio, fontSize = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionPill("Сообщение", Icons.Rounded.Forum, Modifier.weight(1f)) { onMessage() }
                ActionPill("Звонок", Icons.Rounded.Call, Modifier.weight(1f)) { onCall(false) }
                ActionPill("Видео", Icons.Rounded.VideoCall, Modifier.weight(1f)) { onCall(true) }
            }

            Spacer(Modifier.height(18.dp))
            SettingsGroup(
                items = listOf(
                    Triple("Уведомления", Icons.Rounded.Notifications, {}),
                    Triple("Фото и видео", Icons.Rounded.Videocam, {}),
                    Triple("Файлы", Icons.Rounded.AttachFile, {}),
                    Triple("Оформление чата", Icons.Rounded.Palette, onOpenAppearance)
                )
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsScreen(
    user: AppUser?,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onDevices: () -> Unit,
    onAppearance: () -> Unit,
    onDetail: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
            }
            Spacer(Modifier.width(6.dp))
            Text("Настройки", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(14.dp))
        ElevatedCard(
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarCircle(user = user, fallbackSymbol = user?.name?.take(1) ?: "⚡", highlight = true)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user?.name ?: "Гость", fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = user?.let { "@${it.username}" } ?: "@guest",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 19.sp
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                FilledTonalButton(
                    onClick = onProfile,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Редактировать профиль")
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        SettingsGroup(
            items = listOf(
                Triple("Профиль и интерфейс", Icons.Rounded.Person, onProfile),
                Triple("Избранное", Icons.Rounded.Favorite) { onDetail("Избранное", "Тут можно хранить важные сообщения и заметки.") },
                Triple("Устройства", Icons.Rounded.Devices, onDevices),
                Triple("Заявки в друзья", Icons.Rounded.AccountBox) { onDetail("Заявки в друзья", "Пока новых заявок нет.") }
            )
        )
        Spacer(Modifier.height(14.dp))
        SettingsGroup(
            items = listOf(
                Triple("Уведомления и звуки", Icons.Rounded.Notifications) { onDetail("Уведомления и звуки", "Звуки включены, вибрация включена.") },
                Triple("Конфиденциальность", Icons.Rounded.Security) { onDetail("Конфиденциальность", "Локальный режим: данные хранятся только на устройстве. Для реального режима дальше подключим сервер и шифрование.") },
                Triple("Данные и память", Icons.Rounded.Storage) { onDetail("Данные и память", "Голосовые пишутся в cache и могут быть очищены системой.") },
                Triple("Оформление", Icons.Rounded.Palette, onAppearance)
            )
        )
        Spacer(Modifier.height(14.dp))
        SettingsGroup(
            items = listOf(
                Triple("Помощь", Icons.Rounded.Help) { onDetail("Помощь", "Следующий шаг — подключить backend, WebSocket и push-уведомления для реального онлайна.") },
                Triple("О Speeky", Icons.Rounded.Info) { onDetail("О Speeky", "Версия 1.1.0 • rebuild") }
            )
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(22.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            Icon(Icons.Rounded.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Выйти из аккаунта")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EditProfileScreen(
    user: AppUser,
    selectedScale: UiScalePreset,
    onBack: () -> Unit,
    onSave: (String, String, String, String?, UiScalePreset) -> Unit
) {
    var name by remember(user.id) { mutableStateOf(user.name) }
    var username by remember(user.id) { mutableStateOf(user.username) }
    var bio by remember(user.id) { mutableStateOf(user.bio) }
    var avatarUri by remember(user.id) { mutableStateOf(user.avatarUri) }
    var scale by remember(user.id) { mutableStateOf(selectedScale) }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        avatarUri = uri?.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
            }
            Spacer(Modifier.width(6.dp))
            Column {
                Text("Профиль и интерфейс", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Ник, @username, фото, описание и масштаб UI", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(18.dp))
        ElevatedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                AvatarCircle(
                    user = user.copy(name = name, username = username, bio = bio, avatarUri = avatarUri),
                    fallbackSymbol = name.take(1).ifBlank { "S" },
                    highlight = true,
                    big = true
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(
                        onClick = { pickPhotoLauncher.launch("image/*") },
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Выбрать фото")
                    }
                    if (avatarUri != null) {
                        TextButton(onClick = { avatarUri = null }) {
                            Text("Убрать")
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                AppField(value = name, onValueChange = { name = it }, label = "Никнейм")
                Spacer(Modifier.height(12.dp))
                AppField(value = username, onValueChange = { username = it.removePrefix("@") }, label = "Username")
                Spacer(Modifier.height(12.dp))
                AppField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Описание",
                    singleLine = false,
                    minLines = 3
                )
                Spacer(Modifier.height(16.dp))
                Text("Масштаб интерфейса", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        UiScalePreset.COMPACT to "Компактный",
                        UiScalePreset.NORMAL to "Нормальный",
                        UiScalePreset.LARGE to "Крупный"
                    ).forEach { (preset, title) ->
                        FilterChip(
                            selected = scale == preset,
                            onClick = { scale = preset },
                            label = { Text(title) }
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = { onSave(name, username, bio, avatarUri, scale) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("Сохранить изменения")
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Переход к настоящему мессенджеру", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Следующий этап: подключить backend с auth, WebSocket, хранение медиа и WebRTC для реальных звонков.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AppearanceScreen(
    selected: ThemePreset,
    onBack: () -> Unit,
    onSelect: (ThemePreset) -> Unit
) {
    val options = listOf(
        ThemePreset.DARK to "Тёмная",
        ThemePreset.VIOLET to "Фиолетовая",
        ThemePreset.OCEAN to "Океан",
        ThemePreset.SAKURA to "Сакура"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
            }
            Spacer(Modifier.width(6.dp))
            Column {
                Text("Оформление", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Тема сохраняется локально", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(18.dp))
        ElevatedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
        ) {
            Column {
                options.forEachIndexed { index, (preset, title) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(preset) }
                            .padding(horizontal = 18.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, fontSize = 22.sp, modifier = Modifier.weight(1f))
                        if (selected == preset) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (index != options.lastIndex) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DevicesScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
            }
            Spacer(Modifier.width(6.dp))
            Text("Устройства", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
        Text("Это устройство", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(10.dp))
        ElevatedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBubble(symbol = "🌐", highlight = false)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Speeky Android 1.1.0", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("Speeky • локальное устройство", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("в сети", color = Color(0xFF49D17D), fontWeight = FontWeight.Bold)
                }
                AssistChip(
                    onClick = {},
                    label = { Text("текущее") }
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            Text("Завершить все сеансы")
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "В реальном серверном режиме здесь будет список активных логинов.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailScreen(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
            }
            Spacer(Modifier.width(6.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(22.dp))
        ElevatedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(subtitle, fontSize = 19.sp)
            }
        }
    }
}

@Composable
private fun CallScreen(
    user: AppUser,
    video: Boolean,
    onBack: () -> Unit
) {
    var connected by remember(user.id, video) { mutableStateOf(false) }
    var elapsedSeconds by remember(user.id, video) { mutableIntStateOf(0) }

    LaunchedEffect(user.id, video) {
        delay(1300)
        connected = true
        while (true) {
            delay(1000)
            elapsedSeconds += 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(36.dp))
        AvatarCircle(user = user, fallbackSymbol = if (user.isSystem) "★" else user.name.take(1), highlight = true, big = true)
        Spacer(Modifier.height(20.dp))
        Text(user.name, fontSize = 38.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (connected) {
                if (video) "Видео • ${formatDuration(elapsedSeconds)}" else "Звонок • ${formatDuration(elapsedSeconds)}"
            } else {
                "Соединение…"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 20.sp
        )

        Spacer(Modifier.height(30.dp))
        ElevatedCard(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f))
        ) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    if (connected) "Вызов активен. Это живой экран звонка, а не просто звук." else "Идёт набор…",
                    fontSize = 19.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionPill("Микрофон", if (connected) Icons.Rounded.Mic else Icons.Rounded.MicOff, Modifier.weight(1f)) {}
                    ActionPill("Динамик", Icons.Rounded.VolumeUp, Modifier.weight(1f)) {}
                    ActionPill(if (video) "Камера" else "Видео", Icons.Rounded.Videocam, Modifier.weight(1f)) {}
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(vertical = 18.dp)
        ) {
            Icon(Icons.Rounded.PhoneInTalk, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Завершить")
        }
    }
}

@Composable
private fun ActionPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun SettingsGroup(
    items: List<Triple<String, androidx.compose.ui.graphics.vector.ImageVector, () -> Unit>>
) {
    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
    ) {
        Column {
            items.forEachIndexed { index, item ->
                val (title, icon, click) = item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { click() }
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(14.dp))
                    Text(title, modifier = Modifier.weight(1f), fontSize = 21.sp)
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (index != items.lastIndex) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarCircle(
    user: AppUser?,
    fallbackSymbol: String,
    highlight: Boolean,
    big: Boolean = false
) {
    val context = LocalContext.current
    val size = if (big) 108.dp else 58.dp
    val image = remember(user?.avatarUri) { loadAvatarBitmap(context, user?.avatarUri) }

    if (image != null && user?.isSystem != true) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        }
    } else {
        IconBubble(symbol = fallbackSymbol, highlight = highlight, big = big)
    }
}

@Composable
private fun IconBubble(
    symbol: String,
    highlight: Boolean,
    big: Boolean = false
) {
    val size = if (big) 108.dp else 58.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    if (highlight) {
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.92f)
                        )
                    } else {
                        listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontWeight = FontWeight.Black,
            color = if (highlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontSize = if (big) 42.sp else 26.sp
        )
    }
}

@Composable
private fun AppField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    leading: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = if (singleLine) 1 else 5,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        leadingIcon = leading,
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.14f),
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

private fun lastMessagePreview(message: ChatMessage?): String {
    if (message == null) return "Пустой диалог"
    return if (message.kind == MessageKind.VOICE) {
        "🎤 Голосовое • ${message.durationSeconds}с"
    } else {
        message.text
    }
}

private fun latestMessageTime(messages: List<ChatMessage>, peerId: String): Long {
    return messages.filter { it.peerId == peerId }.maxOfOrNull { it.timestamp } ?: 0L
}

private fun formatClock(time: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

private fun loadAvatarBitmap(context: Context, avatarUri: String?) = avatarUri?.let {
    runCatching {
        val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(it))
        ImageDecoder.decodeBitmap(source)
    }.getOrNull()
}

private fun AppUser?.isSystem(): Boolean = this?.isSystem == true
