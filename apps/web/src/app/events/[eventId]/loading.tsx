import { EventDetailSkeleton } from "@/components/EventDetailSkeleton";

export default function Loading() {
  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto w-full max-w-6xl">
        <EventDetailSkeleton />
      </section>
    </main>
  );
}
