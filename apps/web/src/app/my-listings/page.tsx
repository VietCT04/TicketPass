"use client";

import { RequireAuth } from "@/components/RequireAuth";
import { SellerOwnListings } from "@/components/SellerOwnListings";

export default function MyListingsPage() {
  return (
    <RequireAuth returnTo="/my-listings">
      <main className="min-h-screen px-6 py-10">
        <div className="mx-auto w-full max-w-4xl">
          <SellerOwnListings />
        </div>
      </main>
    </RequireAuth>
  );
}
