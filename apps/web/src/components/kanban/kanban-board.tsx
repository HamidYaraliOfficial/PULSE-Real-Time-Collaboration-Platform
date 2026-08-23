"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useT } from "@/lib/i18n";
import { useWebSocketClient } from "@/hooks/use-websocket";
import { KanbanColumn } from "./kanban-column";
import type { TaskResponse, TaskStatus } from "@/types";

const COLUMNS: TaskStatus[] = ["BACKLOG", "TODO", "IN_PROGRESS", "REVIEW", "TESTING", "DONE"];

/**
 * Live Kanban board: loads the project's tasks once, then listens on
 * /topic/kanban.{projectId} for drag-and-drop moves and edits made by any
 * team member so every open board updates instantly.
 */
export function KanbanBoard({ projectId }: { projectId: string }) {
  const t = useT();
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const ws = useWebSocketClient();

  useEffect(() => {
    api.get<TaskResponse[]>(`/api/v1/projects/${projectId}/tasks`).then(setTasks);
  }, [projectId]);

  useEffect(() => {
    if (!ws.connected) return;
    const sub = ws.subscribe(`/topic/kanban.${projectId}`, (frame) => {
      const updated: TaskResponse = JSON.parse(frame.body);
      setTasks((prev) => {
        const exists = prev.find((task) => task.id === updated.id);
        return exists ? prev.map((task) => (task.id === updated.id ? updated : task)) : [...prev, updated];
      });
    });
    return () => sub?.unsubscribe();
  }, [ws.connected, projectId]);

  async function moveTask(taskId: string, status: TaskStatus, position: number) {
    // Optimistic local update, then confirm with the server (which also broadcasts to everyone else).
    setTasks((prev) => prev.map((task) => (task.id === taskId ? { ...task, status, position } : task)));
    await api.post(`/api/v1/tasks/${taskId}/move`, { status, position });
  }

  async function addTask(status: TaskStatus) {
    const title = window.prompt(t("kanban.newTask"));
    if (!title) return;
    await api.post(`/api/v1/projects/${projectId}/tasks`, { title, status });
  }

  return (
    <div className="flex h-full gap-3 overflow-x-auto p-4">
      {COLUMNS.map((status) => (
        <KanbanColumn
          key={status}
          status={status}
          title={t(`kanban.${status === "IN_PROGRESS" ? "inProgress" : status.toLowerCase()}`)}
          tasks={tasks.filter((task) => task.status === status).sort((a, b) => a.position - b.position)}
          onDropTask={moveTask}
          onAddTask={() => addTask(status)}
          onOpenTask={() => {}}
        />
      ))}
    </div>
  );
}
