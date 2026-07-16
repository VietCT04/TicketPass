export type EventSummary = {
  id: string;
  name: string;
  starts_at: string;
  venue: string;
  city: string;
};

export type BrowseEventSummary = EventSummary & {
  image_url: string | null;
  lowest_price_minor: number;
  currency: "VND";
  available_listing_count: number;
};

export type EventBrowsePagination = {
  page: number;
  page_size: number;
  total_items: number;
  total_pages: number;
};

export type EventBrowseResponse = {
  events: BrowseEventSummary[];
  pagination: EventBrowsePagination;
};

export type EventListingSummary = {
  id: string;
  ticket_type: string;
  seat_info: string;
  event_platform: string;
  asking_price_minor: number;
  currency: "VND";
  transfer_method: string;
};

export type EventDetailResponse = {
  event: EventSummary & { image_url: string | null };
  listings: EventListingSummary[];
  pagination: EventBrowsePagination;
};

type EventAutocompleteResponse = {
  events: EventSummary[];
};

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export class EventAutocompleteAuthError extends Error {
  constructor() {
    super("Sign in to search and select an event.");
    this.name = "EventAutocompleteAuthError";
  }
}

export class EventDetailUnavailableError extends Error {
  constructor() {
    super("Event unavailable");
    this.name = "EventDetailUnavailableError";
  }
}

export async function searchEvents(
  query: string,
  signal?: AbortSignal
): Promise<EventSummary[]> {
  const params = new URLSearchParams({ q: query });
  const response = await fetch(`${apiBaseUrl}/api/events/autocomplete?${params}`, {
    credentials: "include",
    signal
  });

  if (response.status === 401) {
    throw new EventAutocompleteAuthError();
  }

  if (!response.ok) {
    throw new Error(await readError(response, "Could not search events"));
  }

  const body = (await response.json()) as EventAutocompleteResponse;
  return body.events;
}

export async function browseEvents(page: number): Promise<EventBrowseResponse> {
  const params = new URLSearchParams({
    page: page.toString(),
    page_size: "20"
  });

  const response = await fetch(`${apiBaseUrl}/api/events?${params}`, {
    cache: "no-store"
  });

  if (!response.ok) {
    throw new Error(await readError(response, "Could not load events"));
  }

  return (await response.json()) as EventBrowseResponse;
}

export async function getEventDetail(
  eventId: string,
  page: number
): Promise<EventDetailResponse> {
  const params = new URLSearchParams({ page: page.toString(), page_size: "20" });
  const response = await fetch(`${apiBaseUrl}/api/events/${eventId}?${params}`, {
    cache: "no-store"
  });

  if (response.status === 400 || response.status === 404) {
    throw new EventDetailUnavailableError();
  }

  if (!response.ok) {
    throw new Error(await readError(response, "Could not load event"));
  }

  return (await response.json()) as EventDetailResponse;
}

async function readError(response: Response, fallbackMessage: string): Promise<string> {
  try {
    const body = (await response.json()) as { error?: string };
    return body.error ?? fallbackMessage;
  } catch {
    return fallbackMessage;
  }
}
