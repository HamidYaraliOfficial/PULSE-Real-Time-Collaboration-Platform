"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useT } from "@/lib/i18n";
import type { TaskResponse } from "@/types";

const priorityTone = { LOW: "neutral", MEDIUM: "accent", HIGH: "warning", URGENT: "danger" } as const;

export function MyTasksWidget({ tasks }: { tasks: TaskResponse[] }) {
  const t = useT();
  return (
    <Card>
      <CardHeader>
        <CardTitle>{t("dashboard.myTasks")}</CardTitle>
      </CardHeader>
      <CardContent className="space-y-2">
        {tasks.length === 0 && <p className="text-sm text-text-muted">{t("dashboard.noTasks")}</p>}
        {tasks.slice(0, 6).map((task) => (
          <div key={task.id} className="flex items-center justify-between rounded-win px-2 py-1.5 hover:bg-surface-2">
            <span className="truncate text-sm text-text">{task.title}</span>
            <Badge tone={priorityTone[task.priority]}>{t(`kanban.${task.priority.toLowerCase()}`)}</Badge>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
