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

export const getHoldings = () => fetchApi<HoldingResponse[]>("/api/holdings");
