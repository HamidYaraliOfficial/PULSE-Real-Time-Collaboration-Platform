"use client";

import { useRef, useState } from "react";
import { Send, Paperclip, Smile } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useT } from "@/lib/i18n";

export function MessageInput({
  channelName,
  onSend,
  onTyping
}: {
  channelName: string;
  onSend: (body: string) => void;
  onTyping: () => void;
}) {
  const t = useT();
  const [value, setValue] = useState("");
  const typingTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);

  function handleChange(v: string) {
    setValue(v);
    if (typingTimeout.current) clearTimeout(typingTimeout.current);
    onTyping();
    typingTimeout.current = setTimeout(() => {}, 2000);
  }

  function submit() {
    if (!value.trim()) return;
    onSend(value.trim());
    setValue("");
  }

  return (
    <div className="border-t border-border p-3">
      <div className="flex items-end gap-2 rounded-win-lg border border-border bg-surface p-2 focus-within:border-accent">
        <button className="rounded-win p-1.5 text-text-muted hover:bg-surface-2">
          <Paperclip size={16} />
        </button>
        <textarea
          value={value}
          onChange={(e) => handleChange(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              submit();
            }
          }}
          placeholder={t("chat.typeMessage", { channel: channelName })}
          rows={1}
          className="max-h-40 flex-1 resize-none bg-transparent text-sm text-text outline-none placeholder:text-text-muted"
        />
        <button className="rounded-win p-1.5 text-text-muted hover:bg-surface-2">
          <Smile size={16} />
        </button>
        <Button size="icon" onClick={submit} disabled={!value.trim()}>
          <Send size={15} />
        </Button>
      </div>
    </div>
  );
}
