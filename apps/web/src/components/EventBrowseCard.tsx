import { EventDateTime } from "@/components/EventDateTime";
import { BrowseEventSummary } from "@/lib/events";

type EventBrowseCardProps = {
  event: BrowseEventSummary;
};

export function EventBrowseCard({ event }: EventBrowseCardProps) {
  return (
    <article className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
      <div className="flex aspect-[16/9] items-center justify-center border-b border-slate-200 bg-slate-100">
        <div className="flex h-16 w-16 items-center justify-center rounded-full border border-slate-300 bg-white text-sm font-semibold text-slate-500">
          TP
        </div>
      </div>

      <div className="flex min-h-64 flex-col gap-4 p-5">
        <div className="flex flex-1 flex-col gap-2">
          <h2 className="text-xl font-semibold leading-tight text-slate-950">{event.name}</h2>
          <p className="text-sm font-medium text-slate-700">
            <EventDateTime value={event.starts_at} />
          </p>
          <p className="text-sm text-slate-600">
            {event.venue}, {event.city}
          </p>
        </div>

        <div className="border-t border-slate-100 pt-4">
          <p className="text-lg font-semibold text-slate-950">
            {formatVnd(event.lowest_price_minor)}
          </p>
          <p className="mt-1 text-sm text-slate-600">
            {formatAvailableTickets(event.available_listing_count)}
          </p>
        </div>
      </div>
    </article>
  );
}

function formatVnd(value: number) {
  return `From ${new Intl.NumberFormat("vi-VN").format(value)} ₫`;
}

function formatAvailableTickets(value: number) {
  return `${new Intl.NumberFormat().format(value)} ${value === 1 ? "ticket" : "tickets"} available`;
}
