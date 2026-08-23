"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { WorkspaceResponse } from "@/types";

interface WorkspaceState {
  workspaces: WorkspaceResponse[];
  activeWorkspaceId: string | null;
  setWorkspaces: (list: WorkspaceResponse[]) => void;
  setActiveWorkspace: (id: string) => void;
}

export const useWorkspaceStore = create<WorkspaceState>()(
  persist(
    (set) => ({
      workspaces: [],
      activeWorkspaceId: null,
      setWorkspaces: (list) =>
        set((state) => ({
          workspaces: list,
          activeWorkspaceId: state.activeWorkspaceId ?? list[0]?.id ?? null
        })),
      setActiveWorkspace: (id) => set({ activeWorkspaceId: id })
    }),
    { name: "pulse-workspace" }
  )
);
