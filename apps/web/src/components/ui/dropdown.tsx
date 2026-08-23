"use client";

import * as RadixDropdown from "@radix-ui/react-dropdown-menu";
import { cn } from "@/lib/utils";

export const DropdownMenu = RadixDropdown.Root;
export const DropdownMenuTrigger = RadixDropdown.Trigger;

export function DropdownMenuContent({ className, ...props }: RadixDropdown.DropdownMenuContentProps) {
  return (
    <RadixDropdown.Portal>
      <RadixDropdown.Content
        sideOffset={6}
        className={cn(
          "z-50 min-w-[180px] rounded-win-lg border border-border bg-surface p-1 shadow-acrylic-lg acrylic",
          className
        )}
        {...props}
      />
    </RadixDropdown.Portal>
  );
}

export function DropdownMenuItem({ className, ...props }: RadixDropdown.DropdownMenuItemProps) {
  return (
    <RadixDropdown.Item
      className={cn(
        "flex cursor-pointer items-center gap-2 rounded-win px-2.5 py-1.5 text-sm text-text",
        "outline-none hover:bg-surface-2 data-[highlighted]:bg-surface-2",
        className
      )}
      {...props}
    />
  );
}
