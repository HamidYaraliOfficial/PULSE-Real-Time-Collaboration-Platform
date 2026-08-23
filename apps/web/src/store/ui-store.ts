"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { ThemeName, Locale } from "@/types";

interface UiState {
  theme: ThemeName;
  locale: Locale;
  density: "comfortable" | "compact";
  sidebarCollapsed: boolean;
  setTheme: (t: ThemeName) => void;
  setLocale: (l: Locale) => void;
  setDensity: (d: "comfortable" | "compact") => void;
  toggleSidebar: () => void;
}

export const useUiStore = create<UiState>()(
  persist(
    (set) => ({
      theme: "win11-light",
      locale: "fa",
      density: "comfortable",
      sidebarCollapsed: false,
      setTheme: (theme) => set({ theme }),
      setLocale: (locale) => set({ locale }),
      setDensity: (density) => set({ density }),
      toggleSidebar: () => set((s) => ({ sidebarCollapsed: !s.sidebarCollapsed }))
    }),
    { name: "pulse-ui" }
  )
);
