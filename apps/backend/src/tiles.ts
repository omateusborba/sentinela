const TILE_UA = "Sentinela/1.0 (sentinela-fire-monitor; contact=local)";

const TILE_SOURCES = [
  (z: string, x: string, y: string) =>
    `https://tile.openstreetmap.org/${z}/${x}/${y}.png`,
  (z: string, x: string, y: string) =>
    `https://a.tile.openstreetmap.org/${z}/${x}/${y}.png`,
  (z: string, x: string, y: string) =>
    `https://basemaps.cartocdn.com/rastertiles/voyager/${z}/${x}/${y}.png`,
];

const TILE_HEADERS = {
  "Content-Type": "image/png",
  "Cache-Control": "public, max-age=86400",
  "Access-Control-Allow-Origin": "*",
};

export async function fetchOsmTile(
  z: string,
  x: string,
  y: string,
): Promise<Response> {
  const yClean = y.replace(/\.png$/i, "");

  for (const buildUrl of TILE_SOURCES) {
    const url = buildUrl(z, x, yClean);
    const response = await fetch(url, {
      headers: { "User-Agent": TILE_UA },
    });

    if (!response.ok) continue;

    const body = await response.arrayBuffer();
    if (body.byteLength < 200) continue;

    return new Response(body, { status: 200, headers: TILE_HEADERS });
  }

  return new Response(null, { status: 502 });
}