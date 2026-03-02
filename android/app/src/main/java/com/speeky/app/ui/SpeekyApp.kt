package com.speeky.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.speeky.app.calls.CallManager
import com.speeky.app.media.SoundFx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class Route { Auth, Chats, Chat, Contact, Settings, Devices, Theme }

data class UiMessage(val id: String, val text: String, val mine: Boolean, val time: String)
data class UiChat(val id: String, val title: String, val username: String, val status: String, val preview: String, val time: String, val messages: List<UiMessage>)

@Composable
fun SpeekyApp() {
    val scope = rememberCoroutineScope()
    val soundFx = remember { SoundFx(androidx.compose.ui.platform.LocalContext.current) }
    val callManager = remember { CallManager() }
    var route by remember { mutableStateOf(Route.Auth) }
    var search by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf("") }
    var selectedChatId by remember { mutableStateOf("c1") }
    var chats by remember {
        mutableStateOf(listOf(
            UiChat("c1", "forever", "@forever", "был(а) 3 ч. назад", "время ж поздно уже за комп та не...", "20:55", listOf(
                UiMessage("1", "маму ем", false, "00:00"),
                UiMessage("2", "все норм", false, "00:00"),
                UiMessage("3", "завтра узнаем", false, "00:00"),
                UiMessage("4", "как узнал ?", true, "00:55"),
                UiMessage("5", "время ж поздно уже за комп та не пустят", true, "00:55")
            )),
            UiChat("c2", "Избранное", "@saved", "личное пространство", "Нет сообщений", "12:22", emptyList())
        ))
    }
    val activeChat = chats.first { it.id == selectedChatId }
    val filtered = chats.filter { search.isBlank() || it.title.contains(search, true) || it.username.contains(search, true) || it.preview.contains(search, true) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF060812), Color.Black)))) {
            AnimatedContent(targetState = route, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "route") { target ->
                when (target) {
                    Route.Auth -> AuthScreen(onLogin = { route = Route.Chats })
                    Route.Chats -> ChatsScreen(search, { search = it }, filtered, { selectedChatId = it; route = Route.Chat }, { route = Route.Settings })
                    Route.Chat -> ChatScreen(activeChat, draft, { draft = it }, { route = Route.Chats }, { route = Route.Contact }, {
                        if (draft.isBlank()) return@ChatScreen
                        chats = chats.map { c -> if (c.id == activeChat.id) c.copy(preview = draft, time = "сейчас", messages = c.messages + UiMessage(System.currentTimeMillis().toString(), draft, true, "сейчас")) else c }
                        draft = ""
                        soundFx.playSent()
                    }, {
                        callManager.startOutgoingAudio(); soundFx.startIncomingCall(); scope.launch { delay(1200); soundFx.stopIncomingCall() }
                    })
                    Route.Contact -> ContactScreen(activeChat, { route = Route.Chat }, { route = Route.Chat }, { callManager.startOutgoingAudio() }, { callManager.startOutgoingVideo() })
                    Route.Settings -> SettingsScreen({ route = Route.Chats }, { route = Route.Devices }, { route = Route.Theme }, { route = Route.Auth })
                    Route.Devices -> DevicesScreen { route = Route.Settings }
                    Route.Theme -> ThemeScreen { route = Route.Settings }
                }
            }
        }
    }
}

@Composable private fun AuthScreen(onLogin: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Speeky", fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Настоящий Android-мессенджер", color = Color.White.copy(alpha = 0.6f))
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(value = "hello@speeky.app", onValueChange = {}, modifier = Modifier.fillMaxWidth(), label = { Text("Email или username") }, shape = RoundedCornerShape(20.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = "password123", onValueChange = {}, modifier = Modifier.fillMaxWidth(), label = { Text("Пароль") }, shape = RoundedCornerShape(20.dp))
        Spacer(Modifier.height(16.dp))
        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(20.dp)) { Text("Войти") }
    }
}

@Composable private fun ChatsScreen(search: String, onSearch: (String) -> Unit, chats: List<UiChat>, onOpenChat: (String) -> Unit, onOpenSettings: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {}) { Text("Изм.") }
                Text("Чаты", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                FilledIconButton(onClick = {}) { Icon(Icons.Rounded.Edit, contentDescription = null) }
            }
            OutlinedTextField(value = search, onValueChange = onSearch, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), placeholder = { Text("Поиск") }, leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }, shape = RoundedCornerShape(22.dp))
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp, 8.dp, 12.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(chats) { chat -> ChatRow(chat) { onOpenChat(chat.id) } }
            }
        }
        BottomTabs(Modifier.align(Alignment.BottomCenter), "chats", {}, onOpenSettings)
    }
}

@Composable private fun ChatScreen(chat: UiChat, draft: String, onDraft: (String) -> Unit, onBack: () -> Unit, onContact: () -> Unit, onSend: () -> Unit, onCall: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = null) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.clickable { onContact() }) {
                    Text(chat.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(chat.status, color = Color.White.copy(alpha = 0.45f))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCall) { Icon(Icons.Rounded.Call, contentDescription = null) }
                Avatar(chat.title.first().uppercase(), MaterialTheme.colorScheme.secondary)
            }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(14.dp, 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(chat.messages) { msg ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (msg.mine) Arrangement.End else Arrangement.Start) {
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = if (msg.mine) MaterialTheme.colorScheme.primary else Color(0xFF242833)), modifier = Modifier.fillMaxWidth(0.82f)) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(msg.text, fontSize = 18.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(msg.time, fontSize = 12.sp, color = Color.White.copy(alpha = 0.55f), modifier = Modifier.align(Alignment.End))
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FilledIconButton(onClick = {}) { Icon(Icons.Rounded.Mic, contentDescription = null) }
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(value = draft, onValueChange = onDraft, modifier = Modifier.weight(1f), placeholder = { Text("Сообщение...") }, shape = RoundedCornerShape(22.dp))
            Spacer(Modifier.width(8.dp))
            FilledIconButton(onClick = onSend, modifier = Modifier.size(56.dp)) { Icon(if (draft.isBlank()) Icons.Rounded.Mic else Icons.Rounded.Send, contentDescription = null) }
        }
    }
}

