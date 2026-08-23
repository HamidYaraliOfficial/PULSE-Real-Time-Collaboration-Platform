"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { DocumentEditor } from "@/components/documents/document-editor";
import type { DocumentResponse } from "@/types";

/** Deep-link entry point for a single document (e.g. shared via a link or a search result). */
export default function DocumentDeepLinkPage({ params }: { params: { workspaceId: string; docId: string } }) {
  const [doc, setDoc] = useState<DocumentResponse | null>(null);

  useEffect(() => {
    api.get<DocumentResponse>(`/api/v1/workspaces/${params.workspaceId}/documents/${params.docId}`).then(setDoc);
  }, [params.workspaceId, params.docId]);

  if (!doc) return null;
  return <DocumentEditor workspaceId={params.workspaceId} doc={doc} />;
}
