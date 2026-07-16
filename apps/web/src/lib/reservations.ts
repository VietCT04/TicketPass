export type ListingReservation = {
  id: string;
  listing_id: string;
  status: "ACTIVE";
  expires_at: string;
};

type ReservationResponse = {
  reservation: ListingReservation;
};

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export class ReservationRequestError extends Error {
  constructor(
    readonly status: number,
    message: string
  ) {
    super(message);
    this.name = "ReservationRequestError";
  }
}

export async function reserveListing(listingId: string): Promise<ListingReservation> {
  const response = await fetch(`${apiBaseUrl}/api/listings/${listingId}/reservations`, {
    method: "POST",
    credentials: "include"
  });

  if (response.status !== 200 && response.status !== 201) {
    throw new ReservationRequestError(
      response.status,
      await readError(response, "Could not reserve this ticket")
    );
  }

  const reservation = await readReservation(response, listingId);

  if (!reservation) {
    throw new ReservationRequestError(0, "Could not reserve this ticket");
  }

  return reservation;
}

async function readReservation(
  response: Response,
  listingId: string
): Promise<ListingReservation | null> {
  try {
    const body = (await response.json()) as Partial<ReservationResponse>;
    const reservation = body.reservation;

    if (
      !reservation ||
      typeof reservation.id !== "string" ||
      typeof reservation.listing_id !== "string" ||
      !uuidPattern.test(reservation.id) ||
      reservation.listing_id !== listingId ||
      reservation.status !== "ACTIVE" ||
      typeof reservation.expires_at !== "string" ||
      Number.isNaN(Date.parse(reservation.expires_at))
    ) {
      return null;
    }

    return reservation;
  } catch {
    return null;
  }
}

async function readError(response: Response, fallbackMessage: string): Promise<string> {
  try {
    const body = (await response.json()) as { error?: unknown };
    return typeof body.error === "string" && body.error.trim()
      ? body.error
      : fallbackMessage;
  } catch {
    return fallbackMessage;
  }
}
