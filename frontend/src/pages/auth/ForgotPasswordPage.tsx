import { useState } from "react";
import { Link } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { AuthLayout, authErrorMessage } from "../../components/auth/AuthLayout.tsx";
import { Button } from "../../components/ui/Button.tsx";
import { controlClass, Field } from "../../components/ui/Field.tsx";
import { supabase, supabaseConfigured } from "../../lib/supabase.ts";

const schema = z.object({
  email: z.string().trim().email("Enter a valid email address"),
});

type FormValues = z.infer<typeof schema>;

export function ForgotPasswordPage() {
  const [formError, setFormError] = useState<string | null>(null);
  const [sent, setSent] = useState(false);
  const form = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { email: "" } });

  const onSubmit = form.handleSubmit(async (values) => {
    setFormError(null);
    if (!supabase || !supabaseConfigured) {
      setFormError("Supabase Auth is not configured in this frontend.");
      return;
    }
    const { error } = await supabase.auth.resetPasswordForEmail(values.email.trim(), {
      redirectTo: `${window.location.origin}/reset-password`,
    });
    if (error) {
      const message = error.message.toLowerCase();
      if (message.includes("rate") || message.includes("config") || message.includes("redirect")) {
        setFormError(authErrorMessage(error));
        return;
      }
    }
    setSent(true);
  });

  return (
    <AuthLayout
      title="Reset your password"
      lede="We will email a recovery link if that address has a Portico account."
      alternate={<Link className="font-medium text-teal-deep underline decoration-teal/30 underline-offset-2 hover:text-teal" to="/login">Back to sign in</Link>}
    >
      {sent ? (
        <p className="text-sm leading-6 text-ink-soft" role="status">
          If an account exists for that email, a reset link is on its way. Open it on this browser to choose a new password.
        </p>
      ) : (
        <form className="space-y-4" onSubmit={onSubmit} noValidate>
          <Field label="Email" htmlFor="email" error={form.formState.errors.email?.message}>
            <input id="email" type="email" autoComplete="email" className={controlClass} {...form.register("email")} />
          </Field>
          {formError ? <p className="text-sm text-clay" role="alert">{formError}</p> : null}
          <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
            {form.formState.isSubmitting ? "Sending…" : "Send reset link"}
          </Button>
        </form>
      )}
    </AuthLayout>
  );
}
