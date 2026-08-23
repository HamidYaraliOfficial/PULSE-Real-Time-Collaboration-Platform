"use client";

import { Check } from "lucide-react";
import { useT } from "@/lib/i18n";
import { useUiStore } from "@/store/ui-store";
import { useAuthStore } from "@/store/auth-store";
import { useWorkspaceStore } from "@/store/workspace-store";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { BusinessHoursEditor } from "@/components/calendar/business-hours-editor";
import { cn } from "@/lib/utils";
import type { ThemeName, Locale } from "@/types";

const THEMES: { id: ThemeName; swatch: string }[] = [
  { id: "win11-light", swatch: "#0067c0" },
  { id: "win11-dark", swatch: "#60cdff" },
  { id: "win11-default", swatch: "#005fb8" },
  { id: "red", swatch: "#e5484d" },
  { id: "blue", swatch: "#3b82f6" }
];

const LOCALES: { id: Locale; label: string }[] = [
  { id: "en", label: "English" },
  { id: "fa", label: "فارسی" },
  { id: "zh", label: "中文" }
];

export default function SettingsPage() {
  const t = useT();
  const user = useAuthStore((s) => s.user);
  const { theme, locale, density, setTheme, setLocale, setDensity } = useUiStore();
  const { activeWorkspaceId } = useWorkspaceStore();

  return (
    <div className="h-full space-y-4 overflow-y-auto p-6">
      <h1 className="text-xl font-bold text-text">{t("settings.title")}</h1>

      <Card>
        <CardHeader>
          <CardTitle>{t("settings.profile")}</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-text-muted">
          {user?.displayName} — {user?.email}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>{t("settings.appearance")}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <p className="mb-2 text-xs font-medium text-text-muted">{t("settings.theme")}</p>
            <div className="flex flex-wrap gap-2">
              {THEMES.map((th) => (
                <button
                  key={th.id}
                  onClick={() => setTheme(th.id)}
                  className={cn(
                    "flex items-center gap-2 rounded-win border px-3 py-2 text-sm",
                    theme === th.id ? "border-accent" : "border-border hover:border-accent/50"
                  )}
                >
                  <span className="h-3.5 w-3.5 rounded-full" style={{ backgroundColor: th.swatch }} />
                  {t(`settings.theme${th.id === "win11-light" ? "Win11Light" : th.id === "win11-dark" ? "Win11Dark" : th.id === "win11-default" ? "Win11Default" : th.id === "red" ? "Red" : "Blue"}`)}
                  {theme === th.id && <Check size={13} className="text-accent" />}
                </button>
              ))}
            </div>
          </div>

          <div>
            <p className="mb-2 text-xs font-medium text-text-muted">{t("settings.language")}</p>
            <div className="flex gap-2">
              {LOCALES.map((l) => (
                <button
                  key={l.id}
                  onClick={() => setLocale(l.id)}
                  className={cn(
                    "rounded-win border px-3 py-1.5 text-sm",
                    locale === l.id ? "border-accent text-accent" : "border-border text-text-muted hover:border-accent/50"
                  )}
                >
                  {l.label}
                </button>
              ))}
            </div>
          </div>

          <div>
            <p className="mb-2 text-xs font-medium text-text-muted">{t("settings.density")}</p>
            <div className="flex gap-2">
              {(["comfortable", "compact"] as const).map((d) => (
                <button
                  key={d}
                  onClick={() => setDensity(d)}
                  className={cn(
                    "rounded-win border px-3 py-1.5 text-sm",
                    density === d ? "border-accent text-accent" : "border-border text-text-muted hover:border-accent/50"
                  )}
                >
                  {t(`settings.${d}`)}
                </button>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>

      {activeWorkspaceId && <BusinessHoursEditor workspaceId={activeWorkspaceId} />}
    </div>
  );
}
