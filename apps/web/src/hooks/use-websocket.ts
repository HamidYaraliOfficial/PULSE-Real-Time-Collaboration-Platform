"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import { Client, type IMessage, type StompSubscription } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useAuthStore } from "@/store/auth-store";

const WS_URL = process.env.NEXT_PUBLIC_WS_URL ?? "http://localhost:8080/ws";

/**
 * Owns a single STOMP-over-SockJS connection for the whole app (chat,
 * presence, kanban board updates, and live document collaboration all
 * share this connection, each on their own /topic/... destination).
 * Reconnects automatically; queues subscriptions requested before the
 * socket is actually connected.
 */
export function useWebSocketClient() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const clientRef = useRef<Client | null>(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    if (!accessToken) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL) as unknown as WebSocket,
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => setConnected(true),
      onDisconnect: () => setConnected(false),
      onStompError: () => setConnected(false)
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
      setConnected(false);
    };
  }, [accessToken]);

  const subscribe = useCallback(
    (destination: string, callback: (msg: IMessage) => void): StompSubscription | undefined => {
      return clientRef.current?.subscribe(destination, callback);
    },
    []
  );

  const publish = useCallback((destination: string, body: unknown) => {
    clientRef.current?.publish({ destination, body: JSON.stringify(body) });
  }, []);

  return { connected, subscribe, publish };
}
