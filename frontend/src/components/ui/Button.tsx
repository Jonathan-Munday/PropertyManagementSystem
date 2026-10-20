import type { ButtonHTMLAttributes } from "react";
import { clsx } from "clsx";

type Variant = "primary" | "secondary" | "ghost" | "danger";
type Size = "md" | "sm" | "icon";

const variants: Record<Variant, string> = {
  primary: "bg-ink text-paper hover:bg-ink-soft",
  secondary: "border border-line bg-card text-ink hover:bg-paper",
  ghost: "text-ink-soft hover:bg-ink/5",
  danger: "bg-clay text-white hover:bg-[#742820]",
};

const sizes: Record<Size, string> = {
  md: "h-10 px-3.5 text-sm",
  sm: "h-8 px-2.5 text-xs",
  icon: "h-10 w-10",
};

export function Button({
  variant = "primary",
  size = "md",
  className,
  type = "button",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant; size?: Size }) {
  return (
    <button
      type={type}
      className={clsx(
        "inline-flex items-center justify-center gap-2 rounded-lg font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brass disabled:cursor-not-allowed disabled:opacity-50",
        variants[variant],
        sizes[size],
        className,
      )}
      {...props}
    />
  );
}
