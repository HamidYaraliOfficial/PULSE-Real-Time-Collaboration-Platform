"use client";

import { CheckSquare, MessageCircle, Bell, CalendarClock, FolderKanban, Users } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { useT } from "@/lib/i18n";
import type { DashboardSummaryResponse } from "@/types";

export function SummaryCards({ summary }: { summary: DashboardSummaryResponse }) {
  const t = useT();
  const items = [
    { icon: CheckSquare, label: t("dashboard.myTasks"), value: summary.myTaskCount },
    { icon: MessageCircle, label: t("dashboard.unreadMessages"), value: summary.unreadMessageCount },
    { icon: Bell, label: t("dashboard.notifications"), value: summary.unreadNotificationCount },
    { icon: CalendarClock, label: t("dashboard.upcomingMeetings"), value: summary.upcomingMeetingCount },
    { icon: FolderKanban, label: t("dashboard.activeProjects"), value: summary.activeProjectCount },
    { icon: Users, label: t("dashboard.onlineMembers"), value: summary.onlineMemberCount }
  ];

  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
      {items.map((item) => {
        const Icon = item.icon;
        return (
          <Card key={item.label}>
            <CardContent className="flex flex-col gap-2 pt-4">
              <Icon size={18} className="text-accent" />
              <span className="text-2xl font-bold text-text">{item.value}</span>
              <span className="text-xs text-text-muted">{item.label}</span>
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
}
