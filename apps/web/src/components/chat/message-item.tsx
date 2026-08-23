"use client";

import { Pin, MoreHorizontal } from "lucide-react";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { timeAgo } from "@/lib/utils";
import { useT } from "@/lib/i18n";
import { useUiStore } from "@/store/ui-store";
import type { MessageResponse } from "@/types";

export function MessageItem({
  message,
  onReact,
  onOpenThread
}: {
  message: MessageResponse;
  onReact: (emoji: string) => void;
  onOpenThread: () => void;
}) {
  const t = useT();
  const locale = useUiStore((s) => s.locale);

  return (
    <div className="group flex gap-3 rounded-win px-3 py-2 hover:bg-surface-2">
      <Avatar name={message.authorName} src={message.authorAvatarUrl} size={36} />
      <div className="min-w-0 flex-1">
        <div className="flex items-baseline gap-2">
          <span className="text-sm font-semibold text-text">{message.authorName}</span>
          <span className="text-xs text-text-muted">{timeAgo(message.createdAt, locale)}</span>
          {message.isPinned && <Pin size={12} className="text-accent" />}
          {message.isEdited && <span className="text-xs text-text-muted">({t("chat.edited")})</span>}
        </div>
        <p className="whitespace-pre-wrap break-words text-sm text-text">{message.body}</p>

        {Object.keys(message.reactions).length > 0 && (
          <div className="mt-1 flex flex-wrap gap-1">
            {Object.entries(message.reactions).map(([emoji, count]) => (
              <button
                key={emoji}
                onClick={() => onReact(emoji)}
                className="rounded-full border border-border bg-surface px-1.5 py-0.5 text-xs hover:border-accent"
              >
                {emoji} {count}
              </button>
            ))}
          </div>
        )}

        {message.replyCount > 0 && (
          <button onClick={onOpenThread} className="mt-1 text-xs font-medium text-accent hover:underline">
            {message.replyCount} {t("chat.replies")}
          </button>
        )}
      </div>

      <div className="hidden shrink-0 items-start gap-1 group-hover:flex">
        <button onClick={() => onReact("👍")} className="rounded-win p-1 text-text-muted hover:bg-surface">
          🙂
        </button>
        <button className="rounded-win p-1 text-text-muted hover:bg-surface">
          <MoreHorizontal size={15} />
        </button>
      </div>
    </div>
  );
}
