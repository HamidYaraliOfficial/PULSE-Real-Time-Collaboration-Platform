"use client";

import { useEffect, useState } from "react";
import { addDays, endOfMonth, format, startOfMonth, startOfWeek, isSameMonth, isSameDay } from "date-fns";
import { Plus, ChevronLeft, ChevronRight } from "lucide-react";
import { api } from "@/lib/api";
import { useT } from "@/lib/i18n";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { BusinessHoursEditor } from "./business-hours-editor";
import type { EventResponse } from "@/types";

const eventTone = { EVENT: "accent", MEETING: "success", DEADLINE: "danger", REMINDER: "warning" } as const;

export function CalendarView({ workspaceId }: { workspaceId: string }) {
  const t = useT();
  const [cursor, setCursor] = useState(new Date());
  const [events, setEvents] = useState<EventResponse[]>([]);

  useEffect(() => {
    const from = startOfMonth(cursor);
    const to = endOfMonth(cursor);
    api
      .get<EventResponse[]>(
        `/api/v1/workspaces/${workspaceId}/calendar/events?from=${from.toISOString()}&to=${to.toISOString()}`
      )
      .then(setEvents);
  }, [workspaceId, cursor]);

  const gridStart = startOfWeek(startOfMonth(cursor));
  const days = Array.from({ length: 42 }, (_, i) => addDays(gridStart, i));

  async function createEvent() {
    const title = window.prompt(t("calendar.newEvent"));
    if (!title) return;
    const startsAt = new Date();
    const endsAt = new Date(startsAt.getTime() + 30 * 60 * 1000);
    await api.post(`/api/v1/workspaces/${workspaceId}/calendar/events`, {
      title,
      eventType: "EVENT",
      startsAt: startsAt.toISOString(),
      endsAt: endsAt.toISOString(),
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
    });
    setCursor(new Date(cursor));
  }

  return (
    <div className="grid h-full grid-cols-1 gap-4 overflow-y-auto p-4 lg:grid-cols-[1fr_320px]">
      <div>
        <div className="mb-3 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <button onClick={() => setCursor(addDays(startOfMonth(cursor), -1))} className="rounded-win p-1.5 hover:bg-surface-2">
              <ChevronLeft size={16} />
            </button>
            <h2 className="text-sm font-semibold text-text">{format(cursor, "MMMM yyyy")}</h2>
            <button onClick={() => setCursor(addDays(endOfMonth(cursor), 1))} className="rounded-win p-1.5 hover:bg-surface-2">
              <ChevronRight size={16} />
            </button>
          </div>
          <Button size="sm" onClick={createEvent}>
            <Plus size={14} /> {t("calendar.newEvent")}
          </Button>
        </div>

        <div className="grid grid-cols-7 overflow-hidden rounded-win-lg border border-border">
          {days.map((day) => {
            const dayEvents = events.filter((e) => isSameDay(new Date(e.startsAt), day));
            return (
              <div
                key={day.toISOString()}
                className={`min-h-24 border-b border-e border-border p-1.5 text-xs ${
                  isSameMonth(day, cursor) ? "bg-surface" : "bg-surface-2/40 text-text-muted"
                }`}
              >
                <span className={isSameDay(day, new Date()) ? "font-bold text-accent" : ""}>{format(day, "d")}</span>
                <div className="mt-1 space-y-0.5">
                  {dayEvents.slice(0, 3).map((e) => (
                    <Badge key={e.id} tone={eventTone[e.eventType]} className="block w-full truncate text-start">
                      {e.title}
                    </Badge>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      <BusinessHoursEditor workspaceId={workspaceId} />
    </div>
  );
}
