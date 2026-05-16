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

export const getDividendSummary = () => fetchApi<DividendSummary>("/api/dividends/summary");

export const getMonthlyDividends = (year?: number) =>
  fetchApi<MonthlyDividend[]>(`/api/dividends/monthly${year ? `?year=${year}` : ""}`);

export const getSecurityDividends = () => fetchApi<SecurityDividend[]>("/api/dividends/by-security");
