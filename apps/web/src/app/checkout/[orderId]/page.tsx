import Link from "next/link";
import { CheckoutPanel } from "@/components/CheckoutPanel";
import { RequireAuth } from "@/components/RequireAuth";
import { isCheckoutOrderId } from "@/lib/checkout";

type CheckoutPageProps = {
  params: Promise<{ orderId: string }>;
};

export default async function CheckoutPage({ params }: CheckoutPageProps) {
  const { orderId } = await params;

  if (!isCheckoutOrderId(orderId)) {
    return <CheckoutUnavailable />;
  }

  return (
    <RequireAuth returnTo={`/checkout/${orderId}`}>
      <CheckoutPanel orderId={orderId} />
    </RequireAuth>
  );
}

function CheckoutUnavailable() {
  return (
    <main className="min-h-screen px-6 py-10">
      <section className="mx-auto max-w-2xl rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <p className="text-sm font-medium uppercase text-slate-500">Checkout unavailable</p>
        <h1 className="mt-3 text-3xl font-semibold text-slate-950">This checkout cannot be viewed.</h1>
        <p className="mt-3 text-sm text-slate-600">Return to the marketplace to find available tickets.</p>
        <Link
          href="/"
          className="mt-6 inline-flex rounded-md bg-slate-950 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-800"
        >
          Browse events
        </Link>
      </section>
    </main>
  );
}
