import { EventSummary } from "@/lib/events";

export type CreateListingInput = {
  eventId: string;
  eventPlatform: string;
  seatInfo: string;
  ticketType: string;
  askingPriceMinor: number;
  isTransferableConfirmed: boolean;
  publicNotes: string;
};

export type ListingResponse = {
  id: string;
  seller_id: string;
  event: EventSummary;
  event_platform: string;
  seat_info: string;
  ticket_type: string;
  quantity: number;
  currency: "VND";
  asking_price_minor: number;
  transfer_method: "PLATFORM_TRANSFER";
  is_transferable_confirmed: boolean;
  status: string;
  public_notes: string | null;
  created_at: string;
  updated_at: string;
};

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export class ListingAuthError extends Error {
  constructor() {
    super("Sign in to create a listing.");
    this.name = "ListingAuthError";
  }
}

export async function createListing(input: CreateListingInput): Promise<ListingResponse> {
  const response = await fetch(`${apiBaseUrl}/api/listings`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      event_id: input.eventId,
      event_platform: input.eventPlatform,
      seat_info: input.seatInfo,
      ticket_type: input.ticketType,
      asking_price_minor: input.askingPriceMinor,
      transfer_method: "PLATFORM_TRANSFER",
      is_transferable_confirmed: input.isTransferableConfirmed,
      public_notes: input.publicNotes
    })
  });

  if (response.status === 401) {
    throw new ListingAuthError();
  }

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  return (await response.json()) as ListingResponse;
}

async function readError(response: Response): Promise<string> {
  try {
    const body = (await response.json()) as { error?: string };
    return body.error ?? "Could not create listing";
  } catch {
    return "Could not create listing";
  }
}
