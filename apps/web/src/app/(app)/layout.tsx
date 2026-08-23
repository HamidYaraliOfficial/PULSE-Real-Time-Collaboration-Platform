"use client";

import { useEffect, useState } from "react";
import { Sidebar } from "@/components/layout/sidebar";
import { Topbar } from "@/components/layout/topbar";
import { api } from "@/lib/api";
import { useAuthStore } from "@/store/auth-store";
import { useWorkspaceStore } from "@/store/workspace-store";
import type { NotificationResponse, WorkspaceResponse } from "@/types";

export default function AppLayout({ children }: { children: React.ReactNode }) {
  const user = useAuthStore((s) => s.user);
  const { workspaces, setWorkspaces, activeWorkspaceId } = useWorkspaceStore();
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    if (!user) return;
    api.get<WorkspaceResponse[]>("/api/v1/workspaces").then(setWorkspaces);
  }, [user, setWorkspaces]);

  useEffect(() => {
    if (!user) return;
    api.get<{ count: number }>("/api/v1/notifications/unread-count").then((r) => setUnread(r.count));
  }, [user, activeWorkspaceId]);

  if (!user) return null;

  return (
    <div className="flex h-screen overflow-hidden bg-bg">
      <Sidebar />
      <div className="flex min-w-0 flex-1 flex-col">
        <Topbar unreadNotifications={unread} />
        <main className="min-h-0 flex-1 overflow-hidden">{children}</main>
      </div>
    </div>
  );
}
