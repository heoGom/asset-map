import { fetchApi } from "../api-client";
import { DividendPaymentResponse } from "./dividends";
import { HoldingResponse } from "./holdings";
import { TradeTransactionResponse } from "./trades";

export enum AccountType {
  GENERAL = "GENERAL",
  CASH = "CASH",
  ISA = "ISA",
  PENSION = "PENSION",
  IRP = "IRP",
  CMA = "CMA",
  SAVINGS = "SAVINGS",
  CRYPTO = "CRYPTO",
}

export interface AccountResponse {
  id: number;
  userId: number;
  name: string;
  brokerName: string;
  accountType: AccountType;
  currency: string;
  memo: string;
}

export interface AccountCreateRequest {
  name: string;
  brokerName?: string;
  accountType: AccountType;
  currency: string;
  memo?: string;
}

export const getAccounts = () => fetchApi<AccountResponse[]>("/api/accounts");

export const getAccount = (accountId: number) => fetchApi<AccountResponse>(`/api/accounts/${accountId}`);

export const getAccountHoldings = (accountId: number) =>
  fetchApi<HoldingResponse[]>(`/api/accounts/${accountId}/holdings`);

export const getAccountTrades = (accountId: number) =>
  fetchApi<TradeTransactionResponse[]>(`/api/accounts/${accountId}/trades`);

export const getAccountDividendPayments = (accountId: number) =>
  fetchApi<DividendPaymentResponse[]>(`/api/accounts/${accountId}/dividend-payments`);

export const createAccount = (request: AccountCreateRequest) => 
  fetchApi<AccountResponse>("/api/accounts", {
    method: "POST",
    body: JSON.stringify(request),
  });

export const deleteAccount = (accountId: number) => 
  fetchApi<void>(`/api/accounts/${accountId}`, {
    method: "DELETE",
  });
