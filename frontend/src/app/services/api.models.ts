export type ChatRoomType = 'SEGUIMIENTO';
export type ChatSenderRole = 'MECANICO' | 'USUARIO';
export type AuthUserRole = 'USER' | 'TALLER' | 'ADMIN';

export interface AuthLoginRequest {
  email: string;
  password: string;
}

export interface AuthRegisterRequest {
  fullName: string;
  email: string;
  password: string;
  role: AuthUserRole;
}

export interface AuthUserResponse {
  id: number;
  fullName: string;
  email: string;
  role: AuthUserRole;
  avatarUrl: string;
  createdAt: string;
}

export interface ChatJoinResponse {
  roomType: ChatRoomType;
  participantId: number;
  activeUsers: number;
  maxUsers: number;
  joined: boolean;
}

export interface ChatMessageRequest {
  participantId: number;
  roomType: ChatRoomType;
  senderRole: ChatSenderRole;
  commentText: string;
}

export interface ChatMessageResponse {
  id: number;
  roomType: ChatRoomType;
  participantId: number;
  senderRole: ChatSenderRole;
  commentText: string;
  wordCount: number;
  readByUser: boolean;
  createdAt: string;
}
