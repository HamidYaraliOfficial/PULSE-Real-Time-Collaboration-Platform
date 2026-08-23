// Shared frontend types, mirroring the backend DTOs in apps/backend/.../dto

export type WorkspaceRole = "OWNER" | "ADMIN" | "MANAGER" | "MEMBER" | "GUEST";
export type PresenceStatus = "ONLINE" | "AWAY" | "BUSY" | "DO_NOT_DISTURB" | "OFFLINE";
export type TaskStatus = "BACKLOG" | "TODO" | "IN_PROGRESS" | "REVIEW" | "TESTING" | "DONE";
export type TaskPriority = "LOW" | "MEDIUM" | "HIGH" | "URGENT";
export type ThemeName = "win11-light" | "win11-dark" | "win11-default" | "red" | "blue";
export type Locale = "en" | "fa" | "zh";

export interface UserSummary {
  id: string;
  email: string;
  displayName: string;
  avatarUrl?: string | null;
  title?: string | null;
  presenceStatus: PresenceStatus;
  locale: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserSummary;
}

export interface WorkspaceResponse {
  id: string;
  name: string;
  slug: string;
  description?: string | null;
  logoUrl?: string | null;
  timezone: string;
  role: WorkspaceRole;
  memberCount: number;
}

export interface WorkspaceMemberResponse {
  userId: string;
  displayName: string;
  avatarUrl?: string | null;
  role: WorkspaceRole;
  presenceStatus: PresenceStatus;
  title?: string | null;
}

export interface ChannelResponse {
  id: string;
  name: string;
  topic?: string | null;
  type: "PUBLIC" | "PRIVATE" | "DIRECT" | "GROUP";
  unreadCount: number;
  lastMessageAt?: string | null;
}

export interface MessageResponse {
  id: string;
  channelId: string;
  authorId: string;
  authorName: string;
  authorAvatarUrl?: string | null;
  body: string;
  parentMessageId?: string | null;
  replyCount: number;
  isPinned: boolean;
  isEdited: boolean;
  reactions: Record<string, number>;
  createdAt: string;
}

export interface ProjectResponse {
  id: string;
  name: string;
  description?: string | null;
  status: "ACTIVE" | "ON_HOLD" | "COMPLETED" | "ARCHIVED";
  taskCount: number;
  completedCount: number;
}

export interface TaskResponse {
  id: string;
  projectId: string;
  title: string;
  description?: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  assigneeId?: string | null;
  assigneeName?: string | null;
  reporterId: string;
  dueDate?: string | null;
  position: number;
  labels: string[];
  commentCount: number;
  createdAt: string;
}

export interface EventResponse {
  id: string;
  title: string;
  description?: string | null;
  location?: string | null;
  eventType: "EVENT" | "MEETING" | "DEADLINE" | "REMINDER";
  startsAt: string;
  endsAt: string;
  meetingUrl?: string | null;
  attendees: { userId: string; displayName: string; rsvp: string }[];
}

export interface DocumentResponse {
  id: string;
  title: string;
  content: string;
  icon?: string | null;
  isFavorite: boolean;
  parentId?: string | null;
  hasChildren: boolean;
  updatedAt: string;
  createdBy: string;
}

export interface NotificationResponse {
  id: string;
  type: string;
  title: string;
  body?: string | null;
  link?: string | null;
  isRead: boolean;
  createdAt: string;
}

export interface DashboardSummaryResponse {
  myTaskCount: number;
  unreadMessageCount: number;
  unreadNotificationCount: number;
  upcomingMeetingCount: number;
  activeProjectCount: number;
  onlineMemberCount: number;
}

export interface BusinessHourEntry {
  dayOfWeek: number; // 0=Sunday .. 6=Saturday
  isClosed: boolean;
  openTime: string | null; // "09:00"
  closeTime: string | null; // "18:00"
  timezone: string;
}

export interface BusinessHoursStatusResponse {
  isOpenNow: boolean;
  timezone: string;
  currentLocalTime: string;
  nextChangeAt: string | null;
  nextChangeType: "OPENS" | "CLOSES";
  secondsUntilNextChange: number | null;
  schedule: BusinessHourEntry[];
}
