import { fetchApi } from "../api-client";

export interface MarketPriceResponse {
  id: number;
  securityItemId: number;
  ticker: string;
  securityName: string;
  priceDate: string;
  closePrice: number;
  currentPrice: number;
  changeAmount: number;
  changeRate: number;
  volume?: number;
  source: "MANUAL" | "PUBLIC_DATA" | "KRX" | "BROKER_API";
}

export interface MarketPriceCreateRequest {
  securityItemId: number;
  priceDate: string;
  closePrice: number;
  currentPrice: number;
  changeAmount?: number;
  changeRate?: number;
  volume?: number;
  source: "MANUAL";
}

export const createMarketPrice = (request: MarketPriceCreateRequest) =>
  fetchApi<MarketPriceResponse>("/api/market-prices", {
    method: "POST",
    body: JSON.stringify(request),
  });
