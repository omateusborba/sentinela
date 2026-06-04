/** Serve static files from the ASSETS binding (./static). */
export async function serveStatic(
  assets: Fetcher,
  pathname: string,
  incoming: Request,
): Promise<Response> {
  const base = new URL(incoming.url);
  const url = new URL(pathname, base.origin);
  return assets.fetch(new Request(url.toString(), incoming));
}
