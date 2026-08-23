"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { ChatWindow } from "@/components/chat/chat-window";
import { useT } from "@/lib/i18n";
import type { ChannelResponse } from "@/types";
import { Hash } from "lucide-react";

export default function ChannelPage({ params }: { params: { workspaceId: string; channelId: string } }) {
  const t = useT();
  const [channels, setChannels] = useState<ChannelResponse[]>([]);

  useEffect(() => {
    api.get<ChannelResponse[]>(`/api/v1/workspaces/${params.workspaceId}/channels`).then(setChannels);
  }, [params.workspaceId]);

  const channel = channels.find((c) => c.id === params.channelId);

  return (
    <div className="flex h-full">
      <div className="hidden w-56 shrink-0 flex-col border-e border-border p-3 md:flex">
        <h3 className="mb-2 text-xs font-semibold uppercase text-text-muted">{t("chat.channels")}</h3>
        <div className="space-y-0.5">
          {channels.map((c) => (
            <a
              key={c.id}
              href={`/workspace/${params.workspaceId}/chat/${c.id}`}
              className={`flex items-center gap-1.5 rounded-win px-2 py-1.5 text-sm ${
                c.id === params.channelId ? "bg-accent/15 text-accent" : "text-text-muted hover:bg-surface-2"
              }`}
            >
              <Hash size={13} />
              <span className="truncate">{c.name}</span>
              {c.unreadCount > 0 && (
                <span className="ms-auto rounded-full bg-danger px-1.5 text-[10px] text-white">{c.unreadCount}</span>
              )}
            </a>
          ))}
        </div>
      </div>
      <div className="min-w-0 flex-1">{channel && <ChatWindow channel={channel} />}</div>
    </div>
  );
}
