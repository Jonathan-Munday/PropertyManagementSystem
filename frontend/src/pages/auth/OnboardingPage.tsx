import { useState } from "react";
import { Navigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { AuthLayout, SessionScreen } from "../../components/auth/AuthLayout.tsx";
import { Button } from "../../components/ui/Button.tsx";
import { controlClass, Field } from "../../components/ui/Field.tsx";
import { useAuth } from "../../context/AuthContext.tsx";
import { createOrganisation } from "../../services/account.ts";
import { toErrorMessage } from "../../services/api.ts";

const schema = z.object({
  displayName: z.string().trim().min(1, "Enter your name").max(200, "Name is too long"),
  name: z.string().trim().min(1, "Enter the organisation name").max(200, "Name is too long"),
});

type FormValues = z.infer<typeof schema>;

export function OnboardingPage() {
  const auth = useAuth();
  const [formError, setFormError] = useState<string | null>(null);
  const metadataName = typeof auth.user?.user_metadata?.display_name === "string" ? auth.user.user_metadata.display_name : "";
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { displayName: metadataName, name: "" },
  });

  if (auth.loading || auth.accountState.status === "loading") return <SessionScreen label="Checking your session…" />;
  if (!auth.session) return <Navigate to="/login" replace />;
  if (!auth.emailVerified) return <Navigate to="/verify-email" replace />;
  if (auth.accountState.status === "ready" && auth.organisations.length > 0) return <Navigate to="/" replace />;

  const onSubmit = form.handleSubmit(async (values) => {
    setFormError(null);
    try {
      const created = await createOrganisation(values.name, values.displayName);
      if (created) localStorage.setItem("portico.organisationId", created.organisationId);
      await auth.refreshAccount();
    } catch (error) {
      setFormError(toErrorMessage(error));
    }
  });

  return (
    <AuthLayout
      title="Set up your organisation"
      lede="This creates the property business and makes you its owner. You can belong to more than one organisation later."
    >
      {auth.accountState.status === "error" ? (
        <p className="mb-4 text-sm text-clay" role="alert">{auth.accountState.message}</p>
      ) : null}
      <form className="space-y-4" onSubmit={onSubmit} noValidate>
        <Field label="Your name" htmlFor="displayName" error={form.formState.errors.displayName?.message}>
          <input id="displayName" autoComplete="name" className={controlClass} {...form.register("displayName")} />
        </Field>
        <Field label="Organisation name" htmlFor="name" error={form.formState.errors.name?.message} hint="The property management business, for example Harbour Lettings.">
          <input id="name" className={controlClass} {...form.register("name")} />
        </Field>
        {formError ? <p className="text-sm text-clay" role="alert">{formError}</p> : null}
        <Button type="submit" className="w-full" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? "Creating…" : "Create organisation"}
        </Button>
        <Button type="button" variant="ghost" className="w-full" onClick={() => void auth.signOut()}>
          Sign out
        </Button>
      </form>
    </AuthLayout>
  );
}
