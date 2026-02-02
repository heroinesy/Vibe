const resolveApiBase = () =>
  process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080";

export async function POST(request: Request) {
  try {
    const body = await request.text();
    const response = await fetch(`${resolveApiBase()}/api/v1/code/review`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body,
    });

    const payload = await response.text();
    return new Response(payload, {
      status: response.status,
      headers: {
        "Content-Type":
          response.headers.get("Content-Type") ?? "application/json",
      },
    });
  } catch (error) {
    const message =
      error instanceof Error ? error.message : "Proxy request failed.";
    return new Response(message, {
      status: 502,
      headers: {
        "Content-Type": "text/plain",
      },
    });
  }
}
