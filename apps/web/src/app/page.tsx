import { AuthStatus } from "@/components/AuthStatus";

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export default function Home() {
  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto flex w-full max-w-5xl flex-col gap-6">
        <div>
          <p className="text-sm font-medium uppercase tracking-wide text-slate-500">
            TicketPass MVP
          </p>
          <h1 className="mt-3 text-4xl font-semibold text-slate-950">
            Event ticketing foundation
          </h1>
          <p className="mt-4 max-w-2xl text-base leading-7 text-slate-600">
            The frontend is ready to connect to the TicketPass API and build out the
            core ticketing workflow.
          </p>
        </div>

        <AuthStatus />

        <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-slate-500">API base URL</p>
          <p className="mt-2 font-mono text-sm text-slate-900">{apiBaseUrl}</p>
        </div>
      </section>
    </main>
  );
}
