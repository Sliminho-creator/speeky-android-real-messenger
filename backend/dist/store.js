import { mkdirSync, readFileSync, writeFileSync, existsSync } from "node:fs";
import { randomUUID, createHash } from "node:crypto";
import { join } from "node:path";
const storageDir = join(process.cwd(), "storage");
const dataFile = join(storageDir, "data.json");
function now() {
    return new Date().toISOString();
}
function colorFrom(seed) {
    const palette = ["#7C6CF2", "#5E9BFF", "#EF83A6", "#5BD0C7", "#9AD85B"];
    const idx = Math.abs(seed.split("").reduce((acc, c) => acc + c.charCodeAt(0), 0)) % palette.length;
    return palette[idx];
}
function hashPassword(value) {
    return createHash("sha256").update(value).digest("hex");
}
function initial() {
    const me = {
        id: randomUUID(),
        email: "hello@speeky.app",
        username: "sliminho",
        displayName: "Sliminho",
        passwordHash: hashPassword("password123"),
        createdAt: now(),
        avatarColor: colorFrom("sliminho"),
        devices: []
    };
    const forever = {
        id: randomUUID(),
        email: "forever@speeky.app",
        username: "forever",
        displayName: "forever",
        passwordHash: hashPassword("password123"),
        createdAt: now(),
        avatarColor: colorFrom("forever"),
        devices: []
    };
    const chat = {
        id: randomUUID(),
        memberIds: [me.id, forever.id],
        createdAt: now(),
    };
    const messages = [
        { id: randomUUID(), chatId: chat.id, senderId: forever.id, text: "маму ем", type: "text", createdAt: now() },
        { id: randomUUID(), chatId: chat.id, senderId: forever.id, text: "все норм", type: "text", createdAt: now() },
        { id: randomUUID(), chatId: chat.id, senderId: forever.id, text: "завтра узнаем", type: "text", createdAt: now() },
        { id: randomUUID(), chatId: chat.id, senderId: me.id, text: "как узнал ?", type: "text", createdAt: now() },
        { id: randomUUID(), chatId: chat.id, senderId: me.id, text: "время ж поздно уже за комп та не пустят", type: "text", createdAt: now() }
    ];
    return { users: [me, forever], chats: [chat], messages, calls: [] };
}
export function readDb() {
    mkdirSync(storageDir, { recursive: true });
    if (!existsSync(dataFile)) {
        const db = initial();
        writeDb(db);
        return db;
    }
    return JSON.parse(readFileSync(dataFile, "utf8"));
}
export function writeDb(db) {
    mkdirSync(storageDir, { recursive: true });
    writeFileSync(dataFile, JSON.stringify(db, null, 2), "utf8");
}
export function registerUser(email, username, displayName, password) {
    const db = readDb();
    const lowerEmail = email.trim().toLowerCase();
    const cleanUsername = username.trim().toLowerCase().replace(/^@/, "");
    if (db.users.some((u) => u.email === lowerEmail))
        throw new Error("EMAIL_TAKEN");
    if (db.users.some((u) => u.username === cleanUsername))
        throw new Error("USERNAME_TAKEN");
    const user = {
        id: randomUUID(),
        email: lowerEmail,
        username: cleanUsername,
        displayName: displayName.trim(),
        passwordHash: hashPassword(password),
        createdAt: now(),
        avatarColor: colorFrom(cleanUsername),
        devices: []
    };
    db.users.push(user);
    writeDb(db);
    return user;
}
export function loginUser(login, password, deviceName) {
    const db = readDb();
    const key = login.trim().toLowerCase().replace(/^@/, "");
    const user = db.users.find((u) => u.email === key || u.username === key);
    if (!user)
        return null;
    if (user.passwordHash !== hashPassword(password))
        return null;
    const device = {
        id: randomUUID(),
        name: deviceName,
        createdAt: now(),
        lastSeenAt: now(),
    };
    user.devices.push(device);
    writeDb(db);
    return { user, device };
}
export function findUserById(id) {
    return readDb().users.find((u) => u.id === id) ?? null;
}
export function searchUsers(query, excludeUserId) {
    const q = query.trim().toLowerCase().replace(/^@/, "");
    return readDb().users.filter((u) => u.id !== excludeUserId && (u.username.includes(q) || u.displayName.toLowerCase().includes(q)));
}
export function listChatsForUser(userId) {
    const db = readDb();
    return db.chats
        .filter((chat) => chat.memberIds.includes(userId))
        .map((chat) => {
        const otherId = chat.memberIds.find((id) => id !== userId);
        const other = db.users.find((u) => u.id === otherId);
        const messages = db.messages.filter((m) => m.chatId === chat.id).sort((a, b) => a.createdAt.localeCompare(b.createdAt));
        const last = messages[messages.length - 1];
        return {
            id: chat.id,
            peer: {
                id: other.id,
                username: other.username,
                displayName: other.displayName,
                avatarColor: other.avatarColor,
                lastSeen: other.devices.length ? "в сети" : "был(а) недавно"
            },
            lastMessage: last?.fileName || last?.text || "Нет сообщений",
            updatedAt: last?.createdAt || chat.createdAt,
            messages
        };
    })
        .sort((a, b) => b.updatedAt.localeCompare(a.updatedAt));
}
export function ensureDirectChat(userId, otherUserId) {
    const db = readDb();
    const existing = db.chats.find((chat) => chat.memberIds.includes(userId) && chat.memberIds.includes(otherUserId));
    if (existing)
        return existing;
    const created = {
        id: randomUUID(),
        memberIds: [userId, otherUserId],
        createdAt: now(),
    };
    db.chats.push(created);
    writeDb(db);
    return created;
}
export function addMessage(input) {
    const db = readDb();
    const msg = {
        id: randomUUID(),
        chatId: input.chatId,
        senderId: input.senderId,
        text: input.text,
        type: input.type,
        fileUrl: input.fileUrl,
        fileName: input.fileName,
        durationMs: input.durationMs,
        createdAt: now(),
    };
    db.messages.push(msg);
    writeDb(db);
    return msg;
}
export function messagesForChat(chatId) {
    return readDb().messages.filter((m) => m.chatId === chatId).sort((a, b) => a.createdAt.localeCompare(b.createdAt));
}
export function startCall(chatId, fromUserId, toUserId, mode) {
    const db = readDb();
    const call = {
        id: randomUUID(),
        chatId,
        fromUserId,
        toUserId,
        mode,
        state: "ringing",
        createdAt: now(),
    };
    db.calls.push(call);
    writeDb(db);
    return call;
}
export function updateCall(callId, state) {
    const db = readDb();
    const call = db.calls.find((c) => c.id === callId);
    if (!call)
        return null;
    call.state = state;
    writeDb(db);
    return call;
}
