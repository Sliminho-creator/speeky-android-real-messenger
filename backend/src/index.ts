import "dotenv/config";
import cors from "cors";
import express from "express";
import { createServer } from "node:http";
import { mkdirSync } from "node:fs";
import { join } from "node:path";
import multer from "multer";
import { Server as SocketIOServer } from "socket.io";
import { requireAuth, signToken, type AuthRequest } from "./auth.js";
import {
  addMessage,
  ensureDirectChat,
  findUserById,
  listChatsForUser,
  loginUser,
  messagesForChat,
  registerUser,
  searchUsers,
  startCall,
  updateCall,
} from "./store.js";

const port = Number(process.env.PORT || 4000);
const publicBaseUrl = process.env.PUBLIC_BASE_URL || `http://localhost:${port}`;

const app = express();
const server = createServer(app);
const io = new SocketIOServer(server, {
  cors: { origin: "*" },
});

const uploadsDir = join(process.cwd(), "uploads");
mkdirSync(uploadsDir, { recursive: true });
const upload = multer({ dest: uploadsDir });

app.use(cors());
app.use(express.json({ limit: "20mb" }));
app.use("/uploads", express.static(uploadsDir));

io.on("connection", (socket) => {
  socket.on("register-user", (userId: string) => {
    socket.join(`user:${userId}`);
  });

  socket.on("typing", ({ toUserId, fromUserId, chatId }) => {
    io.to(`user:${toUserId}`).emit("typing", { fromUserId, chatId });
  });
});

app.get("/health", (_req, res) => {
  res.json({ ok: true });
});

app.post("/api/auth/register", (req, res) => {
  const { email, username, displayName, password } = req.body || {};
  if (!email || !username || !displayName || !password) {
    return res.status(400).json({ error: "INVALID_PAYLOAD" });
  }
  try {
    const user = registerUser(email, username, displayName, password);
    const token = signToken(user.id, "first-device");
    return res.status(201).json({ token, user: { id: user.id, email: user.email, username: user.username, displayName: user.displayName, avatarColor: user.avatarColor } });
  } catch (error) {
    const code = error instanceof Error ? error.message : "REGISTER_FAILED";
    return res.status(409).json({ error: code });
  }
});

app.post("/api/auth/login", (req, res) => {
  const { login, password, deviceName } = req.body || {};
  if (!login || !password) {
    return res.status(400).json({ error: "INVALID_PAYLOAD" });
  }
  const result = loginUser(login, password, deviceName || "Android device");
  if (!result) return res.status(401).json({ error: "INVALID_CREDENTIALS" });
  const token = signToken(result.user.id, result.device.id);
  return res.json({ token, user: { id: result.user.id, email: result.user.email, username: result.user.username, displayName: result.user.displayName, avatarColor: result.user.avatarColor, devices: result.user.devices } });
});

app.get("/api/auth/me", requireAuth, (req: AuthRequest, res) => {
  const user = findUserById(req.auth!.userId);
  if (!user) return res.status(404).json({ error: "NOT_FOUND" });
  res.json({ user: { id: user.id, email: user.email, username: user.username, displayName: user.displayName, avatarColor: user.avatarColor, devices: user.devices } });
});

app.get("/api/users/search", requireAuth, (req: AuthRequest, res) => {
  const rawQ = req.query.q;
  const q = typeof rawQ === "string" ? rawQ : Array.isArray(rawQ) && typeof rawQ[0] === "string" ? rawQ[0] : "";
  if (!q.trim()) return res.json({ items: [] });
  const users = searchUsers(q, req.auth!.userId).map((u) => ({ id: u.id, username: u.username, displayName: u.displayName, avatarColor: u.avatarColor }));
  res.json({ items: users });
});

app.get("/api/chats", requireAuth, (req: AuthRequest, res) => {
  res.json({ items: listChatsForUser(req.auth!.userId) });
});

app.post("/api/chats/direct", requireAuth, (req: AuthRequest, res) => {
  const { userId } = req.body || {};
  if (!userId) return res.status(400).json({ error: "INVALID_PAYLOAD" });
  const chat = ensureDirectChat(req.auth!.userId, String(userId));
  res.status(201).json({ item: chat });
});

app.get("/api/chats/:id/messages", requireAuth, (req: AuthRequest, res) => {
  res.json({ items: messagesForChat(String(req.params.id)) });
});

app.post("/api/messages/direct", requireAuth, (req: AuthRequest, res) => {
  const { chatId, text, type, fileUrl, fileName, durationMs } = req.body || {};
  if (!chatId || typeof text !== "string") return res.status(400).json({ error: "INVALID_PAYLOAD" });
  const message = addMessage({ chatId: String(chatId), senderId: req.auth!.userId, text, type: (type || "text") as any, fileUrl, fileName, durationMs });
  const chat = listChatsForUser(req.auth!.userId).find((c) => c.id === chatId);
  const peerId = chat?.peer?.id;
  if (peerId) {
    io.to(`user:${peerId}`).emit("direct-message", { chatId, message, preview: fileName || text });
  }
  res.status(201).json({ item: message });
});

app.post("/api/upload", requireAuth, upload.single("file"), (req, res) => {
  if (!req.file) return res.status(400).json({ error: "FILE_REQUIRED" });
  return res.status(201).json({ url: `${publicBaseUrl}/uploads/${req.file.filename}`, fileName: req.file.originalname, size: req.file.size });
});

app.post("/api/calls/offer", requireAuth, (req: AuthRequest, res) => {
  const { chatId, toUserId, mode, sdp } = req.body || {};
  if (!chatId || !toUserId || !mode) return res.status(400).json({ error: "INVALID_PAYLOAD" });
  const call = startCall(String(chatId), req.auth!.userId, String(toUserId), mode === "video" ? "video" : "audio");
  io.to(`user:${toUserId}`).emit("call:incoming", { callId: call.id, chatId, fromUserId: req.auth!.userId, mode: call.mode, sdp });
  res.status(201).json({ item: call });
});

app.post("/api/calls/answer", requireAuth, (req: AuthRequest, res) => {
  const { callId, toUserId, accepted, sdp } = req.body || {};
  if (!callId || !toUserId) return res.status(400).json({ error: "INVALID_PAYLOAD" });
  const updated = updateCall(String(callId), accepted ? "accepted" : "ended");
  io.to(`user:${toUserId}`).emit("call:answer", { callId, accepted: !!accepted, sdp });
  res.json({ item: updated });
});

app.post("/api/calls/ice", requireAuth, (req: AuthRequest, res) => {
  const { toUserId, callId, candidate } = req.body || {};
  if (!toUserId || !callId || !candidate) return res.status(400).json({ error: "INVALID_PAYLOAD" });
  io.to(`user:${toUserId}`).emit("call:ice", { callId, candidate });
  res.status(204).end();
});

server.listen(port, () => {
  console.log(`speeky-backend listening on :${port}`);
});
