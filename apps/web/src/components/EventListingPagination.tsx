import Link from "next/link";
import { EventBrowsePagination } from "@/lib/events";

type EventListingPaginationProps = {
  eventId: string;
  pagination: EventBrowsePagination;
};

export function EventListingPagination({ eventId, pagination }: EventListingPaginationProps) {
  if (pagination.total_pages <= 1) {
    return null;
  }

  const hrefForPage = (page: number) =>
    page === 1 ? `/events/${eventId}` : `/events/${eventId}?page=${page}`;

  return (
    <nav
      aria-label="Available tickets pagination"
      className="flex flex-col items-start gap-3 border-t border-slate-200 pt-6 sm:flex-row sm:items-center sm:justify-between"
    >
      <p className="text-sm text-slate-600">
        Page {pagination.page} of {pagination.total_pages}
      </p>
      <div className="flex gap-3">
        {pagination.page > 1 ? (
          <Link
            href={hrefForPage(pagination.page - 1)}
            className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-800 transition hover:border-slate-400 hover:bg-slate-50"
          >
            Previous
          </Link>
        ) : (
          <span className="rounded-md border border-slate-200 px-4 py-2 text-sm font-medium text-slate-400">
            Previous
          </span>
        )}
        {pagination.page < pagination.total_pages ? (
          <Link
            href={hrefForPage(pagination.page + 1)}
            className="rounded-md bg-slate-950 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-800"
          >
            Next
          </Link>
        ) : (
          <span className="rounded-md bg-slate-200 px-4 py-2 text-sm font-medium text-slate-500">
            Next
          </span>
        )}
      </div>
    </nav>
  );
}
