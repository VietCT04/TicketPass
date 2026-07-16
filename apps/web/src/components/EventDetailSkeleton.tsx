export function EventDetailSkeleton() {
  return (
    <div className="flex flex-col gap-8">
      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
        <div className="aspect-[3/1] animate-pulse bg-slate-200" />
        <div className="space-y-4 p-6">
          <div className="h-8 w-2/3 animate-pulse rounded bg-slate-200" />
          <div className="h-5 w-1/2 animate-pulse rounded bg-slate-200" />
          <div className="h-5 w-3/4 animate-pulse rounded bg-slate-200" />
        </div>
      </div>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {Array.from({ length: 3 }).map((_, index) => (
          <div
            key={index}
            className="h-52 animate-pulse rounded-lg border border-slate-200 bg-slate-200"
          />
        ))}
      </div>
    </div>
  );
}
