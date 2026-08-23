"use client";

import * as RadixDialog from "@radix-ui/react-dialog";
import { X } from "lucide-react";
import { cn } from "@/lib/utils";

export const Dialog = RadixDialog.Root;
export const DialogTrigger = RadixDialog.Trigger;

export function DialogContent({ className, children, title }: { className?: string; children: React.ReactNode; title: string }) {
  return (
    <RadixDialog.Portal>
      <RadixDialog.Overlay className="fixed inset-0 z-40 bg-black/40 backdrop-blur-sm data-[state=open]:animate-in data-[state=open]:fade-in" />
      <RadixDialog.Content
        className={cn(
          "fixed left-1/2 top-1/2 z-50 w-full max-w-md -translate-x-1/2 -translate-y-1/2",
          "rounded-win-lg border border-border bg-surface p-5 shadow-acrylic-lg",
          className
        )}
      >
        <div className="mb-3 flex items-center justify-between">
          <RadixDialog.Title className="text-base font-semibold text-text">{title}</RadixDialog.Title>
          <RadixDialog.Close className="rounded-win p-1 text-text-muted hover:bg-surface-2">
            <X size={16} />
          </RadixDialog.Close>
        </div>
        {children}
      </RadixDialog.Content>
    </RadixDialog.Portal>
  );
}
