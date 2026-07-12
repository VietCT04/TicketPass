export type CurrentUser = {
  id: string;
  email: string;
  display_name: string;
  created_at: string;
};

export type AuthResponse = {
  user: CurrentUser;
};

export type AuthState =
  | { status: "loading" }
  | { status: "authenticated"; user: CurrentUser }
  | { status: "unauthenticated" }
  | { status: "error"; message: string };

type SignupInput = {
  email: string;
  password: string;
  displayName: string;
};

type LoginInput = {
  email: string;
  password: string;
};

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export async function getCurrentUser(): Promise<AuthState> {
  const response = await fetch(`${apiBaseUrl}/api/me`, {
    credentials: "include"
  });

  if (response.status === 401) {
    return { status: "unauthenticated" };
  }

  if (!response.ok) {
    return { status: "error", message: await readError(response) };
  }

  const body = (await response.json()) as AuthResponse;
  return { status: "authenticated", user: body.user };
}

export async function signup(input: SignupInput): Promise<AuthResponse> {
  return postJson("/api/auth/signup", {
    email: input.email,
    password: input.password,
    display_name: input.displayName
  });
}

export async function login(input: LoginInput): Promise<AuthResponse> {
  return postJson("/api/auth/login", {
    email: input.email,
    password: input.password
  });
}

export async function logout(): Promise<void> {
  const response = await fetch(`${apiBaseUrl}/api/auth/logout`, {
    method: "POST",
    credentials: "include"
  });

  if (!response.ok) {
    throw new Error(await readError(response));
  }
}

async function postJson(path: string, body: unknown): Promise<AuthResponse> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    throw new Error(await readError(response));
  }

  return (await response.json()) as AuthResponse;
}

async function readError(response: Response): Promise<string> {
  try {
    const body = (await response.json()) as { error?: string };
    return body.error ?? "Request failed";
  } catch {
    return "Request failed";
  }
}