@Composable private fun ContactScreen(chat: UiChat, onBack: () -> Unit, onMessage: () -> Unit, onAudio: () -> Unit, onVideo: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillMaxWidth().height(220.dp).background(Brush.verticalGradient(listOf(Color(0xFF1B2144), Color(0xFF090B11))))) {
                FilledIconButton(onClick = onBack, modifier = Modifier.padding(16.dp)) { Icon(Icons.Rounded.ArrowBack, contentDescription = null) }
                Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text(chat.title, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Text(chat.status, color = Color.White.copy(alpha = 0.65f))
                }
            }
        }
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard(chat.username, "Имя пользователя")
                ActionRow(onMessage, onAudio, onVideo)
                MenuCard(listOf("Уведомления", "Фото и видео", "Файлы", "Ссылки", "Общие группы", "Оформление чата"))
            }
        }
    }
}

@Composable private fun SettingsScreen(onBack: () -> Unit, onDevices: () -> Unit, onTheme: () -> Unit, onLogout: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = onBack) { Text("Назад") }; Text("Настройки", fontSize = 28.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(56.dp)) } }
            item { MenuCard(listOf("Мой профиль", "Избранное", "Устройства", "Заявки в друзья"), onClick = { if (it == "Устройства") onDevices() }) }
            item { MenuCard(listOf("Уведомления и звуки", "Конфиденциальность", "Данные и память", "Оформление"), onClick = { if (it == "Оформление") onTheme() }) }
            item { Button(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(22.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD16B84))) { Text("Выйти из аккаунта") } }
        }
        BottomTabs(Modifier.align(Alignment.BottomCenter), "settings", onBack, {})
    }
}

@Composable private fun DevicesScreen(onBack: () -> Unit) { Column(Modifier.fillMaxSize().padding(16.dp)) { TextButton(onClick = onBack) { Text("Назад") }; Text("Устройства", fontSize = 30.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); MenuCard(listOf("Pixel 8 Pro - это устройство", "Chrome on Windows - веб вход", "Tablet - был(а) вчера")) } }
@Composable private fun ThemeScreen(onBack: () -> Unit) { Column(Modifier.fillMaxSize().padding(16.dp)) { TextButton(onClick = onBack) { Text("Назад") }; Text("Оформление", fontSize = 30.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); MenuCard(listOf("Тёмная", "Фиолетовая", "Океан", "Сакура")) } }
@Composable private fun ChatRow(chat: UiChat, onClick: () -> Unit) { Card(onClick = onClick, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) { Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(chat.title.first().uppercase(), MaterialTheme.colorScheme.secondary); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(chat.title, fontWeight = FontWeight.Bold, fontSize = 22.sp, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(chat.time, color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp) }; Text(chat.preview, color = Color.White.copy(alpha = 0.4f), maxLines = 1, overflow = TextOverflow.Ellipsis) } } } }
@Composable private fun Avatar(letter: String, color: Color) { Box(Modifier.size(58.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) { Text(letter, fontSize = 28.sp, fontWeight = FontWeight.Bold) } }
@Composable private fun InfoCard(value: String, subtitle: String) { Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))) { Column(Modifier.fillMaxWidth().padding(16.dp)) { Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = Color.White.copy(alpha = 0.45f)) } } }
@Composable private fun ActionRow(onMessage: () -> Unit, onAudio: () -> Unit, onVideo: () -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { SmallAction("Сообщение", onMessage); SmallAction("Звонок", onAudio); SmallAction("Видео", onVideo) } }
@Composable private fun SmallAction(label: String, onClick: () -> Unit) { Card(onClick = onClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))) { Box(Modifier.fillMaxWidth().padding(vertical = 18.dp), contentAlignment = Alignment.Center) { Text(label, fontWeight = FontWeight.SemiBold) } } }
@Composable private fun MenuCard(items: List<String>, onClick: (String) -> Unit = {}) { Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))) { Column(Modifier.fillMaxWidth()) { items.forEachIndexed { index, label -> Text(label, modifier = Modifier.fillMaxWidth().clickable { onClick(label) }.padding(horizontal = 16.dp, vertical = 16.dp), fontSize = 18.sp); if (index != items.lastIndex) Divider(color = Color.White.copy(alpha = 0.06f)) } } } }
@Composable private fun BottomTabs(modifier: Modifier, current: String, onChats: () -> Unit, onSettings: () -> Unit) { Card(modifier = modifier.padding(12.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xDD090B11))) { Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { TabButton("Чаты", current == "chats", onChats, Modifier.weight(1f)); TabButton("Настройки", current == "settings", onSettings, Modifier.weight(1f)) } } }
@Composable private fun TabButton(label: String, active: Boolean, onClick: () -> Unit, modifier: Modifier) { Button(onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = if (active) Color.White.copy(alpha = 0.1f) else Color.Transparent, contentColor = Color.White)) { Text(label) } }
