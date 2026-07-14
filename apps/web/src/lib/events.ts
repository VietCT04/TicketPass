export type EventSummary = {
  id: string;
  name: string;
  starts_at: string;
  venue: string;
  city: string;
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
    throw new Error(await readError(response));
  }

  const body = (await response.json()) as EventAutocompleteResponse;
  return body.events;
}

async function readError(response: Response): Promise<string> {
  try {
    const body = (await response.json()) as { error?: string };
    return body.error ?? "Could not search events";
  } catch {
    return "Could not search events";
  }
}
