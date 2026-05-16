import { fetchApi } from "../api-client";

export enum AccountType {
  CASH = "CASH",
  STOCK = "STOCK",
  ISA = "ISA",
  PENSION = "PENSION",
  IRP = "IRP",
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
  userId: number;
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
