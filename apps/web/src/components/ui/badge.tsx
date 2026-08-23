import { cn } from "@/lib/utils";

type Tone = "neutral" | "accent" | "success" | "danger" | "warning";

const toneClasses: Record<Tone, string> = {
  neutral: "bg-surface-2 text-text-muted",
  accent: "bg-accent/15 text-accent",
  success: "bg-success/15 text-success",
  danger: "bg-danger/15 text-danger",
  warning: "bg-warning/15 text-warning"
};

export function Badge({ tone = "neutral", className, ...props }: React.HTMLAttributes<HTMLSpanElement> & { tone?: Tone }) {
  return (
    <span
      className={cn("inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium", toneClasses[tone], className)}
      {...props}
    />
  );
}
