"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useAuthStore } from "@/store/auth-store";

const PUBLIC_ROUTES = ["/login", "/register"];

const AuthContext = createContext<{ ready: boolean }>({ ready: false });
export const useAuthReady = () => useContext(AuthContext);

/** Waits for zustand's persisted session to rehydrate, then guards private routes. */
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [ready, setReady] = useState(false);
  const user = useAuthStore((s) => s.user);
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    setReady(true);
  }, []);

  useEffect(() => {
    if (!ready) return;
    const isPublic = PUBLIC_ROUTES.some((r) => pathname.startsWith(r));
    if (!user && !isPublic) {
      router.replace("/login");
    }
  }, [ready, user, pathname, router]);

  return <AuthContext.Provider value={{ ready }}>{children}</AuthContext.Provider>;
}
