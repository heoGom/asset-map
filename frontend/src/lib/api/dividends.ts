import { fetchApi } from "../api-client";

export interface DividendSummary {
  expectedAnnualDividend: number;
  monthlyAverageDividend: number;
  dividendYieldOnEvaluated: number;
  dividendYieldOnInvested: number;
  currentYearReceived: number;
  totalReceived: number;
}

export interface MonthlyDividend {
  month: number;
  amount: number;
}

export interface SecurityDividend {
  securityItemId: number;
  ticker: string;
  securityName: string;
  expectedAnnual: number;
  receivedTotal: number;
  dividendYieldOnEvaluated: number;
  dividendYieldOnInvested: number;
  contributionRatio: number;
}

export const getDividendSummary = (userId: number) => 
  fetchApi<DividendSummary>(`/api/dividends/summary?userId=${userId}`);

export const getMonthlyDividends = (userId: number, year?: number) => 
  fetchApi<MonthlyDividend[]>(`/api/dividends/monthly?userId=${userId}${year ? `&year=${year}` : ""}`);

export const getSecurityDividends = (userId: number) => 
  fetchApi<SecurityDividend[]>(`/api/dividends/by-security?userId=${userId}`);
