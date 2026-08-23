"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import type { ChannelResponse } from "@/types";

/** No channel selected yet: redirect to the first available channel. */
export default function ChatIndexPage({ params }: { params: { workspaceId: string } }) {
  const router = useRouter();
  const [channels, setChannels] = useState<ChannelResponse[]>([]);

  useEffect(() => {
    api.get<ChannelResponse[]>(`/api/v1/workspaces/${params.workspaceId}/channels`).then((list) => {
      setChannels(list);
      if (list[0]) router.replace(`/workspace/${params.workspaceId}/chat/${list[0].id}`);
    });
  }, [params.workspaceId, router]);

  if (channels.length === 0) {
    return <div className="flex h-full items-center justify-center text-sm text-text-muted">No channels yet.</div>;
  }
  return null;
}
