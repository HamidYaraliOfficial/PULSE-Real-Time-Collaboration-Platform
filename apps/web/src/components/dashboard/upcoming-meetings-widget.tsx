"use client";

import { format } from "date-fns";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useT } from "@/lib/i18n";
import type { EventResponse } from "@/types";

export function UpcomingMeetingsWidget({ events }: { events: EventResponse[] }) {
  const t = useT();
  return (
    <Card>
      <CardHeader>
        <CardTitle>{t("dashboard.upcomingMeetings")}</CardTitle>
      </CardHeader>
      <CardContent className="space-y-2">
        {events.length === 0 && <p className="text-sm text-text-muted">{t("dashboard.noMeetings")}</p>}
        {events.slice(0, 5).map((event) => (
          <div key={event.id} className="rounded-win px-2 py-1.5 hover:bg-surface-2">
            <p className="truncate text-sm font-medium text-text">{event.title}</p>
            <p className="text-xs text-text-muted">{format(new Date(event.startsAt), "MMM d, HH:mm")}</p>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
