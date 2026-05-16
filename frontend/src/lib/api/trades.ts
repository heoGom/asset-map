import { fetchApi } from "../api-client";

export enum TradeType {
  INITIAL = "INITIAL",
  BUY = "BUY",
  SELL = "SELL",
}

export enum TransactionSource {
  MANUAL = "MANUAL",
  INITIAL = "INITIAL",
  CSV = "CSV",
  BROKER_API = "BROKER_API",
}

export interface TradeTransactionResponse {
  id: number;
  userId: number;
  accountId: number;
  accountName: string;
  securityItemId: number;
  ticker: string;
  securityName: string;
  tradeDate: string;
  tradeType: TradeType;
  quantity: number;
  price: number;
  grossAmount: number;
  fee: number;
  tax: number;
  netAmount: number;
  currency: string;
  source: TransactionSource;
  memo?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TradeTransactionCreateRequest {
  userId: number;
  accountId: number;
  securityItemId: number;
  tradeDate: string;
  tradeType: TradeType;
  quantity: number;
  price: number;
  fee?: number;
  tax?: number;
  currency: string;
  source: TransactionSource;
  memo?: string;
}

export const getTrades = (userId: number) =>
  fetchApi<TradeTransactionResponse[]>(`/api/trades?userId=${userId}`);

export const createTrade = (request: TradeTransactionCreateRequest) =>
  fetchApi<TradeTransactionResponse>("/api/trades", {
    method: "POST",
    body: JSON.stringify(request),
  });
