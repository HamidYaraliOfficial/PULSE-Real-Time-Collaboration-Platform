"use client";

import { useEffect, useState } from "react";
import { FileText, Plus, Star } from "lucide-react";
import { api } from "@/lib/api";
import { useT } from "@/lib/i18n";
import { Button } from "@/components/ui/button";
import { DocumentEditor } from "@/components/documents/document-editor";
import type { DocumentResponse } from "@/types";

export default function DocumentsPage({ params }: { params: { workspaceId: string } }) {
  const t = useT();
  const [docs, setDocs] = useState<DocumentResponse[]>([]);
  const [activeDoc, setActiveDoc] = useState<DocumentResponse | null>(null);

  useEffect(() => {
    api.get<DocumentResponse[]>(`/api/v1/workspaces/${params.workspaceId}/documents`).then(setDocs);
  }, [params.workspaceId]);

  async function createDocument() {
    const title = window.prompt(t("documents.newDocument")) || t("documents.untitled");
    const doc = await api.post<DocumentResponse>(`/api/v1/workspaces/${params.workspaceId}/documents`, { title });
    setDocs((prev) => [...prev, doc]);
    setActiveDoc(doc);
  }

  return (
    <div className="flex h-full">
      <div className="hidden w-64 shrink-0 flex-col border-e border-border p-3 md:flex">
        <div className="mb-2 flex items-center justify-between">
          <h3 className="text-xs font-semibold uppercase text-text-muted">{t("documents.title")}</h3>
          <button onClick={createDocument} className="rounded-win p-1 text-text-muted hover:bg-surface-2">
            <Plus size={14} />
          </button>
        </div>
        <div className="space-y-0.5 overflow-y-auto">
          {docs.map((doc) => (
            <button
              key={doc.id}
              onClick={() => setActiveDoc(doc)}
              className={`flex w-full items-center gap-1.5 rounded-win px-2 py-1.5 text-start text-sm ${
                activeDoc?.id === doc.id ? "bg-accent/15 text-accent" : "text-text-muted hover:bg-surface-2"
              }`}
            >
              <FileText size={13} />
              <span className="truncate flex-1">{doc.title || t("documents.untitled")}</span>
              {doc.isFavorite && <Star size={11} className="fill-warning text-warning" />}
            </button>
          ))}
        </div>
      </div>
      <div className="min-w-0 flex-1">
        {activeDoc ? (
          <DocumentEditor workspaceId={params.workspaceId} doc={activeDoc} />
        ) : (
          <div className="flex h-full flex-col items-center justify-center gap-3 text-text-muted">
            <FileText size={32} />
            <Button size="sm" onClick={createDocument}>
              <Plus size={14} /> {t("documents.newDocument")}
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
