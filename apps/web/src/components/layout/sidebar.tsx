"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  MessageSquare,
  KanbanSquare,
  CalendarDays,
  FileText,
  Users,
  Settings,
  ChevronsUpDown
} from "lucide-react";
import { cn } from "@/lib/utils";
import { useT } from "@/lib/i18n";
import { useWorkspaceStore } from "@/store/workspace-store";
import { Avatar } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger
} from "@/components/ui/dropdown";

export function Sidebar() {
  const t = useT();
  const pathname = usePathname();
  const { workspaces, activeWorkspaceId, setActiveWorkspace } = useWorkspaceStore();
  const active = workspaces.find((w) => w.id === activeWorkspaceId) ?? workspaces[0];

  const nav = [
    { href: "/dashboard", label: t("nav.dashboard"), icon: LayoutDashboard },
    { href: `/workspace/${active?.id}/chat`, label: t("nav.chat"), icon: MessageSquare },
    { href: `/workspace/${active?.id}/kanban`, label: t("nav.kanban"), icon: KanbanSquare },
    { href: `/workspace/${active?.id}/calendar`, label: t("nav.calendar"), icon: CalendarDays },
    { href: `/workspace/${active?.id}/documents`, label: t("nav.documents"), icon: FileText },
    { href: `/workspace/${active?.id}/team`, label: t("nav.team"), icon: Users }
  ];

  return (
    <aside className="acrylic flex h-full w-64 shrink-0 flex-col border-e border-border">
      <div className="p-3">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button className="flex w-full items-center gap-2 rounded-win-lg p-2 text-start hover:bg-surface-2">
              <span className="flex h-8 w-8 items-center justify-center rounded-win bg-accent text-accent-text font-bold">
                {active?.name?.[0] ?? "P"}
              </span>
              <span className="flex-1 truncate text-sm font-semibold text-text">
                {active?.name ?? t("common.appName")}
              </span>
              <ChevronsUpDown size={14} className="text-text-muted" />
            </button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            {workspaces.map((w) => (
              <DropdownMenuItem key={w.id} onSelect={() => setActiveWorkspace(w.id)}>
                {w.name}
              </DropdownMenuItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <nav className="flex-1 space-y-0.5 overflow-y-auto px-2">
        {nav.map((item) => {
          const isActive = pathname === item.href || pathname.startsWith(item.href + "/");
          const Icon = item.icon;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center gap-2.5 rounded-win px-2.5 py-2 text-sm font-medium transition-colors",
                isActive ? "bg-accent/15 text-accent" : "text-text-muted hover:bg-surface-2 hover:text-text"
              )}
            >
              <Icon size={17} />
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="border-t border-border p-2">
        <Link
          href="/settings"
          className={cn(
            "flex items-center gap-2.5 rounded-win px-2.5 py-2 text-sm font-medium",
            pathname === "/settings" ? "bg-accent/15 text-accent" : "text-text-muted hover:bg-surface-2 hover:text-text"
          )}
        >
          <Settings size={17} />
          {t("nav.settings")}
        </Link>
      </div>
    </aside>
  );
}
