import { forwardRef } from "react";
import { cn } from "@/lib/utils";

export const Input = forwardRef<HTMLInputElement, React.InputHTMLAttributes<HTMLInputElement>>(
  ({ className, ...props }, ref) => (
    <input
      ref={ref}
      className={cn(
        "h-9 w-full rounded-win border border-border bg-surface px-3 text-sm text-text",
        "placeholder:text-text-muted focus:border-accent transition-colors",
        className
      )}
      {...props}
    />
  )
);
Input.displayName = "Input";

export const Textarea = forwardRef<HTMLTextAreaElement, React.TextareaHTMLAttributes<HTMLTextAreaElement>>(
  ({ className, ...props }, ref) => (
    <textarea
      ref={ref}
      className={cn(
        "w-full rounded-win border border-border bg-surface px-3 py-2 text-sm text-text",
        "placeholder:text-text-muted focus:border-accent transition-colors resize-none",
        className
      )}
      {...props}
    />
  )
);
Textarea.displayName = "Textarea";
