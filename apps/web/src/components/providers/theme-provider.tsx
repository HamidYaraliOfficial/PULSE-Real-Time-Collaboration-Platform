"use client";

import { useEffect } from "react";
import { useUiStore } from "@/store/ui-store";

/**
 * Applies the selected theme as a data-attribute on <html>, which every
 * theme's CSS custom properties in globals.css key off of. Done
 * imperatively in an effect (not via SSR-rendered className) so there is
 * no server/client markup mismatch while zustand's persisted value
 * rehydrates from localStorage.
 */
export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const theme = useUiStore((s) => s.theme);
  const density = useUiStore((s) => s.density);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
  }, [theme]);

  useEffect(() => {
    document.documentElement.dataset.density = density;
  }, [density]);

  return <>{children}</>;
}
