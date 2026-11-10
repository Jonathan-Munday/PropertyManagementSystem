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
  email: z.string().trim().email("Enter a valid email address"),
  password: z.string().min(1, "Enter your password"),
});

type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const next = params.get("next") || "/";
  const [formError, setFormError] = useState<string | null>(null);
  const form = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { email: "", password: "" } });

  const onSubmit = form.handleSubmit(async (values) => {
    setFormError(null);
    if (!supabase || !supabaseConfigured) {
      setFormError("Supabase Auth is not configured in this frontend.");
      return;
    }
    const { error } = await supabase.auth.signInWithPassword({
      email: values.email.trim(),
      password: values.password,
    });
    if (error) {
      setFormError(authErrorMessage(error));
      return;
    }
    navigate(next.startsWith("/") ? next : "/", { replace: true });
  });

  return (
    <AuthLayout
      title="Sign in"
      lede="Use the email and password for your Portico account."
      alternate={
        <>
          New to Portico?{" "}
          <Link className="font-medium text-teal-deep underline decoration-teal/30 underline-offset-2 hover:text-teal" to={`/register?next=${encodeURIComponent(next)}`}>
            Create an account
          </Link>
        </>
      }
    >
      <form className="space-y-4" onSubmit={onSubmit} noValidate>
        <Field label="Email" htmlFor="email" error={form.formState.errors.email?.message}>
          <input id="email" type="email" autoComplete="email" className={controlClass} {...form.register("email")} />
        </Field>
        <Field label="Password" htmlFor="password" error={form.formState.errors.password?.message}>
          <input id="password" type="password" autoComplete="current-password" className={controlClass} {...form.register("password")} />
        </Field>
        {formError ? <p className="text-sm text-clay" role="alert">{formError}</p> : null}
        <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Signing in…" : "Sign in"}
        </Button>
        <p className="text-center text-sm">
          <Link className="font-medium text-teal-deep underline decoration-teal/30 underline-offset-2 hover:text-teal" to="/forgot-password">
            Forgot your password?
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
}
