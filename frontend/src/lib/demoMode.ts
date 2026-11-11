/** Sample portfolio data is a local development aid. A production build never uses it. */
export function allowsDemoFallback(production: boolean, demoMode: string | undefined) {
  return !production && demoMode !== "false";
}
