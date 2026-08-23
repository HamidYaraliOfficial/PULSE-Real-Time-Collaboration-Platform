"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Avatar } from "@/components/ui/avatar";
import { useT } from "@/lib/i18n";
import type { WorkspaceMemberResponse } from "@/types";

export function OnlineMembersWidget({ members }: { members: WorkspaceMemberResponse[] }) {
  const t = useT();
  const online = members.filter((m) => m.presenceStatus !== "OFFLINE");
  return (
    <Card>
      <CardHeader>
        <CardTitle>{t("dashboard.onlineMembers")}</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-wrap gap-2">
        {online.length === 0 && <p className="text-sm text-text-muted">{t("common.empty")}</p>}
        {online.map((m) => (
          <div key={m.userId} className="flex items-center gap-1.5 rounded-full bg-surface-2 py-1 pe-2.5 ps-1">
            <Avatar name={m.displayName} src={m.avatarUrl} presence={m.presenceStatus} size={24} />
            <span className="text-xs text-text">{m.displayName}</span>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
