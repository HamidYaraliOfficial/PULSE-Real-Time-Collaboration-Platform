"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { AuthResponse, UserSummary } from "@/types";

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

interface AuthState {
  user: UserSummary | null;
  accessToken: string | null;
  refreshToken: string | null;
  setSession: (auth: AuthResponse) => void;
  clearSession: () => void;
  refresh: () => Promise<boolean>;
}

/**
 * Session storage for the demo/scaffold uses localStorage via zustand's
 * persist middleware, which is the simplest thing that works for a
 * client-rendered SPA. For a hardened production deployment, swap this for
 * the Secure, HttpOnly refresh-token cookie flow the backend already
 * supports (issue the access token to JS, keep the refresh token in an
 * HttpOnly cookie set by a small Next.js route handler) - no backend
 * changes are required to make that switch.
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,

      setSession: (auth) =>
        set({ user: auth.user, accessToken: auth.accessToken, refreshToken: auth.refreshToken }),

      clearSession: () => set({ user: null, accessToken: null, refreshToken: null }),

      refresh: async () => {
        const { refreshToken } = get();
        if (!refreshToken) return false;
        try {
          const res = await fetch(`${API_BASE}/api/v1/auth/refresh`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ refreshToken })
          });
          if (!res.ok) {
            get().clearSession();
            return false;
          }
          const auth: AuthResponse = await res.json();
          set({ user: auth.user, accessToken: auth.accessToken, refreshToken: auth.refreshToken });
          return true;
        } catch {
          return false;
        }
      }
    }),
    { name: "pulse-auth" }
  )
);
