const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type CreateEventRequestInput = {
  eventName: string;
  startsAt: string;
  venue: string;
  city: string;
  officialUrl: string;
};

export type EventRequestResponse = {
  id: string;
  status: "PENDING";
  event_name: string;
  starts_at: string;
  venue: string;
  city: string;
  official_url: string | null;
  created_at: string;
  updated_at: string;
};

export class EventRequestAuthError extends Error {
  constructor() {
    super("Sign in to request an event.");
    this.name = "EventRequestAuthError";
  }
}

export class EventRequestOriginError extends Error {
  constructor() {
    super("This request was rejected from the current origin. Please try again from TicketPass.");
    this.name = "EventRequestOriginError";
  }
}

export class EventRequestValidationError extends Error {
  constructor() {
    super("Check the event request details and try again.");
    this.name = "EventRequestValidationError";
  }
}

export class EventRequestUnexpectedError extends Error {
  constructor() {
    super("Could not submit the event request. Please try again.");
    this.name = "EventRequestUnexpectedError";
  }
}

export async function createEventRequest(
  input: CreateEventRequestInput
): Promise<EventRequestResponse> {
  let response: Response;

  try {
    response = await fetch(`${apiBaseUrl}/api/event-requests`, {
      method: "POST",
      credentials: "include",
      cache: "no-store",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        event_name: input.eventName,
        starts_at: input.startsAt,
        venue: input.venue,
        city: input.city,
        official_url: input.officialUrl || undefined
      })
    });
  } catch {
    throw new EventRequestUnexpectedError();
  }

  if (response.status === 401) {
    throw new EventRequestAuthError();
  }

  if (response.status === 403) {
    throw new EventRequestOriginError();
  }

  if (response.status === 400) {
    throw new EventRequestValidationError();
  }

  if (response.status !== 200 && response.status !== 201) {
    throw new EventRequestUnexpectedError();
  }

  try {
    return parseEventRequestResponse(await response.json());
  } catch {
    throw new EventRequestUnexpectedError();
  }
}

function parseEventRequestResponse(value: unknown): EventRequestResponse {
  if (!isRecord(value) || !hasOnlySafeFields(value)) {
    throw new Error("Invalid event request response");
  }

  if (
    !isNonBlankString(value.id) ||
    value.status !== "PENDING" ||
    !isNonBlankString(value.event_name) ||
    !isIsoTimestamp(value.starts_at) ||
    !isNonBlankString(value.venue) ||
    !isNonBlankString(value.city) ||
    !(value.official_url === null || isNonBlankString(value.official_url)) ||
    !isIsoTimestamp(value.created_at) ||
    !isIsoTimestamp(value.updated_at)
  ) {
    throw new Error("Invalid event request response");
  }

  return value as EventRequestResponse;
}

function hasOnlySafeFields(value: Record<string, unknown>) {
  const safeFields = [
    "id",
    "status",
    "event_name",
    "starts_at",
    "venue",
    "city",
    "official_url",
    "created_at",
    "updated_at"
  ];

  return Object.keys(value).length === safeFields.length && Object.keys(value).every((key) => safeFields.includes(key));
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isNonBlankString(value: unknown): value is string {
  return typeof value === "string" && value.trim().length > 0;
}

function isIsoTimestamp(value: unknown): value is string {
  return isNonBlankString(value) && !Number.isNaN(Date.parse(value));
}
