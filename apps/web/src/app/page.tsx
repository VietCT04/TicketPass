import Link from "next/link";
import { redirect } from "next/navigation";
import { AuthStatus } from "@/components/AuthStatus";
import { EventBrowseCard } from "@/components/EventBrowseCard";
import { EventBrowsePagination } from "@/components/EventBrowsePagination";
import { browseEvents } from "@/lib/events";

type HomeProps = {
  searchParams?: Promise<{
    page?: string | string[];
  }>;
};

export default async function Home({ searchParams }: HomeProps) {
  const params = (await searchParams) ?? {};
  const requestedPage = normalizePage(params.page);
  const result = await loadBrowseEvents(requestedPage);

  if (result.status === "error") {
    return <BrowseEventsError message={result.message} />;
  }

  const { events, pagination } = result.data;

  if (pagination.total_pages > 0 && requestedPage > pagination.total_pages) {
    redirect(pagination.total_pages === 1 ? "/" : `/?page=${pagination.total_pages}`);
  }

  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto flex w-full max-w-6xl flex-col gap-8">
        <div>
          <p className="text-sm font-medium uppercase tracking-wide text-slate-500">
            TicketPass marketplace
          </p>
          <h1 className="mt-3 text-4xl font-semibold text-slate-950 sm:text-5xl">
            Browse events with available tickets
          </h1>
          <p className="mt-4 max-w-2xl text-base leading-7 text-slate-600">
            Find events that currently have active TicketPass listings. Ticket details stay
            hidden until future checkout and reveal flows allow access.
          </p>
          <div className="mt-6">
            <Link
              href="/sell"
              className="inline-flex rounded-md bg-slate-950 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-800"
            >
              Sell a ticket
            </Link>
          </div>
        </div>

        {events.length > 0 ? (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {events.map((event) => (
              <EventBrowseCard key={event.id} event={event} />
            ))}
          </div>
        ) : (
          <div className="rounded-lg border border-slate-200 bg-white p-8 text-center shadow-sm">
            <h2 className="text-xl font-semibold text-slate-950">
              No events currently have tickets available.
            </h2>
            <p className="mt-2 text-sm text-slate-600">
              Check back later as sellers add new transferable tickets.
            </p>
          </div>
        )}

        <EventBrowsePagination pagination={pagination} />

        <div className="grid gap-4 lg:grid-cols-[1fr_2fr]">
          <AuthStatus />
        </div>
      </section>
    </main>
  );
}

async function loadBrowseEvents(page: number) {
  try {
    return {
      status: "success" as const,
      data: await browseEvents(page)
    };
  } catch (error) {
    return {
      status: "error" as const,
      message: error instanceof Error ? error.message : "Could not load events"
    };
  }
}

function normalizePage(value: string | string[] | undefined) {
  const rawValue = Array.isArray(value) ? value[0] : value;
  const parsedValue = Number(rawValue ?? "1");

  if (!Number.isInteger(parsedValue) || parsedValue < 1) {
    return 1;
  }

  return parsedValue;
}

function BrowseEventsError({ message }: { message: string }) {
  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto flex w-full max-w-3xl flex-col gap-6">
        <div className="rounded-lg border border-red-200 bg-red-50 p-6">
          <p className="text-sm font-medium uppercase tracking-wide text-red-800">
            Events unavailable
          </p>
          <h1 className="mt-3 text-3xl font-semibold text-red-950">
            Could not load events
          </h1>
          <p className="mt-3 text-sm text-red-800">{message}</p>
          <Link
            href="/"
            className="mt-5 inline-flex rounded-md bg-red-950 px-4 py-2 text-sm font-medium text-white transition hover:bg-red-900"
          >
            Reload events
          </Link>
        </div>

        <AuthStatus />
      </section>
    </main>
  );
}
