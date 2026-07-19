"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { ReactNode, useEffect, useMemo, useState } from "react";
import { EventDateTime } from "@/components/EventDateTime";
import {
  getSellerOwnListings,
  parseSellerOwnListingsQuery,
  SellerListingStatus,
  SellerListingsRequestError,
  SellerListingsResponseError,
  SellerOwnListing,
  SellerOwnListingsPage
} from "@/lib/sellerListings";
import { buildAuthHref } from "@/lib/redirects";

const statusOptions: Array<{ value: SellerListingStatus | null; label: string }> = [
  { value: null, label: "All statuses" },
  { value: "DRAFT", label: "Draft" },
  { value: "ACTIVE", label: "Active" },
  { value: "RESERVED", label: "Reserved" },
  { value: "SOLD", label: "Sold" },
  { value: "CANCELLED", label: "Cancelled" },
  { value: "EXPIRED", label: "Expired" }
];

const statusDetails: Record<SellerListingStatus, { label: string; description: string; className: string }> = {
  DRAFT: { label: "Draft", description: "Not publicly available", className: "border-slate-300 bg-slate-100 text-slate-700" },
  ACTIVE: { label: "Active", description: "Available to buyers", className: "border-emerald-200 bg-emerald-50 text-emerald-800" },
  RESERVED: { label: "Reserved", description: "Temporarily held; buyer identity is not shown", className: "border-amber-200 bg-amber-50 text-amber-900" },
  SOLD: { label: "Sold", description: "No longer available; this does not imply payout or settlement", className: "border-blue-200 bg-blue-50 text-blue-800" },
  CANCELLED: { label: "Cancelled", description: "Cancelled and no longer available", className: "border-red-200 bg-red-50 text-red-800" },
  EXPIRED: { label: "Expired", description: "Expired and no longer available", className: "border-slate-300 bg-slate-100 text-slate-700" }
};

type PageState =
  | { status: "loading" }
  | { status: "ready"; page: SellerOwnListingsPage }
  | { status: "error"; kind: "validation" | "inaccessible" | "service" | "unavailable" };

type ErrorKind = "validation" | "inaccessible" | "service" | "unavailable";

export function SellerOwnListings() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const query = useMemo(
    () => parseSellerOwnListingsQuery(searchParams.get("page"), searchParams.get("status")),
    [searchParams]
  );
  const [pageState, setPageState] = useState<PageState>({ status: "loading" });
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setPageState({ status: "loading" });

    getSellerOwnListings(query, controller.signal)
      .then((page) => {
        if (!controller.signal.aborted) {
          setPageState({ status: "ready", page });
        }
      })
      .catch((error: unknown) => {
        if (controller.signal.aborted) {
          return;
        }
        if (error instanceof SellerListingsRequestError && error.status === 401) {
          router.replace(buildAuthHref("/login", "/my-listings"));
          return;
        }
        if (error instanceof SellerListingsRequestError && error.status === 400) {
          setPageState({ status: "error", kind: "validation" });
          return;
        }
        if (error instanceof SellerListingsRequestError && (error.status === 403 || error.status === 404)) {
          setPageState({ status: "error", kind: "inaccessible" });
          return;
        }
        setPageState({ status: "error", kind: error instanceof SellerListingsResponseError ? "unavailable" : "service" });
      });

    return () => controller.abort();
  }, [query, retryKey, router]);

  function updateQuery(page: number, status: SellerListingStatus | null) {
    const params = new URLSearchParams();
    if (page > 1) {
      params.set("page", page.toString());
    }
    if (status) {
      params.set("status", status);
    }
    const suffix = params.size > 0 ? `?${params}` : "";
    router.push(`/my-listings${suffix}`);
  }

  if (pageState.status === "loading") {
    return <LoadingState />;
  }

  if (pageState.status === "error") {
    return <ErrorState kind={pageState.kind} onRetry={() => setRetryKey((current) => current + 1)} onReset={() => updateQuery(1, null)} />;
  }

  const { page } = pageState;
  const isFilteredEmpty = page.items.length === 0 && page.total_items > 0 && query.status !== null;
  const hasNoListings = page.items.length === 0 && page.total_items === 0 && query.status === null;

  return (
    <section className="flex flex-col gap-6">
      <div className="flex flex-col gap-4 border-b border-slate-200 pb-6 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm font-medium uppercase text-slate-500">Seller account</p>
          <h1 className="mt-2 text-3xl font-semibold text-slate-950">My listings</h1>
          <p className="mt-2 text-sm text-slate-600">Review the current marketplace status of your ticket listings.</p>
        </div>
        <label className="flex flex-col gap-1 text-sm font-medium text-slate-700">
          Status
          <select
            value={query.status ?? ""}
            onChange={(event) => updateQuery(1, event.target.value ? event.target.value as SellerListingStatus : null)}
            className="min-h-10 rounded-md border border-slate-300 bg-white px-3 text-sm text-slate-900"
          >
            {statusOptions.map((option) => <option key={option.value ?? "all"} value={option.value ?? ""}>{option.label}</option>)}
          </select>
        </label>
      </div>

      {hasNoListings ? <EmptyListings /> : null}
      {isFilteredEmpty ? <FilteredEmpty onClear={() => updateQuery(1, null)} /> : null}
      {!hasNoListings && !isFilteredEmpty ? <ListingResults page={page} onChangePage={(nextPage) => updateQuery(nextPage, query.status)} /> : null}
    </section>
  );
}

