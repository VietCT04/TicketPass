import { EventListingSummary } from "@/lib/events";

type EventListingCardProps = {
  listing: EventListingSummary;
};

export function EventListingCard({ listing }: EventListingCardProps) {
  return (
    <article className="flex min-h-52 flex-col gap-5 rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex flex-1 flex-col gap-3">
        <div>
          <p className="text-sm font-medium text-slate-500">{listing.ticket_type}</p>
          <h2 className="mt-1 text-lg font-semibold text-slate-950">{listing.seat_info}</h2>
        </div>
        <dl className="grid gap-2 text-sm text-slate-600">
          <div className="flex justify-between gap-4">
            <dt>Platform</dt>
            <dd className="text-right font-medium text-slate-800">{listing.event_platform}</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt>Transfer method</dt>
            <dd className="text-right font-medium text-slate-800">
              {formatTransferMethod(listing.transfer_method)}
            </dd>
          </div>
        </dl>
      </div>
      <p className="border-t border-slate-100 pt-4 text-xl font-semibold text-slate-950">
        VND {new Intl.NumberFormat("vi-VN").format(listing.asking_price_minor)}
      </p>
    </article>
  );
}

function formatTransferMethod(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}
