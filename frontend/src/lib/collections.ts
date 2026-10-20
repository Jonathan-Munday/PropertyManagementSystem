export function upsert<T>(items: T[], item: T, idOf: (item: T) => string) {
  const id = idOf(item);
  const index = items.findIndex((entry) => idOf(entry) === id);
  if (index === -1) return [item, ...items];
  return items.map((entry, entryIndex) => (entryIndex === index ? item : entry));
}

export function removeById<T>(items: T[], id: string, idOf: (item: T) => string) {
  return items.filter((item) => idOf(item) !== id);
}
