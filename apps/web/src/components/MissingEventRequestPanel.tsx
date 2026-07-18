"use client";

import Link from "next/link";
import { useEffect, useId, useRef, useState } from "react";
import {
  createEventRequest,
  EventRequestAuthError,
  EventRequestOriginError,
  EventRequestResponse,
  EventRequestUnexpectedError,
  EventRequestValidationError
} from "@/lib/event-requests";

const maxEventNameLength = 255;
const maxVenueLength = 255;
const maxCityLength = 120;
const maxOfficialUrlLength = 2048;

type MissingEventRequestPanelProps = {
  initialEventName: string;
  onClose: () => void;
  onReturnToSearch: () => void;
};

export function MissingEventRequestPanel({
  initialEventName,
  onClose,
  onReturnToSearch
}: MissingEventRequestPanelProps) {
  const eventNameId = useId();
  const startsAtId = useId();
  const offsetId = useId();
  const venueId = useId();
  const cityId = useId();
  const officialUrlId = useId();
  const eventNameInputRef = useRef<HTMLInputElement>(null);
  const [eventName, setEventName] = useState(initialEventName);
  const [startsAt, setStartsAt] = useState("");
  const [offset, setOffset] = useState(getBrowserOffset());
  const [venue, setVenue] = useState("");
  const [city, setCity] = useState("");
  const [officialUrl, setOfficialUrl] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isAuthError, setIsAuthError] = useState(false);
  const [submittedRequest, setSubmittedRequest] = useState<EventRequestResponse | null>(null);

  useEffect(() => {
    eventNameInputRef.current?.focus();
  }, []);

  async function handleSubmit() {
    if (isSubmitting) {
      return;
    }

    const validationError = validateRequest(eventName, startsAt, offset, venue, city, officialUrl);
    if (validationError) {
      setErrorMessage(validationError);
      setIsAuthError(false);
      return;
    }

    setIsSubmitting(true);
    setErrorMessage(null);
    setIsAuthError(false);

    try {
      const request = await createEventRequest({
        eventName: eventName.trim(),
        startsAt: buildOffsetTimestamp(startsAt, offset),
        venue: venue.trim(),
        city: city.trim(),
        officialUrl: officialUrl.trim()
      });
      setSubmittedRequest(request);
    } catch (error) {
      if (error instanceof EventRequestAuthError) {
        setErrorMessage(error.message);
        setIsAuthError(true);
      } else if (
        error instanceof EventRequestValidationError ||
        error instanceof EventRequestOriginError ||
        error instanceof EventRequestUnexpectedError
      ) {
        setErrorMessage(error.message);
        setIsAuthError(false);
      } else {
        setErrorMessage("Could not submit the event request. Please try again.");
        setIsAuthError(false);
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  if (submittedRequest) {
    return (
      <section className="rounded-lg border border-emerald-200 bg-emerald-50 p-5" aria-live="polite">
        <p className="text-sm font-medium text-emerald-800">Event request submitted</p>
        <h2 className="mt-2 text-xl font-semibold text-slate-950">{submittedRequest.event_name}</h2>
        <dl className="mt-4 grid gap-3 text-sm text-slate-700 sm:grid-cols-2">
          <SummaryItem label="Date" value={formatRequestDate(submittedRequest.starts_at)} />
          <SummaryItem label="Venue" value={`${submittedRequest.venue}, ${submittedRequest.city}`} />
          <SummaryItem label="Request ID" value={submittedRequest.id} />
        </dl>
        <p className="mt-4 text-sm text-slate-700">The request is pending review.</p>
        <p className="mt-1 text-sm text-slate-700">No event or listing has been created.</p>
        <button
          type="button"
          onClick={onReturnToSearch}
          className="mt-5 rounded-md bg-slate-950 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-slate-800"
        >
          Return to event search
        </button>
      </section>
    );
  }

  return (
    <section className="rounded-lg border border-amber-200 bg-amber-50 p-5" aria-label="Request a missing event">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-amber-900">Request an event for review</p>
          <p className="mt-2 text-sm leading-6 text-slate-700">
            Verify the local event time and UTC offset before submitting. TicketPass does not infer
            the event timezone.
          </p>
        </div>
        <button
          type="button"
          onClick={onClose}
          disabled={isSubmitting}
          className="text-sm font-medium text-slate-700 underline underline-offset-4 disabled:text-slate-400"
        >
          Close
        </button>
      </div>

      <div
        className="mt-5 flex flex-col gap-4"
        onKeyDown={(event) => {
          if (event.key === "Enter") {
            event.preventDefault();
          }
        }}
      >
        <label htmlFor={eventNameId} className="flex flex-col gap-2 text-sm font-medium text-slate-700">
          Event name
          <input
            ref={eventNameInputRef}
            id={eventNameId}
            value={eventName}
            onChange={(event) => setEventName(event.target.value)}
            required
            maxLength={maxEventNameLength}
            disabled={isSubmitting}
            className="rounded-md border border-slate-300 bg-white px-3 py-2 text-base text-slate-950 outline-none transition focus:border-slate-900 disabled:bg-slate-100"
          />
        </label>

        <div className="grid gap-4 sm:grid-cols-2">
          <label htmlFor={startsAtId} className="flex flex-col gap-2 text-sm font-medium text-slate-700">
            Local event date and time
            <input
              id={startsAtId}
              type="datetime-local"
              value={startsAt}
              onChange={(event) => setStartsAt(event.target.value)}
              required
              disabled={isSubmitting}
              className="rounded-md border border-slate-300 bg-white px-3 py-2 text-base text-slate-950 outline-none transition focus:border-slate-900 disabled:bg-slate-100"
            />
          </label>

          <label htmlFor={offsetId} className="flex flex-col gap-2 text-sm font-medium text-slate-700">
            UTC offset
            <select
              id={offsetId}
              value={offset}
              onChange={(event) => setOffset(event.target.value)}
              disabled={isSubmitting}
              className="rounded-md border border-slate-300 bg-white px-3 py-2 text-base text-slate-950 outline-none transition focus:border-slate-900 disabled:bg-slate-100"
            >
              {utcOffsets.map((value) => (
                <option key={value} value={value}>
                  UTC{value}
                </option>
              ))}
            </select>
          </label>
        </div>

        <label htmlFor={venueId} className="flex flex-col gap-2 text-sm font-medium text-slate-700">
          Venue
          <input
            id={venueId}
            value={venue}
            onChange={(event) => setVenue(event.target.value)}
            required
            maxLength={maxVenueLength}
            disabled={isSubmitting}
            className="rounded-md border border-slate-300 bg-white px-3 py-2 text-base text-slate-950 outline-none transition focus:border-slate-900 disabled:bg-slate-100"
          />
        </label>

        <label htmlFor={cityId} className="flex flex-col gap-2 text-sm font-medium text-slate-700">
          City
          <input
            id={cityId}
            value={city}
            onChange={(event) => setCity(event.target.value)}
            required
            maxLength={maxCityLength}
            disabled={isSubmitting}
            className="rounded-md border border-slate-300 bg-white px-3 py-2 text-base text-slate-950 outline-none transition focus:border-slate-900 disabled:bg-slate-100"
          />
        </label>

        <label htmlFor={officialUrlId} className="flex flex-col gap-2 text-sm font-medium text-slate-700">
          Official event URL <span className="font-normal">(optional)</span>
          <input
            id={officialUrlId}
            type="url"
            value={officialUrl}
            onChange={(event) => setOfficialUrl(event.target.value)}
            maxLength={maxOfficialUrlLength}
            disabled={isSubmitting}
            className="rounded-md border border-slate-300 bg-white px-3 py-2 text-base text-slate-950 outline-none transition focus:border-slate-900 disabled:bg-slate-100"
            placeholder="https://example.com/event"
          />
        </label>

        {errorMessage ? (
          <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700" aria-live="polite">
            <p>{errorMessage}</p>
            {isAuthError ? (
              <Link
                href="/login?next=%2Fsell"
                className="mt-2 inline-flex font-medium text-red-900 underline underline-offset-4"
              >
                Log in
              </Link>
            ) : null}
          </div>
        ) : null}

        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            onClick={handleSubmit}
            disabled={isSubmitting}
            className="rounded-md bg-slate-950 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
          >
            {isSubmitting ? "Submitting request..." : "Request event review"}
          </button>
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="rounded-md border border-slate-300 bg-white px-4 py-2.5 text-sm font-medium text-slate-800 transition hover:bg-slate-50 disabled:text-slate-400"
          >
            Cancel
          </button>
        </div>
      </div>
    </section>
  );
}

function validateRequest(
  eventName: string,
  startsAt: string,
  offset: string,
  venue: string,
  city: string,
  officialUrl: string
) {
  if (eventName.trim().length === 0 || venue.trim().length === 0 || city.trim().length === 0) {
    return "Enter an event name, venue, and city.";
  }

  if (
    eventName.trim().length > maxEventNameLength ||
    venue.trim().length > maxVenueLength ||
    city.trim().length > maxCityLength ||
    officialUrl.trim().length > maxOfficialUrlLength
  ) {
    return "One or more values are too long.";
  }

  const timestamp = buildOffsetTimestamp(startsAt, offset);
  if (!isOffsetTimestamp(timestamp) || Number.isNaN(Date.parse(timestamp)) || new Date(timestamp) <= new Date()) {
    return "Enter a future event date and time with a UTC offset.";
  }

  if (officialUrl.trim().length > 0 && !isOfficialUrl(officialUrl.trim())) {
    return "Enter an absolute HTTPS official event URL without credentials.";
  }

  return null;
}

function buildOffsetTimestamp(localDateTime: string, offset: string) {
  return `${localDateTime.length === 16 ? `${localDateTime}:00` : localDateTime}${offset}`;
}

function isOffsetTimestamp(value: string) {
  return /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}[+-]\d{2}:\d{2}$/.test(value);
}

function isOfficialUrl(value: string) {
  try {
    const url = new URL(value);
    return url.protocol === "https:" && url.hostname.length > 0 && url.username.length === 0 && url.password.length === 0;
  } catch {
    return false;
  }
}

function getBrowserOffset() {
  const minutes = -new Date().getTimezoneOffset();
  return formatOffset(Math.max(-720, Math.min(840, minutes)));
}

function formatOffset(totalMinutes: number) {
  const sign = totalMinutes >= 0 ? "+" : "-";
  const absoluteMinutes = Math.abs(totalMinutes);
  return `${sign}${Math.floor(absoluteMinutes / 60).toString().padStart(2, "0")}:${(absoluteMinutes % 60).toString().padStart(2, "0")}`;
}

const utcOffsets = Array.from({ length: 105 }, (_, index) => formatOffset(-720 + index * 15));

function formatRequestDate(value: string) {
  return new Intl.DateTimeFormat(undefined, {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
    timeZoneName: "short"
  }).format(new Date(value));
}

function SummaryItem({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="font-medium text-slate-500">{label}</dt>
      <dd className="mt-1 break-all text-slate-900">{value}</dd>
    </div>
  );
}
