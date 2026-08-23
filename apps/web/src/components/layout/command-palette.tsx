"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import * as RadixDialog from "@radix-ui/react-dialog";
import { LayoutDashboard, MessageSquare, KanbanSquare, CalendarDays, FileText, Search } from "lucide-react";
import { useT } from "@/lib/i18n";
import { useWorkspaceStore } from "@/store/workspace-store";
import { cn } from "@/lib/utils";

interface CommandItem {
  id: string;
  label: string;
  icon: React.ComponentType<{ size?: number }>;
  href: string;
}

/** Global Ctrl/Cmd+K palette for search + navigation + (future) AI actions. */
export function CommandPalette({ open, onOpenChange }: { open: boolean; onOpenChange: (open: boolean) => void }) {
  const t = useT();
  const router = useRouter();
  const { activeWorkspaceId } = useWorkspaceStore();
  const [query, setQuery] = useState("");

  useEffect(() => {
    function handler(e: KeyboardEvent) {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "k") {
        e.preventDefault();
        onOpenChange(!open);
      }
      if (e.key === "Escape") onOpenChange(false);
    }
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [open, onOpenChange]);

  const items: CommandItem[] = [
    { id: "dashboard", label: t("nav.dashboard"), icon: LayoutDashboard, href: "/dashboard" },
    { id: "chat", label: t("nav.chat"), icon: MessageSquare, href: `/workspace/${activeWorkspaceId}/chat` },
    { id: "kanban", label: t("nav.kanban"), icon: KanbanSquare, href: `/workspace/${activeWorkspaceId}/kanban` },
    { id: "calendar", label: t("nav.calendar"), icon: CalendarDays, href: `/workspace/${activeWorkspaceId}/calendar` },
    { id: "documents", label: t("nav.documents"), icon: FileText, href: `/workspace/${activeWorkspaceId}/documents` }
  ];

  const filtered = items.filter((i) => i.label.toLowerCase().includes(query.toLowerCase()));

  return (
    <RadixDialog.Root open={open} onOpenChange={onOpenChange}>
      <RadixDialog.Portal>
        <RadixDialog.Overlay className="fixed inset-0 z-40 bg-black/40 backdrop-blur-sm" />
        <RadixDialog.Content className="fixed left-1/2 top-24 z-50 w-full max-w-lg -translate-x-1/2 rounded-win-lg border border-border bg-surface shadow-acrylic-lg">
          <RadixDialog.Title className="sr-only">{t("nav.commandPalette")}</RadixDialog.Title>
          <div className="flex items-center gap-2 border-b border-border px-3 py-2.5">
            <Search size={16} className="text-text-muted" />
            <input
              autoFocus
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder={t("nav.search")}
              className="w-full bg-transparent text-sm text-text outline-none placeholder:text-text-muted"
            />
          </div>
          <div className="max-h-80 overflow-y-auto p-2">
            {filtered.map((item) => {
              const Icon = item.icon;
              return (
                <button
                  key={item.id}
                  onClick={() => {
                    router.push(item.href);
                    onOpenChange(false);
                  }}
                  className={cn(
                    "flex w-full items-center gap-2.5 rounded-win px-2.5 py-2 text-start text-sm text-text",
                    "hover:bg-surface-2"
                  )}
                >
                  <Icon size={16} />
                  {item.label}
                </button>
              );
            })}
          </div>
        </RadixDialog.Content>
      </RadixDialog.Portal>
    </RadixDialog.Root>
  );
}
