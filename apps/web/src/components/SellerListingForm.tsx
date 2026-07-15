"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import {
  EventAutocompleteSelector,
  formatEventDate
} from "@/components/EventAutocompleteSelector";
import { EventSummary } from "@/lib/events";
import { createListing, ListingAuthError, ListingResponse } from "@/lib/listings";

const maxEventPlatformLength = 120;
const maxSeatInfoLength = 255;
const maxTicketTypeLength = 120;
const maxPublicNotesLength = 1000;

type SellerListingFormProps = {
  selectedEvent: EventSummary | null;
  onSelectedEventChange: (event: EventSummary | null) => void;
};

export function SellerListingForm({
  selectedEvent,
  onSelectedEventChange
}: SellerListingFormProps) {
  const [eventPlatform, setEventPlatform] = useState("");
  const [seatInfo, setSeatInfo] = useState("");
  const [ticketType, setTicketType] = useState("");
  const [askingPrice, setAskingPrice] = useState("");
  const [publicNotes, setPublicNotes] = useState("");
  const [isTransferableConfirmed, setIsTransferableConfirmed] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isAuthError, setIsAuthError] = useState(false);
  const [createdListing, setCreatedListing] = useState<ListingResponse | null>(null);
  const [selectorKey, setSelectorKey] = useState(0);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (isSubmitting) {
      return;
    }

    const validationError = validateForm();
    if (validationError) {
      setErrorMessage(validationError);
      setIsAuthError(false);
      return;
    }

    setIsSubmitting(true);
    setErrorMessage(null);
    setIsAuthError(false);

    try {
      const listing = await createListing({
        eventId: selectedEvent!.id,
        eventPlatform: eventPlatform.trim(),
        seatInfo: seatInfo.trim(),
        ticketType: ticketType.trim(),
        askingPriceMinor: Number(askingPrice),
        isTransferableConfirmed,
        publicNotes: publicNotes.trim()
      });

      setCreatedListing(listing);
    } catch (error) {
      if (error instanceof ListingAuthError) {
        setErrorMessage(error.message);
        setIsAuthError(true);
      } else {
        setErrorMessage(error instanceof Error ? error.message : "Could not create listing");
        setIsAuthError(false);
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  function validateForm() {
    if (!selectedEvent) {
      return "Select an existing event before creating a listing.";
    }

    if (eventPlatform.trim().length === 0) {
      return "Enter the ticket platform or provider.";
    }

    if (seatInfo.trim().length === 0) {
      return "Enter the seat information.";
    }

    if (ticketType.trim().length === 0) {
      return "Enter the ticket type.";
    }

    if (!/^[1-9]\d*$/.test(askingPrice.trim())) {
      return "Enter a positive whole-VND asking price.";
    }

    if (!isTransferableConfirmed) {
      return "Confirm that this ticket can be transferred to a buyer.";
    }

    return null;
  }

  function handleCreateAnother() {
    onSelectedEventChange(null);
    setEventPlatform("");
    setSeatInfo("");
    setTicketType("");
    setAskingPrice("");
    setPublicNotes("");
    setIsTransferableConfirmed(false);
    setErrorMessage(null);
    setIsAuthError(false);
    setCreatedListing(null);
    setSelectorKey((current) => current + 1);
  }

  if (createdListing) {
    return (
      <div className="flex flex-col gap-6">
        <section className="rounded-lg border border-emerald-200 bg-emerald-50 p-5">
          <p className="text-sm font-medium text-emerald-800">Listing created</p>
          <h2 className="mt-2 text-xl font-semibold text-slate-950">
            {createdListing.event.name}
          </h2>
          <p className="mt-2 text-sm text-slate-700">
            Listing ID: <span className="font-mono">{createdListing.id}</span>
          </p>
          <dl className="mt-4 grid gap-3 text-sm text-slate-700 sm:grid-cols-2">
            <SummaryItem label="Event" value={createdListing.event.name} />
            <SummaryItem label="Date" value={formatEventDate(createdListing.event.starts_at)} />
            <SummaryItem
              label="Venue"
              value={`${createdListing.event.venue}, ${createdListing.event.city}`}
            />
            <SummaryItem label="Platform" value={createdListing.event_platform} />
            <SummaryItem label="Seat" value={createdListing.seat_info} />
            <SummaryItem label="Ticket type" value={createdListing.ticket_type} />
            <SummaryItem
              label="Price"
              value={`${createdListing.asking_price_minor.toLocaleString()} VND`}
            />
            <SummaryItem label="Status" value={createdListing.status} />
          </dl>
        </section>

        <button
          type="button"
          onClick={handleCreateAnother}
          className="self-start rounded-md bg-slate-950 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-slate-800"
        >
          Create another listing
        </button>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-6">
      <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <EventAutocompleteSelector
          key={selectorKey}
          selectedEvent={selectedEvent}
          onSelect={onSelectedEventChange}
        />
      </section>

      {selectedEvent ? (
        <section className="rounded-lg border border-emerald-200 bg-emerald-50 p-5">
          <p className="text-sm font-medium text-emerald-800">Selected event</p>
          <h2 className="mt-2 text-xl font-semibold text-slate-950">{selectedEvent.name}</h2>
          <p className="mt-2 text-sm text-slate-700">
            {formatEventDate(selectedEvent.starts_at)}
          </p>
          <p className="mt-1 text-sm text-slate-700">
            {selectedEvent.venue}, {selectedEvent.city}
          </p>
          <p className="mt-3 font-mono text-xs text-slate-600">event_id: {selectedEvent.id}</p>
        </section>
      ) : null}

      <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <div>
          <p className="text-sm font-medium text-slate-500">Ticket details</p>
          <p className="mt-2 text-sm leading-6 text-slate-600">
            TicketPass supports one ticket per MVP listing.
          </p>
        </div>

        <div className="mt-5 flex flex-col gap-4">
          <TextField
            label="Event platform"
            value={eventPlatform}
            onChange={setEventPlatform}
            maxLength={maxEventPlatformLength}
            disabled={isSubmitting}
            placeholder="Ticketmaster"
          />

          <TextField
            label="Seat information"
            value={seatInfo}
            onChange={setSeatInfo}
            maxLength={maxSeatInfoLength}
            disabled={isSubmitting}
            placeholder="Section 101, Row B, Seat 12"
          />

          <TextField
            label="Ticket type"
            value={ticketType}
            onChange={setTicketType}
            maxLength={maxTicketTypeLength}
            disabled={isSubmitting}
            placeholder="General Admission"
          />

          <label className="flex flex-col gap-2 text-sm font-medium text-slate-700">
            Asking price
            <div className="flex overflow-hidden rounded-md border border-slate-300 bg-white focus-within:border-slate-900">
              <span className="border-r border-slate-200 px-3 py-2 text-base text-slate-600">
                VND
              </span>
              <input
                value={askingPrice}
                onChange={(event) => setAskingPrice(event.target.value)}
                inputMode="numeric"
                pattern="[0-9]*"
                required
                disabled={isSubmitting}
                className="min-w-0 flex-1 px-3 py-2 text-base text-slate-950 outline-none disabled:bg-slate-100"
                placeholder="1250000"
              />
            </div>
          </label>

          <label className="flex flex-col gap-2 text-sm font-medium text-slate-700">
            Public notes
            <textarea
              value={publicNotes}
              onChange={(event) => setPublicNotes(event.target.value)}
              maxLength={maxPublicNotesLength}
              disabled={isSubmitting}
              rows={4}
              className="rounded-md border border-slate-300 px-3 py-2 text-base text-slate-950 outline-none transition focus:border-slate-900 disabled:bg-slate-100"
              placeholder="Mobile transfer available after purchase."
            />
            <span className="text-sm font-normal text-slate-600">
              Do not enter QR codes, barcodes, ticket links, platform credentials, or other usable
              ticket data.
            </span>
          </label>

          <label className="flex gap-3 rounded-md border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
            <input
              type="checkbox"
              checked={isTransferableConfirmed}
              onChange={(event) => setIsTransferableConfirmed(event.target.checked)}
              required
              disabled={isSubmitting}
              className="mt-1"
            />
            <span>
              <span className="font-medium text-slate-900">
                I confirm that this ticket can be transferred to a buyer.
              </span>{" "}
              This is your declaration and does not mean TicketPass has independently verified
              transferability.
            </span>
          </label>

          {errorMessage ? (
            <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
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

          <button
            type="submit"
            disabled={isSubmitting}
            className="rounded-md bg-slate-950 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-400"
          >
            {isSubmitting ? "Creating listing..." : "Create listing"}
          </button>
        </div>
      </section>
    </form>
  );
}

type TextFieldProps = {
  label: string;
  value: string;
  onChange: (value: string) => void;
  maxLength: number;
  disabled: boolean;
  placeholder: string;
};

function TextField({ label, value, onChange, maxLength, disabled, placeholder }: TextFieldProps) {
  return (
    <label className="flex flex-col gap-2 text-sm font-medium text-slate-700">
      {label}
      <input
        value={value}
        onChange={(event) => onChange(event.target.value)}
        required
        maxLength={maxLength}
        disabled={disabled}
        className="rounded-md border border-slate-300 px-3 py-2 text-base text-slate-950 outline-none transition focus:border-slate-900 disabled:bg-slate-100"
        placeholder={placeholder}
      />
    </label>
  );
}

function SummaryItem({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="font-medium text-slate-500">{label}</dt>
      <dd className="mt-1 text-slate-900">{value}</dd>
    </div>
  );
}
