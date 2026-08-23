"use client";

import { useEffect, useRef, useState } from "react";
import { Star, History } from "lucide-react";
import { api } from "@/lib/api";
import { useT } from "@/lib/i18n";
import { useAuthStore } from "@/store/auth-store";
import { useWebSocketClient } from "@/hooks/use-websocket";
import type { DocumentResponse } from "@/types";

interface Block {
  id: string;
  type: "paragraph" | "heading" | "checklist" | "quote" | "code";
  text: string;
}

function parseContent(raw: string): Block[] {
  try {
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed.blocks) && parsed.blocks.length
      ? parsed.blocks
      : [{ id: "b1", type: "paragraph", text: "" }];
  } catch {
    return [{ id: "b1", type: "paragraph", text: "" }];
  }
}

/**
 * Collaborative document editor.
 *
 * Every keystroke (debounced) is persisted via PUT .../content, which the
 * server both saves (with a version snapshot for history/restore) and
 * rebroadcasts on /topic/document.{id}. Other open tabs merge that
 * broadcast in - so this is genuinely live multi-user editing, using a
 * simple last-write-wins strategy. See DocumentService's doc comment for
 * how this upgrades to full CRDT-based conflict-free merging without
 * changing this component's REST/WS calls.
 */
export function DocumentEditor({ workspaceId, doc }: { workspaceId: string; doc: DocumentResponse }) {
  const t = useT();
  const user = useAuthStore((s) => s.user);
  const ws = useWebSocketClient();
  const [title, setTitle] = useState(doc.title);
  const [blocks, setBlocks] = useState<Block[]>(() => parseContent(doc.content));
  const [isFavorite, setIsFavorite] = useState(doc.isFavorite);
  const [peers, setPeers] = useState<string[]>([]);
  const saveTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);
  const applyingRemote = useRef(false);

  useEffect(() => {
    if (!ws.connected) return;
    const sub = ws.subscribe(`/topic/document.${doc.id}`, (frame) => {
      const event = JSON.parse(frame.body);
      if (event.userId === user?.id) return;
      if (event.type === "CONTENT" && event.content) {
        applyingRemote.current = true;
        setBlocks(parseContent(event.content));
      }
      if (event.type === "PRESENCE_JOIN") {
        setPeers((prev) => Array.from(new Set([...prev, event.displayName])));
      }
    });
    return () => sub?.unsubscribe();
  }, [ws.connected, doc.id, user?.id]);

  function scheduleSave(nextBlocks: Block[], nextTitle: string) {
    if (saveTimeout.current) clearTimeout(saveTimeout.current);
    saveTimeout.current = setTimeout(async () => {
      await api.put(`/api/v1/workspaces/${workspaceId}/documents/${doc.id}/content`, {
        content: JSON.stringify({ blocks: nextBlocks }),
        title: nextTitle
      });
    }, 600);
  }

  function updateBlock(id: string, text: string) {
    const next = blocks.map((b) => (b.id === id ? { ...b, text } : b));
    setBlocks(next);
    scheduleSave(next, title);
  }

  function addBlock() {
    const next = [...blocks, { id: crypto.randomUUID(), type: "paragraph" as const, text: "" }];
    setBlocks(next);
    scheduleSave(next, title);
  }

  async function toggleFavorite() {
    const updated = await api.post<DocumentResponse>(`/api/v1/workspaces/${workspaceId}/documents/${doc.id}/favorite`);
    setIsFavorite(updated.isFavorite);
  }

  return (
    <div className="mx-auto h-full max-w-3xl overflow-y-auto p-8">
      <div className="mb-4 flex items-center justify-between">
        <input
          value={title}
          onChange={(e) => {
            setTitle(e.target.value);
            scheduleSave(blocks, e.target.value);
          }}
          placeholder={t("documents.untitled")}
          className="w-full bg-transparent text-3xl font-bold text-text outline-none"
        />
        <div className="flex shrink-0 items-center gap-2">
          <button onClick={toggleFavorite} className="rounded-win p-1.5 hover:bg-surface-2">
            <Star size={16} className={isFavorite ? "fill-warning text-warning" : "text-text-muted"} />
          </button>
          <button className="rounded-win p-1.5 hover:bg-surface-2">
            <History size={16} className="text-text-muted" />
          </button>
        </div>
      </div>

      {peers.length > 0 && (
        <p className="mb-2 text-xs text-text-muted">{peers.join(", ")} {t("chat.typing")}</p>
      )}

      <div className="space-y-2">
        {blocks.map((block) => (
          <textarea
            key={block.id}
            value={block.text}
            onChange={(e) => updateBlock(block.id, e.target.value)}
            rows={1}
            placeholder="Type '/' for blocks..."
            className="w-full resize-none overflow-hidden bg-transparent text-sm leading-relaxed text-text outline-none placeholder:text-text-muted"
            onInput={(e) => {
              const el = e.currentTarget;
              el.style.height = "auto";
              el.style.height = `${el.scrollHeight}px`;
            }}
          />
        ))}
        <button onClick={addBlock} className="text-xs text-text-muted hover:text-accent">
          + Block
        </button>
      </div>
    </div>
  );
}
