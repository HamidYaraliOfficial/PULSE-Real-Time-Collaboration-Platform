"use client";

import { Plus } from "lucide-react";
import { TaskCard } from "./task-card";
import type { TaskResponse, TaskStatus } from "@/types";

export function KanbanColumn({
  title,
  status,
  tasks,
  onDropTask,
  onAddTask,
  onOpenTask
}: {
  title: string;
  status: TaskStatus;
  tasks: TaskResponse[];
  onDropTask: (taskId: string, status: TaskStatus, position: number) => void;
  onAddTask: () => void;
  onOpenTask: (task: TaskResponse) => void;
}) {
  return (
    <div
      className="flex w-72 shrink-0 flex-col rounded-win-lg bg-surface-2/60 p-2"
      onDragOver={(e) => e.preventDefault()}
      onDrop={(e) => {
        const taskId = e.dataTransfer.getData("text/task-id");
        if (taskId) onDropTask(taskId, status, tasks.length);
      }}
    >
      <div className="mb-2 flex items-center justify-between px-1">
        <h4 className="text-xs font-semibold uppercase tracking-wide text-text-muted">
          {title} <span className="ms-1 text-text-muted/70">{tasks.length}</span>
        </h4>
        <button onClick={onAddTask} className="rounded-win p-1 text-text-muted hover:bg-surface">
          <Plus size={14} />
        </button>
      </div>
      <div className="flex-1 space-y-2 overflow-y-auto">
        {tasks.map((task, index) => (
          <div
            key={task.id}
            onDragOver={(e) => e.preventDefault()}
            onDrop={(e) => {
              e.stopPropagation();
              const taskId = e.dataTransfer.getData("text/task-id");
              if (taskId) onDropTask(taskId, status, index);
            }}
          >
            <TaskCard
              task={task}
              onClick={() => onOpenTask(task)}
              onDragStart={(e) => e.dataTransfer.setData("text/task-id", task.id)}
            />
          </div>
        ))}
      </div>
    </div>
  );
}
