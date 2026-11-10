import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react";
import type { Session, User } from "@supabase/supabase-js";
import { supabase, supabaseConfigured } from "../lib/supabase.ts";
import { bindApiAuth } from "../services/api.ts";
import { fetchAccount, type Account, type OrganisationMembership } from "../services/account.ts";
import { toErrorMessage } from "../services/api.ts";

const ORGANISATION_KEY = "portico.organisationId";

type AccountState =
  | { status: "idle" }
  | { status: "loading" }
  | { status: "ready"; account: Account }
  | { status: "error"; message: string };

interface AuthContextValue {
  configured: boolean;
  loading: boolean;
  session: Session | null;
  user: User | null;
  emailVerified: boolean;
  accountState: AccountState;
  organisations: OrganisationMembership[];
  selectedOrganisationId: string | null;
  selectedOrganisation: OrganisationMembership | null;
  selectOrganisation: (organisationId: string) => void;
  refreshAccount: () => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function chooseOrganisation(organisations: OrganisationMembership[]) {
  const stored = localStorage.getItem(ORGANISATION_KEY);
  if (stored && organisations.some((item) => item.organisationId === stored)) return stored;
  return organisations[0]?.organisationId ?? null;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [loading, setLoading] = useState(supabaseConfigured);
  const [session, setSession] = useState<Session | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [accountState, setAccountState] = useState<AccountState>({ status: "idle" });
  const [selectedOrganisationId, setSelectedOrganisationId] = useState<string | null>(null);
  const selectedRef = useRef<string | null>(null);
  selectedRef.current = selectedOrganisationId;

  bindApiAuth({
    token: async () => {
      if (!supabase) return null;
      const { data } = await supabase.auth.getSession();
      return data.session?.access_token ?? null;
    },
    organisationId: () => selectedRef.current,
  });

  const loadAccount = useCallback(async (activeSession: Session | null) => {
    if (!supabase || !activeSession) {
      setUser(null);
      setAccountState({ status: "idle" });
      setSelectedOrganisationId(null);
      setLoading(false);
      return;
    }
    const { data, error } = await supabase.auth.getUser();
    if (error || !data.user) {
      setUser(null);
      setAccountState({ status: "idle" });
      setLoading(false);
      return;
    }
    setUser(data.user);
    if (!data.user.email_confirmed_at) {
      setAccountState({ status: "idle" });
      setLoading(false);
      return;
    }
    setAccountState({ status: "loading" });
    try {
      const account = await fetchAccount();
      const organisationId = chooseOrganisation(account.organisations);
      setSelectedOrganisationId(organisationId);
      setAccountState({ status: "ready", account });
    } catch (accountError) {
      setAccountState({ status: "error", message: toErrorMessage(accountError) });
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!supabase) return;
    const client = supabase;
    let ignore = false;
    const apply = (next: Session | null) => {
      window.setTimeout(() => {
        if (ignore) return;
        setSession(next);
        void loadAccount(next);
      }, 0);
    };
    void client.auth.getSession().then(({ data }) => {
      if (!ignore) apply(data.session);
    });
    const { data } = client.auth.onAuthStateChange((_event, next) => {
      if (!ignore) apply(next);
    });
    return () => {
      ignore = true;
      data.subscription.unsubscribe();
    };
  }, [loadAccount]);

  const refreshAccount = useCallback(async () => {
    if (!supabase) return;
    const { data } = await supabase.auth.getSession();
    setSession(data.session);
    await loadAccount(data.session);
  }, [loadAccount]);

  const selectOrganisation = useCallback((organisationId: string) => {
    const organisations = accountState.status === "ready" ? accountState.account.organisations : [];
    if (!organisations.some((item) => item.organisationId === organisationId)) return;
    localStorage.setItem(ORGANISATION_KEY, organisationId);
    setSelectedOrganisationId(organisationId);
  }, [accountState]);

  const signOut = useCallback(async () => {
    localStorage.removeItem(ORGANISATION_KEY);
    setSelectedOrganisationId(null);
    setAccountState({ status: "idle" });
    setUser(null);
    setSession(null);
    if (supabase) await supabase.auth.signOut();
  }, []);

  const organisations = accountState.status === "ready" ? accountState.account.organisations : [];
  const selectedOrganisation = organisations.find((item) => item.organisationId === selectedOrganisationId) ?? null;

  const value = useMemo<AuthContextValue>(
    () => ({
      configured: supabaseConfigured,
      loading,
      session,
      user,
      emailVerified: Boolean(user?.email_confirmed_at),
      accountState,
      organisations,
      selectedOrganisationId,
      selectedOrganisation,
      selectOrganisation,
      refreshAccount,
      signOut,
    }),
    [
      loading,
      session,
      user,
      accountState,
      organisations,
      selectedOrganisationId,
      selectedOrganisation,
      selectOrganisation,
      refreshAccount,
      signOut,
    ],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("useAuth must be used within AuthProvider");
  return value;
}
