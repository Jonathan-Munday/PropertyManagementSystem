import { useState } from "react";
import { Link, Navigate, useSearchParams } from "react-router-dom";
import { AuthLayout, SessionScreen } from "../../components/auth/AuthLayout.tsx";
import { Button } from "../../components/ui/Button.tsx";
import { useAuth } from "../../context/AuthContext.tsx";
import { acceptInvitation } from "../../services/account.ts";
import { toErrorMessage } from "../../services/api.ts";

export function AcceptInvitePage() {
  const auth = useAuth();
  const [params] = useSearchParams();
  const token = params.get("token") ?? "";
  const next = `/invite?token=${encodeURIComponent(token)}`;
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [done, setDone] = useState(false);

  if (auth.loading) return <SessionScreen label="Checking your session…" />;
  if (!auth.session) return <Navigate to={`/login?next=${encodeURIComponent(next)}`} replace />;
  if (!auth.emailVerified) return <Navigate to="/verify-email" replace />;
  if (done) return <Navigate to="/" replace />;

  async function accept() {
    setError(null);
    setBusy(true);
    try {
      const membership = await acceptInvitation(token);
      if (membership) localStorage.setItem("portico.organisationId", membership.organisationId);
      await auth.refreshAccount();
      setDone(true);
    } catch (acceptError) {
      setError(toErrorMessage(acceptError));
    } finally {
      setBusy(false);
    }
  }

  return (
    <AuthLayout
      title="Join an organisation"
      lede="Accepting uses the signed-in account. The role was chosen when the invitation was sent, and this page cannot change it."
      alternate={<Link className="font-medium text-teal-deep underline decoration-teal/30 underline-offset-2 hover:text-teal" to="/">Go to the desk</Link>}
    >
      {!token ? (
        <p className="text-sm text-clay" role="alert">This invitation link does not include a token.</p>
      ) : (
        <div className="space-y-4">
          <p className="text-sm leading-6 text-ink-soft">
            Signed in as {auth.user?.email}. The invitation is accepted only when that email matches the invitation.
          </p>
          {error ? <p className="text-sm text-clay" role="alert">{error}</p> : null}
          <Button type="button" className="w-full" onClick={() => void accept()} disabled={busy}>
            {busy ? "Joining…" : "Accept invitation"}
          </Button>
        </div>
      )}
    </AuthLayout>
  );
}
