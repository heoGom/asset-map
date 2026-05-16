import { fetchApi } from "../api-client";

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

export const createAccount = (request: AccountCreateRequest) => 
  fetchApi<AccountResponse>("/api/accounts", {
    method: "POST",
    body: JSON.stringify(request),
  });

export const deleteAccount = (accountId: number) => 
  fetchApi<void>(`/api/accounts/${accountId}`, {
    method: "DELETE",
  });
