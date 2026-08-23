"use client";

import { Badge } from "@/components/ui/badge";
import { Avatar } from "@/components/ui/avatar";
import { useT } from "@/lib/i18n";
import type { TaskResponse } from "@/types";

const priorityTone = {
  LOW: "neutral",
  MEDIUM: "accent",
  HIGH: "warning",
  URGENT: "danger"
} as const;

export function TaskCard({
  task,
  onDragStart,
  onClick
}: {
  task: TaskResponse;
  onDragStart: (e: React.DragEvent) => void;
  onClick: () => void;
}) {
  const t = useT();
  return (
    <div
      draggable
      onDragStart={onDragStart}
      onClick={onClick}
      className="cursor-grab space-y-2 rounded-win border border-border bg-surface p-3 shadow-sm hover:border-accent active:cursor-grabbing"
    >
      <p className="text-sm font-medium text-text">{task.title}</p>
      {task.labels.length > 0 && (
        <div className="flex flex-wrap gap-1">
          {task.labels.map((l) => (
            <Badge key={l} tone="neutral">{l}</Badge>
          ))}
        </div>
      )}
      <div className="flex items-center justify-between">
        <Badge tone={priorityTone[task.priority]}>{t(`kanban.${task.priority.toLowerCase()}`)}</Badge>
        {task.assigneeName ? (
          <Avatar name={task.assigneeName} size={22} />
        ) : (
          <span className="text-xs text-text-muted">{t("kanban.unassigned")}</span>
        )}
      </div>
    </div>
  );
}
