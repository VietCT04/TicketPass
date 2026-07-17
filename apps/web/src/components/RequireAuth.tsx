"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { ReactNode, useEffect, useState } from "react";
import { AuthState, getCurrentUser } from "@/lib/auth";
import { buildAuthHref } from "@/lib/redirects";

type RequireAuthProps = {
  children: ReactNode;
  returnTo: string;
};

export function RequireAuth({ children, returnTo }: RequireAuthProps) {
  const router = useRouter();
  const [authState, setAuthState] = useState<AuthState>({ status: "loading" });
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    let isActive = true;

    async function checkSession() {
      setAuthState({ status: "loading" });

      try {
        const currentAuthState = await getCurrentUser();

        if (!isActive) {
          return;
        }

        if (currentAuthState.status === "unauthenticated") {
          router.replace(buildAuthHref("/login", returnTo));
          return;
        }

        if (currentAuthState.status === "error") {
          setAuthState({ status: "error", message: "Could not verify your session" });
          return;
        }

        setAuthState(currentAuthState);
      } catch {
        if (isActive) {
          setAuthState({ status: "error", message: "Could not verify your session" });
        }
      }
    }

    void checkSession();

    return () => {
      isActive = false;
    };
  }, [returnTo, retryKey, router]);

  if (authState.status === "authenticated") {
    return <>{children}</>;
  }

  if (authState.status === "error") {
    return (
      <main className="min-h-screen px-6 py-10">
        <section className="mx-auto flex w-full max-w-md flex-col gap-4">
          <div>
            <p className="text-sm font-medium uppercase text-slate-500">TicketPass</p>
            <h1 className="mt-3 text-3xl font-semibold text-slate-950">
              Could not verify your session
            </h1>
          </div>

          <div className="rounded-lg border border-red-200 bg-red-50 p-5 text-sm text-red-700">
            <p>{authState.message}</p>
          </div>

          <div className="flex flex-wrap gap-3">
            <button
              type="button"
              onClick={() => setRetryKey((current) => current + 1)}
              className="rounded-md bg-slate-950 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-slate-800"
            >
              Retry
            </button>
            <Link
              href="/"
              className="rounded-md border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:border-slate-900 hover:text-slate-950"
            >
              Back to home
            </Link>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto w-full max-w-md">
        <p className="text-sm font-medium uppercase text-slate-500">TicketPass</p>
        <h1 className="mt-3 text-3xl font-semibold text-slate-950">Checking session...</h1>
      </section>
    </main>
  );
}
