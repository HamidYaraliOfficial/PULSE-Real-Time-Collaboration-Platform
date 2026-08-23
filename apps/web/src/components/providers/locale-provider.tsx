"use client";

import { useEffect } from "react";
import { useUiStore } from "@/store/ui-store";
import { isRtl } from "@/lib/i18n";

/** Keeps <html lang> and <html dir> in sync with the chosen locale (fa = RTL, en/zh = LTR). */
export function LocaleProvider({ children }: { children: React.ReactNode }) {
  const locale = useUiStore((s) => s.locale);

  useEffect(() => {
    document.documentElement.lang = locale;
    document.documentElement.dir = isRtl(locale) ? "rtl" : "ltr";
  }, [locale]);

  return <>{children}</>;
}
