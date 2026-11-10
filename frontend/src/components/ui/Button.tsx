import type { ButtonHTMLAttributes } from "react";
import { clsx } from "clsx";

type Variant = "primary" | "secondary" | "ghost" | "danger";
type Size = "md" | "sm" | "icon";

const variants: Record<Variant, string> = {
  primary: "bg-teal text-white hover:bg-teal-deep disabled:hover:bg-teal",
  secondary: "border border-line bg-card text-ink hover:border-teal/40 hover:bg-ivory",
  ghost: "text-ink-soft hover:bg-navy/5 hover:text-ink",
  danger: "bg-clay text-white hover:bg-clay-deep disabled:hover:bg-clay",
};

const sizes: Record<Size, string> = {
  md: "h-10 px-3.5 text-sm",
  sm: "h-8 px-2.5 text-xs",
  icon: "h-10 w-10",
};

export function buttonClass({
  variant = "primary",
  size = "md",
  className,
}: {
  variant?: Variant;
  size?: Size;
  className?: string;
}) {
  return clsx(
    "inline-flex items-center justify-center gap-2 rounded-lg font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal focus-visible:ring-offset-2 focus-visible:ring-offset-paper disabled:cursor-not-allowed disabled:opacity-50",
    variants[variant],
    sizes[size],
    className,
  );
}

export function Button({
  variant = "primary",
  size = "md",
  className,
  type = "button",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant; size?: Size }) {
  return <button type={type} className={buttonClass({ variant, size, className })} {...props} />;
}
