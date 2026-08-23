"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useT } from "@/lib/i18n";
import { KanbanBoard } from "@/components/kanban/kanban-board";
import { Button } from "@/components/ui/button";
import { Plus } from "lucide-react";
import type { ProjectResponse } from "@/types";

export default function KanbanPage({ params }: { params: { workspaceId: string } }) {
  const t = useT();
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [activeProjectId, setActiveProjectId] = useState<string | null>(null);

  useEffect(() => {
    api.get<ProjectResponse[]>(`/api/v1/workspaces/${params.workspaceId}/projects`).then((list) => {
      setProjects(list);
      if (list[0]) setActiveProjectId(list[0].id);
    });
  }, [params.workspaceId]);

  async function createProject() {
    const name = window.prompt(t("kanban.newTask"));
    if (!name) return;
    const project = await api.post<ProjectResponse>(`/api/v1/workspaces/${params.workspaceId}/projects`, { name });
    setProjects((prev) => [...prev, project]);
    setActiveProjectId(project.id);
  }

  return (
    <div className="flex h-full flex-col">
      <div className="flex h-12 shrink-0 items-center gap-3 border-b border-border px-4">
        <div className="flex gap-1 overflow-x-auto">
          {projects.map((p) => (
            <button
              key={p.id}
              onClick={() => setActiveProjectId(p.id)}
              className={`shrink-0 rounded-win px-3 py-1.5 text-sm font-medium ${
                p.id === activeProjectId ? "bg-accent/15 text-accent" : "text-text-muted hover:bg-surface-2"
              }`}
            >
              {p.name}
            </button>
          ))}
        </div>
        <Button size="sm" variant="secondary" onClick={createProject} className="ms-auto">
          <Plus size={14} /> {t("common.create")}
        </Button>
      </div>
      <div className="min-h-0 flex-1">{activeProjectId && <KanbanBoard projectId={activeProjectId} />}</div>
    </div>
  );
}
