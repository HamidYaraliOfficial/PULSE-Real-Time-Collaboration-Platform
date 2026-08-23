"use client";

import { useEffect, useRef, useState } from "react";
import { Hash, Lock } from "lucide-react";
import { api } from "@/lib/api";
import { useT } from "@/lib/i18n";
import { useWebSocketClient } from "@/hooks/use-websocket";
import { usePresenceHeartbeat } from "@/hooks/use-presence-heartbeat";
import { MessageItem } from "./message-item";
import { MessageInput } from "./message-input";
import type { ChannelResponse, MessageResponse } from "@/types";

/**
 * Real-time chat surface for a single channel. History loads over REST;
 * new messages, edits, deletions and reactions arrive over the shared
 * STOMP connection on /topic/channel.{id} and are merged into local state
 * so every open tab stays in sync without polling.
 */
export function ChatWindow({ channel }: { channel: ChannelResponse }) {
  const t = useT();
  const [messages, setMessages] = useState<MessageResponse[]>([]);
  const [typingUser, setTypingUser] = useState<string | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);
  const ws = useWebSocketClient();
  usePresenceHeartbeat(ws);

  useEffect(() => {
    api.get<MessageResponse[]>(`/api/v1/channels/${channel.id}/messages`).then((history) =>
      setMessages(history.slice().reverse())
    );
  }, [channel.id]);

  useEffect(() => {
    if (!ws.connected) return;
    const sub = ws.subscribe(`/topic/channel.${channel.id}`, (frame) => {
      const payload = JSON.parse(frame.body);
      if (payload.deletedMessageId) {
        setMessages((prev) => prev.filter((m) => m.id !== payload.deletedMessageId));
        return;
      }
      setMessages((prev) => {
        const exists = prev.find((m) => m.id === payload.id);
        if (exists) return prev.map((m) => (m.id === payload.id ? payload : m));
        return [...prev, payload as MessageResponse];
      });
    });
    const typingSub = ws.subscribe(`/topic/channel.${channel.id}.typing`, (frame) => {
      const event = JSON.parse(frame.body);
      if (event.isTyping) {
        setTypingUser(event.displayName);
        setTimeout(() => setTypingUser(null), 3000);
      }
    });
    return () => {
      sub?.unsubscribe();
      typingSub?.unsubscribe();
    };
  }, [ws.connected, channel.id]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages.length]);

  async function sendMessage(body: string) {
    await api.post(`/api/v1/channels/${channel.id}/messages`, { body });
  }

  function handleTyping() {
    ws.publish("/app/typing", { channelId: channel.id, isTyping: true, displayName: "You" });
  }

  async function react(messageId: string, emoji: string) {
    await api.post(`/api/v1/channels/${channel.id}/messages/${messageId}/reactions`, { emoji });
  }

  return (
    <div className="flex h-full flex-col">
      <div className="flex h-12 shrink-0 items-center gap-2 border-b border-border px-4">
        {channel.type === "PRIVATE" ? <Lock size={15} className="text-text-muted" /> : <Hash size={15} className="text-text-muted" />}
        <span className="text-sm font-semibold text-text">{channel.name}</span>
        {channel.topic && <span className="truncate text-xs text-text-muted">— {channel.topic}</span>}
      </div>

      <div className="flex-1 space-y-0.5 overflow-y-auto px-2 py-3">
        {messages.length === 0 && (
          <p className="px-3 py-8 text-center text-sm text-text-muted">{t("chat.noMessages")}</p>
        )}
        {messages.map((m) => (
          <MessageItem key={m.id} message={m} onReact={(emoji) => react(m.id, emoji)} onOpenThread={() => {}} />
        ))}
        {typingUser && <p className="px-3 text-xs italic text-text-muted">{typingUser} {t("chat.typing")}</p>}
        <div ref={bottomRef} />
      </div>

      <MessageInput channelName={channel.name} onSend={sendMessage} onTyping={handleTyping} />
    </div>
  );
}
