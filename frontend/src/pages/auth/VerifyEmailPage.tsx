import { useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { AuthLayout, SessionScreen, authErrorMessage } from "../../components/auth/AuthLayout.tsx";
import { Button } from "../../components/ui/Button.tsx";
import { useAuth } from "../../context/AuthContext.tsx";
import { supabase } from "../../lib/supabase.ts";

export function VerifyEmailPage() {
  const auth = useAuth();
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  if (auth.loading) return <SessionScreen label="Checking your session…" />;
  if (!auth.session) return <Navigate to="/login" replace />;
  if (auth.emailVerified) return <Navigate to="/" replace />;

  const email = auth.user?.email ?? "your email address";

  async function resend() {
    setError(null);
    setMessage(null);
    if (!supabase || !auth.user?.email) {
      setError("Supabase Auth is not configured in this frontend.");
      return;
    }
    setBusy(true);
    const { error: resendError } = await supabase.auth.resend({ type: "signup", email: auth.user.email });
    setBusy(false);
    if (resendError) {
      setError(authErrorMessage(resendError));
      return;
    }
    setMessage("A new confirmation email is on its way.");
  }

  return (
    <AuthLayout
      title="Confirm your email"
      lede={`Open the confirmation link sent to ${email}. The desk stays closed until that address is confirmed.`}
      alternate={<Link className="font-medium text-teal-deep underline decoration-teal/30 underline-offset-2 hover:text-teal" to="/login">Use a different account</Link>}
    >
      <div className="space-y-4">
        {message ? <p className="text-sm text-moss" role="status">{message}</p> : null}
        {error ? <p className="text-sm text-clay" role="alert">{error}</p> : null}
        <Button type="button" className="w-full" onClick={() => void resend()} disabled={busy}>
          {busy ? "Sending…" : "Resend confirmation"}
        </Button>
        <Button type="button" variant="secondary" className="w-full" onClick={() => void auth.refreshAccount()}>
          I have confirmed it
        </Button>
        <Button type="button" variant="ghost" className="w-full" onClick={() => void auth.signOut()}>
          Sign out
        </Button>
      </div>
    </AuthLayout>
  );
}
