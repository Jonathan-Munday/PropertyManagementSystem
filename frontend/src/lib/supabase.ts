import { createClient, type SupabaseClient } from "@supabase/supabase-js";

const url = import.meta.env.VITE_SUPABASE_URL?.trim() ?? "";
const publishableKey = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY?.trim() ?? "";

function configurationError() {
  if (!url) return "VITE_SUPABASE_URL is missing.";
  try {
    const parsed = new URL(url);
    if (parsed.protocol !== "https:" && parsed.protocol !== "http:") {
      return "VITE_SUPABASE_URL must be an http(s) URL.";
    }
  } catch {
    return "VITE_SUPABASE_URL is not a valid URL.";
  }
  if (!publishableKey) return "VITE_SUPABASE_PUBLISHABLE_KEY is missing.";
  return null;
}

export const supabaseConfigError = configurationError();
export const supabaseConfigured = supabaseConfigError === null;

/**
 * The one Supabase browser client. Passwords, email confirmation, recovery,
 * session persistence, and token refresh stay in Supabase Auth.
 * The publishable key is the public client key, never the service-role key.
 */
export const supabase: SupabaseClient | null = supabaseConfigured
  ? createClient(url, publishableKey, {
      auth: {
        persistSession: true,
        autoRefreshToken: true,
        detectSessionInUrl: true,
        flowType: "pkce",
      },
    })
  : null;
