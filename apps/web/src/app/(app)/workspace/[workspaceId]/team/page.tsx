"use client";

import { useEffect, useState } from "react";
import { UserPlus } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import { useT } from "@/lib/i18n";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { WorkspaceMemberResponse } from "@/types";

const roleTone = { OWNER: "accent", ADMIN: "warning", MANAGER: "neutral", MEMBER: "neutral", GUEST: "neutral" } as const;

/** Team Hub: everyone in the workspace, their role, and live presence. */
export default function TeamPage({ params }: { params: { workspaceId: string } }) {
  const t = useT();
  const [members, setMembers] = useState<WorkspaceMemberResponse[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<WorkspaceMemberResponse[]>(`/api/v1/workspaces/${params.workspaceId}/members`).then(setMembers);
  }, [params.workspaceId]);

  async function invite() {
    const email = window.prompt(t("team.invite"));
    if (!email) return;
    try {
      const member = await api.post<WorkspaceMemberResponse>(`/api/v1/workspaces/${params.workspaceId}/members/invite`, { email });
      setMembers((prev) => [...prev, member]);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to invite");
    }
  }

  return (
    <div className="h-full overflow-y-auto p-6">
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-xl font-bold text-text">{t("team.title")}</h1>
        <Button size="sm" onClick={invite}>
          <UserPlus size={14} /> {t("team.invite")}
        </Button>
      </div>
      {error && <p className="mb-3 text-sm text-danger">{error}</p>}
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {members.map((m) => (
          <div key={m.userId} className="flex items-center gap-3 rounded-win-lg border border-border bg-surface p-3">
            <Avatar name={m.displayName} src={m.avatarUrl} presence={m.presenceStatus} size={40} />
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium text-text">{m.displayName}</p>
              <p className="truncate text-xs text-text-muted">{m.title ?? "—"}</p>
            </div>
            <Badge tone={roleTone[m.role]}>{t(`team.${m.role.toLowerCase()}`)}</Badge>
          </div>
        ))}
      </div>
    </div>
  );
}
