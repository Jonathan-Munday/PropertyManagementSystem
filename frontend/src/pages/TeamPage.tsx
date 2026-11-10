import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { PageIntro } from "../components/layout/PageIntro.tsx";
import { Button } from "../components/ui/Button.tsx";
import { controlClass, Field } from "../components/ui/Field.tsx";
import { useAuth } from "../context/AuthContext.tsx";
import { grantableRoles, roleLabel } from "../lib/permissions.ts";
import { createInvitation } from "../services/account.ts";
import { toErrorMessage } from "../services/api.ts";

const schema = z.object({
  email: z.string().trim().email("Enter a valid email address"),
  role: z.string().min(1, "Choose a role"),
});

type FormValues = z.infer<typeof schema>;

export function TeamPage() {
  const auth = useAuth();
  const role = auth.selectedOrganisation?.role;
  const roles = grantableRoles(role);
  const [link, setLink] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: "", role: roles[0] ?? "VIEWER" },
  });

  const onSubmit = form.handleSubmit(async (values) => {
    if (!auth.selectedOrganisationId) return;
    setFormError(null);
    setLink(null);
    try {
      const invitation = await createInvitation(auth.selectedOrganisationId, values.email, values.role);
      setLink(`${window.location.origin}/invite?token=${encodeURIComponent(invitation.token)}`);
      form.reset({ email: "", role: values.role });
    } catch (error) {
      setFormError(toErrorMessage(error));
    }
  });

  return (
    <div className="mx-auto max-w-2xl">
      <PageIntro
        description={`${roleLabel(role) || "Members"} can belong to more than one organisation. Invitations are sent from here, and the signed-in person cannot raise their own role.`}
      />
      {roles.length === 0 ? (
        <p className="panel mt-6 px-4 py-3 text-sm leading-6 text-ink-soft">
          Your role can view the desk. An owner or admin invites new members.
        </p>
      ) : (
        <form className="panel mt-6 space-y-4 p-5" onSubmit={onSubmit} noValidate>
          <Field label="Email" htmlFor="invite-email" error={form.formState.errors.email?.message}>
            <input id="invite-email" type="email" className={controlClass} {...form.register("email")} />
          </Field>
          <Field label="Role" htmlFor="invite-role" error={form.formState.errors.role?.message}>
            <select id="invite-role" className={controlClass} {...form.register("role")}>
              {roles.map((item) => (
                <option key={item} value={item}>
                  {roleLabel(item)}
                </option>
              ))}
            </select>
          </Field>
          {formError ? <p className="text-sm text-clay" role="alert">{formError}</p> : null}
          {link ? (
            <p className="break-all rounded-lg bg-paper px-3 py-2 text-sm text-ink" role="status">
              Share this link with the invited person: {link}
            </p>
          ) : null}
          <Button type="submit" disabled={form.formState.isSubmitting}>
            {form.formState.isSubmitting ? "Creating…" : "Create invitation"}
          </Button>
        </form>
      )}
    </div>
  );
}
