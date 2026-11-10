import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { AuthLayout, authErrorMessage } from "../../components/auth/AuthLayout.tsx";
import { Button } from "../../components/ui/Button.tsx";
import { controlClass, Field } from "../../components/ui/Field.tsx";
import { supabase, supabaseConfigured } from "../../lib/supabase.ts";

const schema = z
  .object({
    password: z.string().min(8, "Use at least 8 characters").max(72, "Password is too long"),
    confirm: z.string().min(1, "Confirm the new password"),
  })
  .refine((values) => values.password === values.confirm, {
    path: ["confirm"],
    message: "Passwords do not match",
  });

type FormValues = z.infer<typeof schema>;

export function ResetPasswordPage() {
  const navigate = useNavigate();
  const [ready, setReady] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const form = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { password: "", confirm: "" } });

  useEffect(() => {
    if (!supabase) return;
    const client = supabase;
    void client.auth.getSession().then(({ data }) => {
      if (data.session) setReady(true);
    });
    const { data } = client.auth.onAuthStateChange((event, session) => {
      if ((event === "PASSWORD_RECOVERY" || event === "SIGNED_IN") && session) setReady(true);
    });
    return () => data.subscription.unsubscribe();
  }, []);

  const onSubmit = form.handleSubmit(async (values) => {
    setFormError(null);
    if (!supabase || !supabaseConfigured) {
      setFormError("Supabase Auth is not configured in this frontend.");
      return;
    }
    const { error } = await supabase.auth.updateUser({ password: values.password });
    if (error) {
      setFormError(authErrorMessage(error));
      return;
    }
    navigate("/", { replace: true });
  });

  return (
    <AuthLayout
      title="Choose a new password"
      lede="Choose a new password. Other sessions are signed out when it changes."
      alternate={<Link className="font-medium text-teal-deep underline decoration-teal/30 underline-offset-2 hover:text-teal" to="/forgot-password">Request a new link</Link>}
    >
      {supabaseConfigured && !ready ? (
        <p className="text-sm leading-6 text-ink-soft">
          Open the recovery link from your email in this browser. The page will show the password form when that session is ready.
        </p>
      ) : (
        <form className="space-y-4" onSubmit={onSubmit} noValidate>
          <Field label="New password" htmlFor="password" error={form.formState.errors.password?.message}>
            <input id="password" type="password" autoComplete="new-password" className={controlClass} {...form.register("password")} />
          </Field>
          <Field label="Confirm password" htmlFor="confirm" error={form.formState.errors.confirm?.message}>
            <input id="confirm" type="password" autoComplete="new-password" className={controlClass} {...form.register("confirm")} />
          </Field>
          {formError ? <p className="text-sm text-clay" role="alert">{formError}</p> : null}
          <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
            {form.formState.isSubmitting ? "Saving…" : "Update password"}
          </Button>
        </form>
      )}
    </AuthLayout>
  );
}
