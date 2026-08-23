"use client";

import { useState } from "react";
import { Search, Bell, Sun, Moon } from "lucide-react";
import { useT } from "@/lib/i18n";
import { useAuthStore } from "@/store/auth-store";
import { useUiStore } from "@/store/ui-store";
import { Avatar } from "@/components/ui/avatar";
import { Input } from "@/components/ui/input";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown";
import { CommandPalette } from "./command-palette";

export function Topbar({ unreadNotifications = 0 }: { unreadNotifications?: number }) {
  const t = useT();
  const user = useAuthStore((s) => s.user);
  const clearSession = useAuthStore((s) => s.clearSession);
  const theme = useUiStore((s) => s.theme);
  const setTheme = useUiStore((s) => s.setTheme);
  const [paletteOpen, setPaletteOpen] = useState(false);

  const isDark = theme === "win11-dark" || theme === "red" || theme === "blue";

  return (
    <header className="flex h-14 shrink-0 items-center gap-3 border-b border-border bg-surface px-4">
      <button
        onClick={() => setPaletteOpen(true)}
        className="flex w-full max-w-sm items-center gap-2 rounded-win border border-border bg-surface-2 px-3 py-1.5 text-sm text-text-muted hover:border-accent"
      >
        <Search size={15} />
        <span className="flex-1 text-start">{t("nav.search")}</span>
        <kbd className="rounded border border-border px-1.5 py-0.5 text-[10px]">Ctrl K</kbd>
      </button>

      <div className="flex-1" />

      <button
        aria-label="toggle theme"
        onClick={() => setTheme(isDark ? "win11-light" : "win11-dark")}
        className="rounded-win p-2 text-text-muted hover:bg-surface-2"
      >
        {isDark ? <Sun size={17} /> : <Moon size={17} />}
      </button>

      <button aria-label="notifications" className="relative rounded-win p-2 text-text-muted hover:bg-surface-2">
        <Bell size={17} />
        {unreadNotifications > 0 && (
          <span className="absolute -end-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-danger px-1 text-[10px] font-bold text-white">
            {unreadNotifications > 9 ? "9+" : unreadNotifications}
          </span>
        )}
      </button>

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <button>
            <Avatar name={user?.displayName ?? "?"} src={user?.avatarUrl} presence={user?.presenceStatus} size={32} />
          </button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuItem disabled className="opacity-100 font-medium">
            {user?.displayName}
          </DropdownMenuItem>
          <DropdownMenuItem onSelect={() => clearSession()}>{t("common.signOut")}</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      <CommandPalette open={paletteOpen} onOpenChange={setPaletteOpen} />
    </header>
  );
}
