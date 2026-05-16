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

export const getSecurityItems = () => fetchApi<SecurityItemResponse[]>("/api/securities");
