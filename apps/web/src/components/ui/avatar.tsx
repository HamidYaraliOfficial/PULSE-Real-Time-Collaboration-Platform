import { cn } from "@/lib/utils";
import { initials } from "@/lib/utils";
import type { PresenceStatus } from "@/types";

const presenceColor: Record<PresenceStatus, string> = {
  ONLINE: "bg-success",
  AWAY: "bg-warning",
  BUSY: "bg-danger",
  DO_NOT_DISTURB: "bg-danger",
  OFFLINE: "bg-text-muted"
};

export function Avatar({
  name,
  src,
  size = 32,
  presence,
  className
}: {
  name: string;
  src?: string | null;
  size?: number;
  presence?: PresenceStatus;
  className?: string;
}) {
  return (
    <span className={cn("relative inline-flex shrink-0", className)} style={{ width: size, height: size }}>
      {src ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img src={src} alt={name} className="h-full w-full rounded-full object-cover" />
      ) : (
        <span
          className="flex h-full w-full items-center justify-center rounded-full bg-accent text-accent-text font-semibold"
          style={{ fontSize: size * 0.4 }}
        >
          {initials(name)}
        </span>
      )}
      {presence && (
        <span
          className={cn("absolute bottom-0 end-0 rounded-full ring-2 ring-surface", presenceColor[presence])}
          style={{ width: size * 0.3, height: size * 0.3 }}
        />
      )}
    </span>
  );
}
