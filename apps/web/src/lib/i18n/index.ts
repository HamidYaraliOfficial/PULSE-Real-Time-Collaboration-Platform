"use client";

import en from "./en";
import fa from "./fa";
import zh from "./zh";
import { useUiStore } from "@/store/ui-store";
import type { Locale } from "@/types";

export const dictionaries = { en, fa, zh };
export const rtlLocales: Locale[] = ["fa"];

type Dictionary = typeof en;

function getPath(obj: unknown, path: string): unknown {
  return path.split(".").reduce<unknown>((acc, key) => {
    if (acc && typeof acc === "object" && key in (acc as Record<string, unknown>)) {
      return (acc as Record<string, unknown>)[key];
    }
    return undefined;
  }, obj);
}

/** Usage: const t = useT(); t("chat.typeMessage", { channel: "general" }) */
export function useT() {
  const locale = useUiStore((s) => s.locale);
  const dict: Dictionary = dictionaries[locale] ?? en;

  return (key: string, vars?: Record<string, string | number>) => {
    let value = getPath(dict, key);
    if (typeof value !== "string") value = getPath(en, key);
    if (typeof value !== "string") return key;
    if (vars) {
      Object.entries(vars).forEach(([k, v]) => {
        value = (value as string).replace(`{${k}}`, String(v));
      });
    }
    return value as string;
  };
}

export function isRtl(locale: Locale): boolean {
  return rtlLocales.includes(locale);
}
