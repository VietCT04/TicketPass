const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

const listingStatuses = ["DRAFT", "ACTIVE", "RESERVED", "SOLD", "CANCELLED", "EXPIRED"] as const;
const transferMethods = ["PLATFORM_TRANSFER", "PDF_UPLOAD", "QR_UPLOAD", "MANUAL_TRANSFER"] as const;

export type SellerListingStatus = (typeof listingStatuses)[number];

export type SellerOwnListing = {
  id: string;
  status: SellerListingStatus;
  event_platform: string;
  seat_info: string;
  ticket_type: string;
  quantity: 1;
  asking_price_minor: number;
  currency: "VND";
  transfer_method: (typeof transferMethods)[number];
  is_transferable_confirmed: boolean;
  public_notes: string | null;
  created_at: string;
  updated_at: string;
  event: {
    id: string;
    name: string;
    starts_at: string;
    venue: string;
    city: string;
  };
};

export type SellerOwnListingsPage = {
  items: SellerOwnListing[];
  page: number;
  page_size: number;
  total_items: number;
  total_pages: number;
};

export type SellerOwnListingsQuery = {
  page: number;
  status: SellerListingStatus | null;
};

export class SellerListingsRequestError extends Error {
  constructor(public readonly status: number) {
    super("Could not load listings");
    this.name = "SellerListingsRequestError";
  }
}

export class SellerListingsResponseError extends Error {
  constructor() {
    super("Listings are unavailable");
    this.name = "SellerListingsResponseError";
  }
}

export function parseSellerOwnListingsQuery(
  pageValue: string | null,
  statusValue: string | null
): SellerOwnListingsQuery {
  const page = pageValue && /^[1-9][0-9]*$/.test(pageValue) ? Number(pageValue) : 1;
  const status = isListingStatus(statusValue) ? statusValue : null;

  return { page: Number.isSafeInteger(page) ? page : 1, status };
}

export async function getSellerOwnListings(
  query: SellerOwnListingsQuery,
  signal?: AbortSignal
): Promise<SellerOwnListingsPage> {
  const params = new URLSearchParams({ page: query.page.toString(), page_size: "20" });

  if (query.status) {
    params.set("status", query.status);
  }

  const response = await fetch(`${apiBaseUrl}/api/me/listings?${params}`, {
    credentials: "include",
    cache: "no-store",
    signal
  });

  if (!response.ok) {
    throw new SellerListingsRequestError(response.status);
  }

  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new SellerListingsResponseError();
  }

  return parseSellerOwnListingsPage(body);
}

function parseSellerOwnListingsPage(value: unknown): SellerOwnListingsPage {
  if (!isRecord(value) || !hasOnlyKeys(value, ["items", "page", "page_size", "total_items", "total_pages"])) {
    throw new SellerListingsResponseError();
  }

  if (!Array.isArray(value.items) || !isPageNumber(value.page) || value.page_size !== 20 ||
    !isNonNegativeSafeInteger(value.total_items) || !isNonNegativeSafeInteger(value.total_pages)) {
    throw new SellerListingsResponseError();
  }

  const items = value.items.map(parseSellerOwnListing);
  if ((value.total_items === 0 && value.total_pages !== 0) ||
    (value.total_items > 0 && value.total_pages < 1)) {
    throw new SellerListingsResponseError();
  }

  return {
    items,
    page: value.page,
    page_size: value.page_size,
    total_items: value.total_items,
    total_pages: value.total_pages
  };
}

function parseSellerOwnListing(value: unknown): SellerOwnListing {
  const keys = [
    "id", "status", "event_platform", "seat_info", "ticket_type", "quantity",
    "asking_price_minor", "currency", "transfer_method", "is_transferable_confirmed",
    "public_notes", "created_at", "updated_at", "event"
  ];
  if (!isRecord(value) || !hasOnlyKeys(value, keys) || !isUuid(value.id) || !isListingStatus(value.status) ||
    !isNonBlankString(value.event_platform) || !isNonBlankString(value.seat_info) ||
    !isNonBlankString(value.ticket_type) || value.quantity !== 1 || !isPositiveSafeInteger(value.asking_price_minor) ||
    value.currency !== "VND" || !isTransferMethod(value.transfer_method) ||
    typeof value.is_transferable_confirmed !== "boolean" ||
    !(value.public_notes === null || typeof value.public_notes === "string") ||
    !isIsoTimestamp(value.created_at) || !isIsoTimestamp(value.updated_at) || !isEvent(value.event)) {
    throw new SellerListingsResponseError();
  }

  return value as SellerOwnListing;
}

function isEvent(value: unknown): value is SellerOwnListing["event"] {
  return isRecord(value) && hasOnlyKeys(value, ["id", "name", "starts_at", "venue", "city"]) &&
    isUuid(value.id) && isNonBlankString(value.name) && isIsoTimestamp(value.starts_at) &&
    isNonBlankString(value.venue) && isNonBlankString(value.city);
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function hasOnlyKeys(value: Record<string, unknown>, keys: string[]) {
  const actualKeys = Object.keys(value);
  return actualKeys.length === keys.length &&
    keys.every((key) => Object.prototype.hasOwnProperty.call(value, key));
}

function isUuid(value: unknown): value is string {
  return typeof value === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(value);
}

function isIsoTimestamp(value: unknown): value is string {
  return typeof value === "string" && value.length > 0 && !Number.isNaN(Date.parse(value));
}

function isNonBlankString(value: unknown): value is string {
  return typeof value === "string" && value.trim().length > 0;
}

function isPageNumber(value: unknown): value is number {
  return isPositiveSafeInteger(value);
}

function isPositiveSafeInteger(value: unknown): value is number {
  return typeof value === "number" && Number.isSafeInteger(value) && value > 0;
}

function isNonNegativeSafeInteger(value: unknown): value is number {
  return typeof value === "number" && Number.isSafeInteger(value) && value >= 0;
}

function isListingStatus(value: unknown): value is SellerListingStatus {
  return typeof value === "string" && (listingStatuses as readonly string[]).includes(value);
}

function isTransferMethod(value: unknown): value is SellerOwnListing["transfer_method"] {
  return typeof value === "string" && (transferMethods as readonly string[]).includes(value);
}
