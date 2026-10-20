import { Search } from "lucide-react";
import { controlClass } from "./Field.tsx";

export function SearchField({
  id,
  label,
  value,
  onChange,
  placeholder,
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder: string;
}) {
  return (
    <div className="relative min-w-0 flex-1 sm:max-w-sm">
      <label className="sr-only" htmlFor={id}>
        {label}
      </label>
      <Search className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-muted" size={16} aria-hidden="true" />
      <input
        id={id}
        type="search"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        className={`${controlClass} pl-9`}
      />
    </div>
  );
}
