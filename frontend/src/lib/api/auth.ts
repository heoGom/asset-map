import { fetchApi } from "../api-client";

export interface UserResponse {
  id: number;
  email: string;
  nickname: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: "Bearer";
  user: UserResponse;
}

export interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export const signup = (request: SignupRequest) =>
  fetchApi<AuthResponse>("/api/auth/signup", {
    method: "POST",
    body: JSON.stringify(request),
  });

export const login = (request: LoginRequest) =>
  fetchApi<AuthResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(request),
  });

export const getMe = () => fetchApi<UserResponse>("/api/users/me");
