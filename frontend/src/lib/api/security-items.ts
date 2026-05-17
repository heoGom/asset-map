import { fetchApi } from "../api-client";

export interface SecurityItemResponse {
  id: number;
  ticker: string;
  name: string;
  market: string;
  country: string;
  currency: string;
  securityType: string;
}

export interface SecurityItemCreateRequest {
  ticker: string;
  name: string;
  market?: string;
  country?: string;
  currency: string;
  securityType: string;
}

export const getSecurityItems = () => fetchApi<SecurityItemResponse[]>("/api/securities");

export const createSecurityItem = (request: SecurityItemCreateRequest) =>
  fetchApi<SecurityItemResponse>("/api/securities", {
    method: "POST",
    body: JSON.stringify(request),
  });

export const updateSecurityItem = (securityId: number, request: Partial<SecurityItemCreateRequest>) =>
  fetchApi<SecurityItemResponse>(`/api/securities/${securityId}`, {
    method: "PATCH",
    body: JSON.stringify(request),
  });
