import { EventBrowseSkeleton } from "@/components/EventBrowseSkeleton";

export default function Loading() {
  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto flex w-full max-w-6xl flex-col gap-8">
        <div>
          <div className="h-4 w-36 animate-pulse rounded bg-slate-200" />
          <div className="mt-4 h-10 w-full max-w-xl animate-pulse rounded bg-slate-200" />
          <div className="mt-4 h-5 w-full max-w-2xl animate-pulse rounded bg-slate-200" />
        </div>

        <EventBrowseSkeleton />
      </section>
    </main>
  );
}
