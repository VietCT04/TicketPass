"use client";

import { useState } from "react";
import { EventAutocompleteSelector, formatEventDate } from "@/components/EventAutocompleteSelector";
import { EventSummary } from "@/lib/events";

export default function SellPage() {
  const [selectedEvent, setSelectedEvent] = useState<EventSummary | null>(null);

  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto flex w-full max-w-3xl flex-col gap-6">
        <div>
          <p className="text-sm font-medium uppercase text-slate-500">Sell tickets</p>
          <h1 className="mt-3 text-3xl font-semibold text-slate-950">
            Select the event for your ticket
          </h1>
        </div>

        <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <EventAutocompleteSelector selectedEvent={selectedEvent} onSelect={setSelectedEvent} />
        </div>

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
            <p className="mt-3 font-mono text-xs text-slate-600">
              event_id: {selectedEvent.id}
            </p>
          </section>
        ) : null}

        <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-slate-500">Ticket details</p>
          <p className="mt-2 text-sm leading-6 text-slate-600">
            Ticket-specific fields will be added here in issue #6 after event selection is
            complete.
          </p>
        </section>
      </section>
    </main>
  );
}
