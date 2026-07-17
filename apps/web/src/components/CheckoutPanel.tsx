"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import {
  CheckoutRequestError,
  SafeCheckoutOrder,
  getOrder,
  startCheckout
} from "@/lib/checkout";
import { buildAuthHref } from "@/lib/redirects";

type CheckoutPanelProps = {
  orderId: string;
};

type ProviderReturnHint = "success" | "failed" | "cancelled";
type ViewState =
  | { kind: "loading" }
  | { kind: "ready"; order: SafeCheckoutOrder }
  | { kind: "error"; message: string; unavailable?: boolean };

const providerReturnMessages: Record<ProviderReturnHint, string> = {
  success: "Payment was submitted. Waiting for server confirmation.",
  failed: "The provider reported a failed attempt. Checking current status.",
  cancelled: "You returned from the payment page. Checking current status."
};

export function CheckoutPanel({ orderId }: CheckoutPanelProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const providerReturnHint = useRef(readProviderReturnHint(searchParams.get("provider_return"))).current;
  const [viewState, setViewState] = useState<ViewState>({ kind: "loading" });
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isStartingPayment, setIsStartingPayment] = useState(false);
  const [now, setNow] = useState(() => Date.now());
  const refreshedAtExpiry = useRef(false);

  const checkoutPath = `/checkout/${orderId}`;

  useEffect(() => {
    if (providerReturnHint) {
      router.replace(checkoutPath);
    }
  }, [checkoutPath, providerReturnHint, router]);

  useEffect(() => {
    void refreshOrder();
    // The route ID is server-validated by the page before this client component mounts.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orderId]);

  useEffect(() => {
    if (viewState.kind !== "ready" || viewState.order.status !== "PAYMENT_PENDING") {
      return;
    }

    const expiresAt = Date.parse(viewState.order.expires_at);
    refreshedAtExpiry.current = false;

    const updateCountdown = () => {
      const currentTime = Date.now();
      setNow(currentTime);

      if (currentTime >= expiresAt && !refreshedAtExpiry.current) {
        refreshedAtExpiry.current = true;
        void refreshOrder();
      }
    };

    updateCountdown();
    const timer = window.setInterval(updateCountdown, 1000);

    return () => window.clearInterval(timer);
    // refreshOrder is intentionally stable for this order route and reads current component state only by setter.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [viewState.kind === "ready" ? viewState.order.expires_at : null, viewState.kind === "ready" ? viewState.order.status : null]);

  useEffect(() => {
    if (
      !providerReturnHint ||
      viewState.kind !== "ready" ||
      viewState.order.status !== "PAYMENT_PENDING" ||
      viewState.order.payment_review_required
    ) {
      return;
    }

    let attempts = 0;
    const timer = window.setInterval(() => {
      if (attempts >= 15) {
        window.clearInterval(timer);
        return;
      }

      if (document.visibilityState !== "visible") {
        return;
      }

      attempts += 1;
      void refreshOrder();
    }, 2000);

    return () => window.clearInterval(timer);
    // The polling lifetime is defined by this order's server state and the captured return hint.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [providerReturnHint, viewState.kind === "ready" ? viewState.order.status : null, viewState.kind === "ready" ? viewState.order.payment_review_required : null]);

  async function refreshOrder() {
    setIsRefreshing(true);

    try {
      setViewState({ kind: "ready", order: await getOrder(orderId) });
    } catch (error) {
      handleRequestError(error);
    } finally {
      setIsRefreshing(false);
    }
  }

  async function continueToPayment(reservationId: string) {
    if (isStartingPayment) {
      return;
    }

    setIsStartingPayment(true);

    try {
      const result = await startCheckout(reservationId);

      if (result.payment_url) {
        window.location.assign(result.payment_url);
        return;
      }

      if (result.order.id === orderId) {
        await refreshOrder();
      } else {
        router.push(`/checkout/${result.order.id}`);
      }
    } catch (error) {
      if (error instanceof CheckoutRequestError && error.status === 409) {
        await refreshOrder();
      } else {
        handleRequestError(error);
      }
    } finally {
      setIsStartingPayment(false);
    }
  }

  function handleRequestError(error: unknown) {
    if (error instanceof CheckoutRequestError) {
      if (error.status === 401) {
        router.replace(buildAuthHref("/login", checkoutPath));
        return;
      }

      if (error.status === 403) {
        setViewState({ kind: "error", message: "Request rejected. Reload the page and try again." });
        return;
      }

      if (error.status === 404) {
        setViewState({ kind: "error", message: "This checkout is unavailable.", unavailable: true });
        return;
      }

      if (error.status === 503) {
        setViewState({
          kind: "error",
          message: "The payment provider is temporarily unavailable. Please try again."
        });
        return;
      }
    }

    setViewState({ kind: "error", message: "Could not load checkout. Please try again." });
  }

  if (viewState.kind === "loading") {
    return <CheckoutLoading />;
  }

  if (viewState.kind === "error") {
    return (
      <main className="min-h-screen px-6 py-10">
        <section className="mx-auto flex w-full max-w-2xl flex-col gap-5">
          <Link href="/" className="w-fit text-sm font-medium text-slate-700 underline underline-offset-4">
            Browse events
          </Link>
          <div className="rounded-lg border border-red-200 bg-red-50 p-6">
            <p className="text-sm font-medium uppercase text-red-800">Checkout unavailable</p>
            <h1 className="mt-2 text-2xl font-semibold text-red-950">We could not load this checkout.</h1>
            <p className="mt-3 text-sm text-red-800">{viewState.message}</p>
            {!viewState.unavailable ? (
              <button
                type="button"
                onClick={() => void refreshOrder()}
                disabled={isRefreshing}
                className="mt-5 rounded-md bg-red-950 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-red-900 disabled:cursor-not-allowed disabled:bg-red-400"
              >
                {isRefreshing ? "Refreshing..." : "Retry"}
              </button>
            ) : null}
          </div>
        </section>
      </main>
    );
  }

  const { order } = viewState;
  const remainingSeconds = Math.max(0, Math.ceil((Date.parse(order.expires_at) - now) / 1000));
  const canContinuePayment =
    order.status === "PAYMENT_PENDING" && !order.payment_review_required && remainingSeconds > 0;

  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto flex w-full max-w-2xl flex-col gap-6">
        <Link href="/" className="w-fit text-sm font-medium text-slate-700 underline underline-offset-4">
          Browse events
        </Link>

        <header>
          <p className="text-sm font-medium uppercase text-slate-500">Checkout</p>
          <h1 className="mt-2 text-3xl font-semibold text-slate-950">{statusHeading(order)}</h1>
          {providerReturnHint && order.status === "PAYMENT_PENDING" ? (
            <p className="mt-3 text-sm text-slate-600">{providerReturnMessages[providerReturnHint]}</p>
          ) : null}
        </header>

        <article className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex flex-col gap-5">
            <div>
              <p className="text-sm font-medium text-slate-500">Event</p>
              <h2 className="mt-1 text-xl font-semibold text-slate-950">{order.event.name}</h2>
              <p className="mt-2 text-sm text-slate-600">
                {formatDateTime(order.event.starts_at)} at {order.event.venue}, {order.event.city}
              </p>
            </div>

            <dl className="grid gap-3 border-y border-slate-100 py-5 text-sm text-slate-700 sm:grid-cols-2">
              <div>
                <dt className="text-slate-500">Ticket</dt>
                <dd className="mt-1 font-medium text-slate-950">{order.ticket.ticket_type}</dd>
              </div>
              <div>
                <dt className="text-slate-500">Seat</dt>
                <dd className="mt-1 font-medium text-slate-950">{order.ticket.seat_info}</dd>
              </div>
              <div>
                <dt className="text-slate-500">Transfer method</dt>
                <dd className="mt-1 font-medium text-slate-950">
                  {formatTransferMethod(order.ticket.transfer_method)}
                </dd>
              </div>
              <div>
                <dt className="text-slate-500">Amount</dt>
                <dd className="mt-1 font-medium text-slate-950">{formatAmount(order)}</dd>
              </div>
            </dl>

            {order.status === "PAYMENT_PENDING" ? (
              <div className="rounded-md border border-amber-200 bg-amber-50 p-4 text-sm text-amber-950">
                <p className="font-semibold">
                  {order.payment_review_required
                    ? "Payment confirmation needs review."
                    : "Payment is not confirmed."}
                </p>
                <p className="mt-2">
                  Deadline: {formatDateTime(order.expires_at)}
                  {!order.payment_review_required ? ` (${formatCountdown(remainingSeconds)} remaining)` : ""}
                </p>
              </div>
            ) : (
              <OrderStatusMessage status={order.status} />
            )}

            <div className="flex flex-wrap gap-3">
              {canContinuePayment ? (
                <button
                  type="button"
                  onClick={() => void continueToPayment(order.reservation_id)}
                  disabled={isStartingPayment}
                  className="rounded-md bg-slate-950 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
                >
                  {isStartingPayment ? "Opening payment..." : "Continue to payment"}
                </button>
              ) : null}
              <button
                type="button"
                onClick={() => void refreshOrder()}
                disabled={isRefreshing || isStartingPayment}
                className="rounded-md border border-slate-300 px-4 py-2.5 text-sm font-medium text-slate-800 transition hover:border-slate-900 disabled:cursor-not-allowed disabled:text-slate-400"
              >
                {isRefreshing ? "Refreshing..." : "Refresh status"}
              </button>
            </div>
          </div>
        </article>
      </section>
    </main>
  );
}

function CheckoutLoading() {
  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto w-full max-w-2xl">
        <p className="text-sm font-medium uppercase text-slate-500">Checkout</p>
        <h1 className="mt-3 text-3xl font-semibold text-slate-950">Loading checkout...</h1>
      </section>
    </main>
  );
}

function OrderStatusMessage({ status }: { status: Exclude<SafeCheckoutOrder["status"], "PAYMENT_PENDING"> }) {
  const messages = {
    PAID: "Payment is confirmed. Ticket delivery or reveal and seller payout are not implemented by this checkout flow.",
    PAYMENT_FAILED: "Payment was not completed. This order is terminal.",
    CANCELLED: "Checkout was cancelled. This order is terminal.",
    EXPIRED: "The checkout deadline passed. This order is terminal."
  };

  return (
    <div className="rounded-md border border-slate-200 bg-slate-50 p-4 text-sm text-slate-800">
      <p className="font-semibold">{messages[status]}</p>
    </div>
  );
}

function statusHeading(order: SafeCheckoutOrder): string {
  if (order.status === "PAYMENT_PENDING") {
    return order.payment_review_required ? "Payment confirmation needs review" : "Complete your payment";
  }

  if (order.status === "PAID") {
    return "Payment confirmed";
  }

  if (order.status === "PAYMENT_FAILED") {
    return "Payment not completed";
  }

  if (order.status === "CANCELLED") {
    return "Checkout cancelled";
  }

  return "Checkout expired";
}

function readProviderReturnHint(value: string | null): ProviderReturnHint | null {
  return value === "success" || value === "failed" || value === "cancelled" ? value : null;
}

function formatDateTime(value: string): string {
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}

function formatAmount(order: SafeCheckoutOrder): string {
  return `${order.currency} ${new Intl.NumberFormat("vi-VN").format(order.amount_minor)}`;
}

function formatCountdown(totalSeconds: number): string {
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
}

function formatTransferMethod(value: string): string {
  return value
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}
