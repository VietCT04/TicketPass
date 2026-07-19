const allowedAuthRedirectTargets = new Set(["/sell", "/my-listings"]);
const eventDetailPathPattern = /^\/events\/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})(?:\?page=([1-9][0-9]*))?$/i;
const checkoutPathPattern = /^\/checkout\/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})$/i;

export function getSafeAuthRedirectTarget(next: string | null): string {
  if (!next) {
    return "/";
  }

  if (
    allowedAuthRedirectTargets.has(next) ||
    eventDetailPathPattern.test(next) ||
    checkoutPathPattern.test(next)
  ) {
    return next;
  }

  return "/";
}

export function buildAuthHref(path: "/login" | "/signup", next: string | null): string {
  const safeNext = getSafeAuthRedirectTarget(next);

  if (safeNext === "/") {
    return path;
  }

  return `${path}?next=${encodeURIComponent(safeNext)}`;
}
