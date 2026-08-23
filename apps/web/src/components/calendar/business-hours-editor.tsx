"use client";

import { useEffect, useState } from "react";
import { Clock } from "lucide-react";
import { api } from "@/lib/api";
import { useT } from "@/lib/i18n";
import { useUiStore } from "@/store/ui-store";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { formatDuration } from "@/lib/utils";
import type { BusinessHourEntry, BusinessHoursStatusResponse } from "@/types";

const DAY_KEYS = ["sun", "mon", "tue", "wed", "thu", "fri", "sat"];
const DAY_LABELS: Record<string, { en: string; fa: string; zh: string }> = {
  sun: { en: "Sunday", fa: "یکشنبه", zh: "周日" },
  mon: { en: "Monday", fa: "دوشنبه", zh: "周一" },
  tue: { en: "Tuesday", fa: "سه‌شنبه", zh: "周二" },
  wed: { en: "Wednesday", fa: "چهارشنبه", zh: "周三" },
  thu: { en: "Thursday", fa: "پنجشنبه", zh: "周四" },
  fri: { en: "Friday", fa: "جمعه", zh: "周五" },
  sat: { en: "Saturday", fa: "شنبه", zh: "周六" }
};

function emptyWeek(): BusinessHourEntry[] {
  return Array.from({ length: 7 }, (_, day) => ({
    dayOfWeek: day,
    isClosed: day === 5 || day === 6, // sensible default: closed Fri/Sat, editable by the user
    openTime: "09:00",
    closeTime: "18:00",
    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
  }));
}

/**
 * Fully user-editable opening-hours form. Nothing here is hardcoded: the
 * user enters open/close time (or marks the day fully closed) for every
 * day of the week, and the live status card below - backed by
 * BusinessHoursService on the server - continuously shows whether the
 * workspace is open right now and a live countdown to the next change.
 */
export function BusinessHoursEditor({ workspaceId }: { workspaceId: string }) {
  const t = useT();
  const locale = useUiStore((s) => s.locale);
  const [days, setDays] = useState<BusinessHourEntry[]>(emptyWeek());
  const [status, setStatus] = useState<BusinessHoursStatusResponse | null>(null);
  const [countdown, setCountdown] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api.get<BusinessHourEntry[]>(`/api/v1/workspaces/${workspaceId}/business-hours`).then((schedule) => {
      if (schedule.length) setDays(schedule);
    });
    refreshStatus();
  }, [workspaceId]);

  async function refreshStatus() {
    const s = await api.get<BusinessHoursStatusResponse>(`/api/v1/workspaces/${workspaceId}/business-hours/status`);
    setStatus(s);
    setCountdown(s.secondsUntilNextChange);
  }

  useEffect(() => {
    if (countdown === null) return;
    const interval = setInterval(() => {
      setCountdown((prev) => {
        if (prev === null) return prev;
        if (prev <= 1) {
          refreshStatus();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [countdown !== null]);

  function updateDay(index: number, patch: Partial<BusinessHourEntry>) {
    setDays((prev) => prev.map((d, i) => (i === index ? { ...d, ...patch } : d)));
  }

  async function save() {
    setSaving(true);
    try {
      await api.put(`/api/v1/workspaces/${workspaceId}/business-hours`, { days });
      await refreshStatus();
    } finally {
      setSaving(false);
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Clock size={15} /> {t("calendar.businessHours")}
        </CardTitle>
        {status && (
          <div className="flex items-center gap-2">
            <Badge tone={status.isOpenNow ? "success" : "danger"}>
              {status.isOpenNow ? t("calendar.openNow") : t("calendar.closedNow")}
            </Badge>
            {countdown !== null && countdown > 0 && (
              <span className="text-xs text-text-muted">
                {status.isOpenNow ? t("calendar.closesIn") : t("calendar.opensIn")} {formatDuration(countdown)}
              </span>
            )}
          </div>
        )}
      </CardHeader>
      <CardContent className="space-y-3">
        <p className="text-xs text-text-muted">{t("calendar.setYourHours")}</p>
        <div className="space-y-2">
          {days.map((day, index) => (
            <div key={day.dayOfWeek} className="flex items-center gap-2 text-sm">
              <span className="w-24 shrink-0 text-text">
                {DAY_LABELS[DAY_KEYS[day.dayOfWeek]]?.[locale] ?? DAY_KEYS[day.dayOfWeek]}
              </span>
              <label className="flex items-center gap-1.5 text-xs text-text-muted">
                <input
                  type="checkbox"
                  checked={!day.isClosed}
                  onChange={(e) => updateDay(index, { isClosed: !e.target.checked })}
                />
                {t("calendar.openNow")}
              </label>
              {!day.isClosed ? (
                <>
                  <input
                    type="time"
                    value={day.openTime ?? "09:00"}
                    onChange={(e) => updateDay(index, { openTime: e.target.value })}
                    className="rounded-win border border-border bg-surface px-2 py-1 text-xs"
                  />
                  <span className="text-text-muted">-</span>
                  <input
                    type="time"
                    value={day.closeTime ?? "18:00"}
                    onChange={(e) => updateDay(index, { closeTime: e.target.value })}
                    className="rounded-win border border-border bg-surface px-2 py-1 text-xs"
                  />
                </>
              ) : (
                <span className="text-xs text-text-muted">{t("calendar.closedAllDay")}</span>
              )}
            </div>
          ))}
        </div>
        <Button size="sm" onClick={save} disabled={saving}>
          {saving ? t("common.loading") : t("common.save")}
        </Button>
      </CardContent>
    </Card>
  );
}