function ListingResults({ page, onChangePage }: { page: SellerOwnListingsPage; onChangePage: (page: number) => void }) {
  return (
    <>
      <p className="text-sm text-slate-600">{page.total_items} {page.total_items === 1 ? "listing" : "listings"}</p>
      <div className="grid gap-4">
        {page.items.map((listing) => <ListingCard key={listing.id} listing={listing} />)}
      </div>
      <nav aria-label="My listings pagination" className="flex flex-col gap-3 border-t border-slate-200 pt-6 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-slate-600">Page {page.page} of {page.total_pages}</p>
        <div className="flex gap-3">
          <button type="button" disabled={page.page === 1} onClick={() => onChangePage(page.page - 1)} className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-800 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:border-slate-200 disabled:text-slate-400">Previous</button>
          <button type="button" disabled={page.page >= page.total_pages} onClick={() => onChangePage(page.page + 1)} className="rounded-md bg-slate-950 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-300">Next</button>
        </div>
      </nav>
    </>
  );
}

function ListingCard({ listing }: { listing: SellerOwnListing }) {
  const status = statusDetails[listing.status];
  const notes = listing.public_notes?.trim();
  return (
    <article className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <p className="text-sm font-medium text-slate-500">{listing.event.name}</p>
          <h2 className="mt-1 text-xl font-semibold text-slate-950">{listing.ticket_type}</h2>
          <p className="mt-2 text-sm text-slate-600"><EventDateTime value={listing.event.starts_at} /> · {listing.event.venue}, {listing.event.city}</p>
        </div>
        <div className={`w-fit rounded-full border px-3 py-1 text-sm font-medium ${status.className}`} title={status.description}>{status.label}</div>
      </div>
      <p className="mt-2 text-sm text-slate-600">{status.description}</p>
      <dl className="mt-5 grid gap-3 border-t border-slate-100 pt-5 text-sm sm:grid-cols-2">
        <Detail label="Price" value={`VND ${new Intl.NumberFormat("vi-VN").format(listing.asking_price_minor)}`} />
        <Detail label="Quantity" value={listing.quantity.toString()} />
        <Detail label="Seat" value={listing.seat_info} />
        <Detail label="Platform" value={listing.event_platform} />
        <Detail label="Transfer method" value={formatTransferMethod(listing.transfer_method)} />
        <Detail label="Transferability" value={listing.is_transferable_confirmed ? "Confirmed by seller" : "Not confirmed"} />
        <Detail label="Created" value={<EventDateTime value={listing.created_at} />} />
        <Detail label="Last updated" value={<EventDateTime value={listing.updated_at} />} />
      </dl>
      {notes ? <div className="mt-5 border-t border-slate-100 pt-5"><p className="text-sm font-medium text-slate-700">Public notes</p><p className="mt-1 break-words text-sm leading-6 text-slate-600">{notes}</p></div> : null}
    </article>
  );
}

function Detail({ label, value }: { label: string; value: ReactNode }) {
  return <div className="min-w-0"><dt className="text-slate-500">{label}</dt><dd className="mt-1 break-words font-medium text-slate-800">{value}</dd></div>;
}

function EmptyListings() {
  return <section className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm"><h2 className="text-lg font-semibold text-slate-950">No listings yet</h2><p className="mt-2 text-sm text-slate-600">Create a listing when you have a transferable ticket to sell.</p><Link href="/sell" className="mt-4 inline-flex rounded-md bg-slate-950 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-slate-800">Sell tickets</Link></section>;
}

function FilteredEmpty({ onClear }: { onClear: () => void }) {
  return <section className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm"><h2 className="text-lg font-semibold text-slate-950">No matching listings</h2><p className="mt-2 text-sm text-slate-600">No listings have the selected status.</p><button type="button" onClick={onClear} className="mt-4 rounded-md border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-800 transition hover:border-slate-400 hover:bg-slate-50">Clear filter</button></section>;
}

function ErrorState({ kind, onRetry, onReset }: { kind: ErrorKind; onRetry: () => void; onReset: () => void }) {
  const message = kind === "validation" ? "The selected page or status is unavailable." : kind === "inaccessible" ? "This listing view is unavailable." : kind === "unavailable" ? "Listing data is temporarily unavailable." : "Could not load listings. Please try again.";
  return <section className="rounded-lg border border-red-200 bg-red-50 p-6 text-red-900"><h1 className="text-lg font-semibold">Could not show your listings</h1><p className="mt-2 text-sm">{message}</p><div className="mt-4 flex flex-wrap gap-3"><button type="button" onClick={onRetry} className="rounded-md bg-slate-950 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-slate-800">Retry</button>{kind === "validation" ? <button type="button" onClick={onReset} className="rounded-md border border-slate-300 bg-white px-4 py-2.5 text-sm font-medium text-slate-800 transition hover:border-slate-400 hover:bg-slate-50">Reset filters</button> : null}</div></section>;
}

function LoadingState() {
  return <section className="flex flex-col gap-4" aria-live="polite"><div><p className="text-sm font-medium uppercase text-slate-500">Seller account</p><h1 className="mt-2 text-3xl font-semibold text-slate-950">My listings</h1></div><p className="text-sm text-slate-600">Loading listings...</p></section>;
}

function formatTransferMethod(value: string) {
  return value.toLowerCase().split("_").map((word) => word.charAt(0).toUpperCase() + word.slice(1)).join(" ");
}
