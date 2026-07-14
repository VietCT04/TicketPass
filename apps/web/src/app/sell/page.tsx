"use client";

import { useState } from "react";
import { SellerListingForm } from "@/components/SellerListingForm";
import { EventSummary } from "@/lib/events";

export default function SellPage() {
  const [selectedEvent, setSelectedEvent] = useState<EventSummary | null>(null);

  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto flex w-full max-w-3xl flex-col gap-6">
        <div>
          <p className="text-sm font-medium uppercase text-slate-500">Sell tickets</p>
          <h1 className="mt-3 text-3xl font-semibold text-slate-950">
            Create a ticket listing
          </h1>
        </div>

        <SellerListingForm selectedEvent={selectedEvent} onSelectedEventChange={setSelectedEvent} />
      </section>
    </main>
  );
}
