"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { EventListingSummary } from "@/lib/events";
import {
  ListingReservation,
  ReservationRequestError,
  reserveListing
} from "@/lib/reservations";
import { CheckoutRequestError, startCheckout } from "@/lib/checkout";

type EventListingCardProps = {
  listing: EventListingSummary;
  loginReturnTarget: string;
};

export function EventListingCard({ listing, loginReturnTarget }: EventListingCardProps) {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [reservation, setReservation] = useState<ListingReservation | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isRefreshingAvailability, setIsRefreshingAvailability] = useState(false);
  const [isStartingCheckout, setIsStartingCheckout] = useState(false);
  const [now, setNow] = useState(() => Date.now());
  const [hasExpired, setHasExpired] = useState(false);

  useEffect(() => {
    if (!reservation) {
      return;
    }

    const expiresAt = Date.parse(reservation.expires_at);
    let hasRefreshed = false;

    const updateCountdown = () => {
      const currentTime = Date.now();
      setNow(currentTime);

      if (currentTime >= expiresAt && !hasRefreshed) {
        hasRefreshed = true;
        setHasExpired(true);
        router.refresh();
      }
    };

    updateCountdown();
    const timer = window.setInterval(updateCountdown, 1000);

    return () => window.clearInterval(timer);
  }, [reservation, router]);

  async function handleReservation() {
    if (isSubmitting || isRefreshingAvailability || reservation) {
      return;
    }

    setIsSubmitting(true);
    setErrorMessage(null);

    try {
      const nextReservation = await reserveListing(listing.id);
      setReservation(nextReservation);
      setHasExpired(false);
      setNow(Date.now());
    } catch (error) {
      if (error instanceof ReservationRequestError) {
        if (error.status === 401) {
          router.push(`/login?next=${encodeURIComponent(loginReturnTarget)}`);
          return;
        }

        if (error.status === 404 || error.status === 409) {
          setErrorMessage("This ticket is no longer available. Refreshing availability...");
          setIsRefreshingAvailability(true);
          router.refresh();
          return;
        }

        if (error.status === 403) {
          setErrorMessage("Request rejected. Reload the page and try again.");
          return;
        }

        if (error.status === 400) {
          setErrorMessage(error.message);
          return;
        }
      }

      setErrorMessage("Could not reserve this ticket");
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleCheckout() {
    if (!reservation || isStartingCheckout || hasExpired) {
      return;
    }

    setIsStartingCheckout(true);
    setErrorMessage(null);

    try {
      const result = await startCheckout(reservation.id);

      if (result.payment_url) {
        window.location.assign(result.payment_url);
        return;
      }

      router.push(`/checkout/${result.order.id}`);
    } catch (error) {
      if (error instanceof CheckoutRequestError) {
        if (error.status === 401) {
          router.push(`/login?next=${encodeURIComponent(loginReturnTarget)}`);
          return;
        }

        if (error.status === 403) {
          setErrorMessage("Request rejected. Reload the page and try again.");
          return;
        }

        if (error.status === 404 || error.status === 409) {
          setErrorMessage("This checkout is no longer available. Refreshing availability...");
          setIsRefreshingAvailability(true);
          router.refresh();
          return;
        }

        if (error.status === 503) {
          setErrorMessage("The payment provider is temporarily unavailable. Please try again.");
          return;
        }
      }

      setErrorMessage("Could not start checkout. Please try again.");
    } finally {
      setIsStartingCheckout(false);
    }
  }

  const remainingSeconds = reservation
    ? Math.max(0, Math.ceil((Date.parse(reservation.expires_at) - now) / 1000))
    : 0;

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
      {reservation ? (
        <section className="rounded-md border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-950">
          <h3 className="font-semibold">Held for you</h3>
          <p className="mt-2 text-lg font-semibold tabular-nums">
            {hasExpired ? "00:00" : formatCountdown(remainingSeconds)}
          </p>
          <p className="mt-2 break-all">Expires at {reservation.expires_at}</p>
          <p className="mt-1 break-all">Reservation ID: {reservation.id}</p>
          <p className="mt-3 text-emerald-900">
            Checkout and payment have not occurred. Ticket transfer and reveal are not available.
          </p>
          {hasExpired ? (
            <p className="mt-2 font-medium text-emerald-900">Refreshing availability...</p>
          ) : (
            <button
              type="button"
              onClick={handleCheckout}
              disabled={isStartingCheckout}
              className="mt-4 rounded-md bg-slate-950 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
            >
              {isStartingCheckout ? "Opening checkout..." : "Continue to checkout"}
            </button>
          )}
          {errorMessage ? <p className="mt-3 text-sm font-medium text-red-800">{errorMessage}</p> : null}
        </section>
      ) : (
        <section className="flex flex-col gap-3 border-t border-slate-100 pt-4">
          <p className="text-sm text-slate-600">
            Reserving creates a temporary 10-minute hold only. It does not complete payment,
            purchase, ownership transfer, or ticket reveal.
          </p>
          {errorMessage ? (
            <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
              {errorMessage}
            </p>
          ) : null}
          {isRefreshingAvailability ? null : (
            <button
              type="button"
              onClick={handleReservation}
              disabled={isSubmitting}
              className="rounded-md bg-slate-950 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
            >
              {isSubmitting ? "Reserving..." : errorMessage ? "Retry" : "Reserve for 10 minutes"}
            </button>
          )}
        </section>
      )}
    </article>
  );
}

function formatCountdown(totalSeconds: number) {
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;

  return `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
}

function formatTransferMethod(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}
