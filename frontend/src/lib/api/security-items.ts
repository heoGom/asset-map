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

export const searchSecurityItems = (keyword: string) => 
  fetchApi<SecurityItemResponse[]>(`/api/securities/search?keyword=${keyword}`);
