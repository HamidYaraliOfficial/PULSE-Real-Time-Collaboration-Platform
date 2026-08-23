"use client";

import { useEffect } from "react";
import type { useWebSocketClient } from "./use-websocket";

/** Sends a presence heartbeat every 30s so the backend keeps the user marked ONLINE in Redis. */
export function usePresenceHeartbeat(ws: ReturnType<typeof useWebSocketClient>) {
  useEffect(() => {
    if (!ws.connected) return;
    ws.publish("/app/presence/heartbeat", {});
    const interval = setInterval(() => ws.publish("/app/presence/heartbeat", {}), 30000);
    return () => clearInterval(interval);
  }, [ws.connected, ws]);
}
