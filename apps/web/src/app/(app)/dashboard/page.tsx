"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useT } from "@/lib/i18n";
import { useAuthStore } from "@/store/auth-store";
import { useWorkspaceStore } from "@/store/workspace-store";
import { SummaryCards } from "@/components/dashboard/summary-cards";
import { MyTasksWidget } from "@/components/dashboard/my-tasks-widget";
import { UpcomingMeetingsWidget } from "@/components/dashboard/upcoming-meetings-widget";
import { OnlineMembersWidget } from "@/components/dashboard/online-members-widget";
import type { DashboardSummaryResponse, EventResponse, TaskResponse, WorkspaceMemberResponse } from "@/types";

export default function DashboardPage() {
  const t = useT();
  const user = useAuthStore((s) => s.user);
  const { activeWorkspaceId } = useWorkspaceStore();
  const [summary, setSummary] = useState<DashboardSummaryResponse | null>(null);
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [events, setEvents] = useState<EventResponse[]>([]);
  const [members, setMembers] = useState<WorkspaceMemberResponse[]>([]);

  useEffect(() => {
    if (!activeWorkspaceId) return;
    api.get<DashboardSummaryResponse>(`/api/v1/workspaces/${activeWorkspaceId}/dashboard/summary`).then(setSummary);
    api.get<TaskResponse[]>("/api/v1/tasks/mine").then(setTasks);
    api.get<EventResponse[]>(`/api/v1/workspaces/${activeWorkspaceId}/calendar/events/upcoming`).then(setEvents);
    api.get<WorkspaceMemberResponse[]>(`/api/v1/workspaces/${activeWorkspaceId}/members`).then(setMembers);
  }, [activeWorkspaceId]);

  return (
    <div className="h-full overflow-y-auto p-6">
      <h1 className="mb-1 text-xl font-bold text-text">
        {t("dashboard.welcome")}, {user?.displayName}
      </h1>
      <p className="mb-5 text-sm text-text-muted">{t("common.tagline")}</p>

      {summary && <SummaryCards summary={summary} />}

      <div className="mt-5 grid grid-cols-1 gap-4 lg:grid-cols-3">
        <MyTasksWidget tasks={tasks} />
        <UpcomingMeetingsWidget events={events} />
        <OnlineMembersWidget members={members} />
      </div>
    </div>
  );
}
