export type User = {
  id: string;
  email: string;
  username: string;
  displayName: string;
  passwordHash: string;
  createdAt: string;
  avatarColor: string;
  devices: DeviceSession[];
};

export type DeviceSession = {
  id: string;
  name: string;
  createdAt: string;
  lastSeenAt: string;
  pushToken?: string;
};

export type MessageType = "text" | "file" | "voice" | "video_note";

export type Message = {
  id: string;
  chatId: string;
  senderId: string;
  text: string;
  type: MessageType;
  fileUrl?: string;
  fileName?: string;
  durationMs?: number;
  createdAt: string;
};

export type DirectChat = {
  id: string;
  memberIds: [string, string];
  createdAt: string;
};

export type CallSession = {
  id: string;
  chatId: string;
  fromUserId: string;
  toUserId: string;
  mode: "audio" | "video";
  state: "ringing" | "accepted" | "ended";
  createdAt: string;
};

export type DatabaseShape = {
  users: User[];
  chats: DirectChat[];
  messages: Message[];
  calls: CallSession[];
};
