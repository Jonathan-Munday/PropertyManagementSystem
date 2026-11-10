/** Portfolio identifiers are UUID primary keys in PostgreSQL. */
export function createId(_prefix: string) {
  return crypto.randomUUID();
}
