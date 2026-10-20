export function createId(prefix: string) {
  const token = crypto.randomUUID().replaceAll("-", "").slice(0, 8).toUpperCase();
  return `${prefix}-${token}`;
}
