import { fetchApi } from "../api-client";

export interface HoldingResponse {
  id: number;
  accountId: number;
  accountName: string;
  securityItemId: number;
  ticker: string;
  securityName: string;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  currency: string;
  investedAmount: number;
  evaluatedAmount: number;
  profitLoss: number;
  profitLossRate: number;
}

export interface HoldingCreateRequest {
  userId: number;
  accountId: number;
  securityItemId: number;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  currency: string;
}

export const getHoldings = () => fetchApi<HoldingResponse[]>("/api/holdings");

export const createHolding = (request: HoldingCreateRequest) => 
  fetchApi<HoldingResponse>("/api/holdings", {
    method: "POST",
    body: JSON.stringify(request),
  });
