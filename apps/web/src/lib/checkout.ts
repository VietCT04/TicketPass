const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const orderStatuses = new Set([
  "PAYMENT_PENDING",
  "PAID",
  "PAYMENT_FAILED",
  "CANCELLED",
  "EXPIRED"
]);

export type CheckoutOrderStatus =
  | "PAYMENT_PENDING"
  | "PAID"
  | "PAYMENT_FAILED"
  | "CANCELLED"
  | "EXPIRED";

export type SafeCheckoutOrder = {
  id: string;
  reservation_id: string;
  listing_id: string;
  status: CheckoutOrderStatus;
  amount_minor: number;
  currency: string;
  expires_at: string;
  created_at: string;
  updated_at: string;
  paid_at: string | null;
  payment_review_required: boolean;
  event: {
    name: string;
    starts_at: string;
    venue: string;
    city: string;
  };
  ticket: {
    ticket_type: string;
    seat_info: string;
    transfer_method: string;
  };
};

export type CheckoutStartResult = {
  order: SafeCheckoutOrder;
  payment_url?: string;
  payment_url_expires_at?: string;
};

export class CheckoutRequestError extends Error {
  constructor(readonly status: number) {
    super("Checkout request failed");
    this.name = "CheckoutRequestError";
  }
}

export function isCheckoutOrderId(value: string): boolean {
  return uuidPattern.test(value);
}

export async function startCheckout(reservationId: string): Promise<CheckoutStartResult> {
  const response = await fetch(`${apiBaseUrl}/api/reservations/${reservationId}/checkout`, {
    method: "POST",
    credentials: "include",
    cache: "no-store"
  });

  if (response.status !== 200 && response.status !== 201) {
    throw new CheckoutRequestError(response.status);
  }

  const result = await parseCheckoutStart(response);

  if (!result || result.order.reservation_id !== reservationId) {
    throw new CheckoutRequestError(0);
  }

  return result;
}

export async function getOrder(orderId: string): Promise<SafeCheckoutOrder> {
  const response = await fetch(`${apiBaseUrl}/api/orders/${orderId}`, {
    credentials: "include",
    cache: "no-store"
  });

  if (!response.ok) {
    throw new CheckoutRequestError(response.status);
  }

  const order = await parseOrder(response);

  if (!order || order.id !== orderId) {
    throw new CheckoutRequestError(0);
  }

  return order;
}

async function parseCheckoutStart(response: Response): Promise<CheckoutStartResult | null> {
  try {
    const body = (await response.json()) as Record<string, unknown>;
    const order = parseSafeOrder(body.order);

    if (!order) {
      return null;
    }

    const paymentUrl = body.payment_url;
    const paymentUrlExpiresAt = body.payment_url_expires_at;

    if (paymentUrl === undefined && paymentUrlExpiresAt === undefined) {
      return { order };
    }

    if (
      typeof paymentUrl !== "string" ||
      !isSafePaymentUrl(paymentUrl) ||
      order.status !== "PAYMENT_PENDING" ||
      !isTimestamp(paymentUrlExpiresAt) ||
      paymentUrlExpiresAt !== order.expires_at
    ) {
      return null;
    }

    return {
      order,
      payment_url: paymentUrl,
      payment_url_expires_at: paymentUrlExpiresAt
    };
  } catch {
    return null;
  }
}

async function parseOrder(response: Response): Promise<SafeCheckoutOrder | null> {
  try {
    return parseSafeOrder(await response.json());
  } catch {
    return null;
  }
}

function parseSafeOrder(value: unknown): SafeCheckoutOrder | null {
  if (!isRecord(value)) {
    return null;
  }

  const event = value.event;
  const ticket = value.ticket;

  if (
    !isUuid(value.id) ||
    !isUuid(value.reservation_id) ||
    !isUuid(value.listing_id) ||
    !isOrderStatus(value.status) ||
    !isPositiveSafeInteger(value.amount_minor) ||
    !isNonEmptyString(value.currency) ||
    !isTimestamp(value.expires_at) ||
    !isTimestamp(value.created_at) ||
    !isTimestamp(value.updated_at) ||
    !(value.paid_at === null || isTimestamp(value.paid_at)) ||
    typeof value.payment_review_required !== "boolean" ||
    !isRecord(event) ||
    !isNonEmptyString(event.name) ||
    !isTimestamp(event.starts_at) ||
    !isNonEmptyString(event.venue) ||
    !isNonEmptyString(event.city) ||
    !isRecord(ticket) ||
    !isNonEmptyString(ticket.ticket_type) ||
    !isNonEmptyString(ticket.seat_info) ||
    !isNonEmptyString(ticket.transfer_method)
  ) {
    return null;
  }

  return {
    id: value.id,
    reservation_id: value.reservation_id,
    listing_id: value.listing_id,
    status: value.status,
    amount_minor: value.amount_minor,
    currency: value.currency,
    expires_at: value.expires_at,
    created_at: value.created_at,
    updated_at: value.updated_at,
    paid_at: value.paid_at,
    payment_review_required: value.payment_review_required,
    event: {
      name: event.name,
      starts_at: event.starts_at,
      venue: event.venue,
      city: event.city
    },
    ticket: {
      ticket_type: ticket.ticket_type,
      seat_info: ticket.seat_info,
      transfer_method: ticket.transfer_method
    }
  };
}

function isSafePaymentUrl(value: string): boolean {
  try {
    const url = new URL(value);
    return (url.protocol === "http:" || url.protocol === "https:") && !url.username && !url.password;
  } catch {
    return false;
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isUuid(value: unknown): value is string {
  return typeof value === "string" && uuidPattern.test(value);
}

function isOrderStatus(value: unknown): value is CheckoutOrderStatus {
  return typeof value === "string" && orderStatuses.has(value);
}

function isPositiveSafeInteger(value: unknown): value is number {
  return typeof value === "number" && Number.isSafeInteger(value) && value > 0;
}

function isNonEmptyString(value: unknown): value is string {
  return typeof value === "string" && value.trim().length > 0;
}

function isTimestamp(value: unknown): value is string {
  return typeof value === "string" && !Number.isNaN(Date.parse(value));
}
