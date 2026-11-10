import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { AuthLayout, authErrorMessage } from "../../components/auth/AuthLayout.tsx";
import { Button } from "../../components/ui/Button.tsx";
import { controlClass, Field } from "../../components/ui/Field.tsx";
import { supabase, supabaseConfigured } from "../../lib/supabase.ts";

const schema = z.object({
  displayName: z.string().trim().min(1, "Enter your name").max(200, "Name is too long"),
  email: z.string().trim().email("Enter a valid email address"),
  password: z.string().min(8, "Use at least 8 characters").max(72, "Password is too long"),
});

type FormValues = z.infer<typeof schema>;

export function RegisterPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const next = params.get("next") || "/";
  const [formError, setFormError] = useState<string | null>(null);
  const [pendingEmail, setPendingEmail] = useState<string | null>(null);
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { displayName: "", email: "", password: "" },
  });

  const onSubmit = form.handleSubmit(async (values) => {
    setFormError(null);
    if (!supabase || !supabaseConfigured) {
      setFormError("Supabase Auth is not configured in this frontend.");
      return;
    }
    const { data, error } = await supabase.auth.signUp({
      email: values.email.trim(),
      password: values.password,
      options: {
        emailRedirectTo: `${window.location.origin}/login`,
        data: { display_name: values.displayName.trim() },
      },
    });
    if (error) {
      setFormError(authErrorMessage(error));
      return;
    }
    if (!data.session) {
      setPendingEmail(values.email.trim());
      return;
    }
    navigate(next.startsWith("/") ? next : "/", { replace: true });
  });

  if (pendingEmail) {
    return (
      <AuthLayout
        title="Confirm your email"
        lede={`We sent a confirmation link to ${pendingEmail}. Open it, then sign in.`}
        alternate={<Link className="font-medium text-teal-deep underline decoration-teal/30 underline-offset-2 hover:text-teal" to="/login">Back to sign in</Link>}
      >
        <p className="text-sm leading-6 text-ink-soft">
          The link expires. If it does, sign in and ask for a new one from the verification screen.
        </p>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout
      title="Create an account"
      lede="Create your account, confirm your email, then name the organisation you manage."
      alternate={
        <>
          Already registered?{" "}
          <Link className="font-medium text-teal-deep underline decoration-teal/30 underline-offset-2 hover:text-teal" to={`/login?next=${encodeURIComponent(next)}`}>
            Sign in
          </Link>
        </>
      }
    >
      <form className="space-y-4" onSubmit={onSubmit} noValidate>
        <Field label="Your name" htmlFor="displayName" error={form.formState.errors.displayName?.message}>
          <input id="displayName" autoComplete="name" className={controlClass} {...form.register("displayName")} />
        </Field>
        <Field label="Email" htmlFor="email" error={form.formState.errors.email?.message}>
          <input id="email" type="email" autoComplete="email" className={controlClass} {...form.register("email")} />
        </Field>
        <Field label="Password" htmlFor="password" error={form.formState.errors.password?.message} hint="At least 8 characters. Supabase stores this, not Portico.">
          <input id="password" type="password" autoComplete="new-password" className={controlClass} {...form.register("password")} />
        </Field>
        {formError ? <p className="text-sm text-clay" role="alert">{formError}</p> : null}
        <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Creating account…" : "Create account"}
        </Button>
      </form>
    </AuthLayout>
  );
}
