const allowedAuthRedirectTargets = new Set(["/sell"]);

export function getSafeAuthRedirectTarget(next: string | null): "/" | "/sell" {
  if (!next) {
    return "/";
  }

  return allowedAuthRedirectTargets.has(next) ? (next as "/sell") : "/";
}

export function buildAuthHref(path: "/login" | "/signup", next: string | null): string {
  const safeNext = getSafeAuthRedirectTarget(next);

  if (safeNext === "/") {
    return path;
  }

  return `${path}?next=${encodeURIComponent(safeNext)}`;
}
