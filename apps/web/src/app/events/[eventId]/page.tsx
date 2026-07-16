import Link from "next/link";
import { redirect } from "next/navigation";
import { EventDateTime } from "@/components/EventDateTime";
import { EventListingCard } from "@/components/EventListingCard";
import { EventListingPagination } from "@/components/EventListingPagination";
import { EventDetailUnavailableError, getEventDetail } from "@/lib/events";

type EventDetailPageProps = {
  params: Promise<{ eventId: string }>;
  searchParams?: Promise<{ page?: string | string[] }>;
};

export default async function EventDetailPage({ params, searchParams }: EventDetailPageProps) {
  const { eventId } = await params;
  const requestedPage = normalizePage((await searchParams)?.page);
  const result = await loadEventDetail(eventId, requestedPage);

  if (result.status === "unavailable") {
    return <EventUnavailable />;
  }

  if (result.status === "error") {
    return <EventLoadError />;
  }

  const { event, listings, pagination } = result.data;

  if (pagination.total_pages > 0 && requestedPage > pagination.total_pages) {
    redirect(
      pagination.total_pages === 1
        ? `/events/${eventId}`
        : `/events/${eventId}?page=${pagination.total_pages}`
    );
  }

  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto flex w-full max-w-6xl flex-col gap-8">
        <Link
          href="/"
          className="w-fit text-sm font-medium text-slate-700 underline decoration-slate-300 underline-offset-4 hover:decoration-slate-950"
        >
          Back to events
        </Link>

        <article className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
          {event.image_url ? (
            <img src={event.image_url} alt="" className="aspect-[3/1] w-full object-cover" />
          ) : (
            <div className="flex aspect-[3/1] items-center justify-center bg-slate-100 text-sm font-medium text-slate-500">
              TicketPass
            </div>
          )}
          <div className="p-6">
            <p className="text-sm font-medium text-slate-600">
              <EventDateTime value={event.starts_at} />
            </p>
            <h1 className="mt-2 text-3xl font-semibold text-slate-950 sm:text-4xl">
              {event.name}
            </h1>
            <p className="mt-3 text-base text-slate-600">
              {event.venue}, {event.city}
            </p>
          </div>
        </article>

        <div>
          <h2 className="text-2xl font-semibold text-slate-950">Available tickets</h2>
          <p className="mt-2 text-sm text-slate-600">
            Listings are shown as a current marketplace snapshot. Availability is confirmed later.
          </p>
        </div>

        {listings.length > 0 ? (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {listings.map((listing) => (
              <EventListingCard key={listing.id} listing={listing} />
            ))}
          </div>
        ) : (
          <div className="rounded-lg border border-slate-200 bg-white p-8 text-center shadow-sm">
            <h2 className="text-xl font-semibold text-slate-950">
              No tickets are currently available.
            </h2>
            <p className="mt-2 text-sm text-slate-600">
              Check back later as sellers add eligible listings for this event.
            </p>
          </div>
        )}

        <EventListingPagination eventId={eventId} pagination={pagination} />
      </section>
    </main>
  );
}

async function loadEventDetail(eventId: string, page: number) {
  if (!isUuid(eventId)) {
    return { status: "unavailable" as const };
  }

  try {
    return { status: "success" as const, data: await getEventDetail(eventId, page) };
  } catch (error) {
    return error instanceof EventDetailUnavailableError
      ? { status: "unavailable" as const }
      : { status: "error" as const };
  }
}

function normalizePage(value: string | string[] | undefined) {
  const rawValue = Array.isArray(value) ? value[0] : value;
  const parsedValue = Number(rawValue ?? "1");

  return Number.isInteger(parsedValue) && parsedValue >= 1 ? parsedValue : 1;
}

function isUuid(value: string) {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(value);
}

function EventUnavailable() {
  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto max-w-3xl rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <p className="text-sm font-medium uppercase tracking-wide text-slate-500">
          Event unavailable
        </p>
        <h1 className="mt-3 text-3xl font-semibold text-slate-950">
          This event cannot be viewed.
        </h1>
        <p className="mt-3 text-sm text-slate-600">
          It may no longer be upcoming or is no longer available in the marketplace.
        </p>
        <Link
          href="/"
          className="mt-6 inline-flex rounded-md bg-slate-950 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-800"
        >
          Browse events
        </Link>
      </section>
    </main>
  );
}

function EventLoadError() {
  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto max-w-3xl rounded-lg border border-red-200 bg-red-50 p-8">
        <p className="text-sm font-medium uppercase tracking-wide text-red-800">
          Events unavailable
        </p>
        <h1 className="mt-3 text-3xl font-semibold text-red-950">Could not load this event</h1>
        <p className="mt-3 text-sm text-red-800">
          Please return to the marketplace and try again.
        </p>
        <Link
          href="/"
          className="mt-6 inline-flex rounded-md bg-red-950 px-4 py-2 text-sm font-medium text-white transition hover:bg-red-900"
        >
          Browse events
        </Link>
      </section>
    </main>
  );
}
