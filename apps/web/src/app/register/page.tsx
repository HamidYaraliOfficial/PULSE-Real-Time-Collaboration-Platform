"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { api, ApiError } from "@/lib/api";
import { useAuthStore } from "@/store/auth-store";
import { useT } from "@/lib/i18n";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import type { AuthResponse } from "@/types";

export default function RegisterPage() {
  const t = useT();
  const router = useRouter();
  const setSession = useAuthStore((s) => s.setSession);
  const [displayName, setDisplayName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const auth = await api.post<AuthResponse>("/api/v1/auth/register", { displayName, email, password });
      setSession(auth);
      router.replace("/dashboard");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-bg p-4">
      <Card className="w-full max-w-sm">
        <CardContent className="pt-6">
          <div className="mb-6 text-center">
            <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-win-lg bg-accent text-lg font-bold text-accent-text">
              P
            </div>
            <h1 className="text-lg font-semibold text-text">{t("auth.registerTitle")}</h1>
            <p className="text-sm text-text-muted">{t("auth.registerSubtitle")}</p>
          </div>
          <form onSubmit={submit} className="space-y-3">
            <Input placeholder={t("auth.fullName")} value={displayName} onChange={(e) => setDisplayName(e.target.value)} required />
            <Input type="email" placeholder={t("auth.email")} value={email} onChange={(e) => setEmail(e.target.value)} required />
            <Input
              type="password"
              placeholder={t("auth.password")}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              minLength={8}
              required
            />
            {error && <p className="text-xs text-danger">{error}</p>}
            <Button type="submit" className="w-full justify-center" disabled={loading}>
              {loading ? t("common.loading") : t("auth.register")}
            </Button>
          </form>
          <p className="mt-4 text-center text-sm text-text-muted">
            {t("auth.haveAccount")}{" "}
            <Link href="/login" className="font-medium text-accent hover:underline">
              {t("auth.signIn")}
            </Link>
          </p>
        </CardContent>
      </Card>
    </main>
  );
}
