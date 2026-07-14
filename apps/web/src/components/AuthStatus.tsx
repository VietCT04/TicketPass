"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { AuthState, getCurrentUser, logout } from "@/lib/auth";

export function AuthStatus() {
  const [authState, setAuthState] = useState<AuthState>({ status: "loading" });
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    getCurrentUser()
      .then((state) => {
        if (isMounted) {
          setAuthState(state);
        }
      })
      .catch(() => {
        if (isMounted) {
          setAuthState({ status: "error", message: "Could not load session state" });
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  async function handleLogout() {
    if (isLoggingOut) {
      return;
    }

    setIsLoggingOut(true);
    setErrorMessage(null);

    try {
      await logout();
      setAuthState({ status: "unauthenticated" });
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Could not log out");
    } finally {
      setIsLoggingOut(false);
    }
  }

  if (authState.status === "loading") {
    return (
      <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <p className="text-sm font-medium text-slate-500">Account</p>
        <p className="mt-2 text-sm text-slate-700">Checking session...</p>
      </div>
    );
  }

  if (authState.status === "authenticated") {
    return (
      <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <p className="text-sm font-medium text-slate-500">Signed in</p>
        <p className="mt-2 text-base font-semibold text-slate-950">
          {authState.user.display_name}
        </p>
        <p className="mt-1 text-sm text-slate-600">{authState.user.email}</p>
        {errorMessage ? (
          <p className="mt-3 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {errorMessage}
          </p>
        ) : null}
        <div className="mt-4 flex flex-wrap gap-3">
          <button
            type="button"
            onClick={handleLogout}
            disabled={isLoggingOut}
            className="rounded-md bg-slate-950 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
          >
            {isLoggingOut ? "Logging out..." : "Log out"}
          </button>
          <Link
            href="/sell"
            className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-800 transition hover:border-slate-400 hover:bg-slate-50"
          >
            Sell a ticket
          </Link>
        </div>
      </div>
    );
  }

  if (authState.status === "error") {
    return (
      <div className="rounded-lg border border-amber-200 bg-amber-50 p-5">
        <p className="text-sm font-medium text-amber-900">Session check failed</p>
        <p className="mt-2 text-sm text-amber-800">{authState.message}</p>
      </div>
    );
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <p className="text-sm font-medium text-slate-500">Account</p>
      <p className="mt-2 text-sm text-slate-700">Sign in to sell and manage tickets.</p>
      <div className="mt-4 flex flex-wrap gap-3">
        <Link
          href="/signup"
          className="rounded-md bg-slate-950 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-800"
        >
          Sign up
        </Link>
        <Link
          href="/login"
          className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-800 transition hover:border-slate-400 hover:bg-slate-50"
        >
          Log in
        </Link>
      </div>
    </div>
  );
}
