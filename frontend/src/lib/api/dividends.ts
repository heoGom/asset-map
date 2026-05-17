import { fetchApi } from "../api-client";

export interface DividendSummary {
  expectedAnnualDividend: number;
  monthlyAverageDividend: number;
  dividendYieldOnEvaluated: number;
  dividendYieldOnInvested: number;
  currentYearReceived: number;
  totalReceived: number;
}

interface DividendSummaryResponse {
  expectedAnnualDividendKrw: number;
  averageMonthlyDividendKrw: number;
  portfolioDividendYield: number;
  yieldOnCost: number;
  currentYearReceivedDividendKrw: number;
  totalReceivedDividendKrw: number;
}

export interface MonthlyDividend {
  month: number;
  amount: number;
}

interface MonthlyDividendResponse {
  month: number;
  amountKrw: number;
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

interface SecurityDividendResponse {
  securityItemId: number;
  ticker: string;
  securityName: string;
  expectedAnnualDividendKrw: number;
  receivedDividendKrw: number;
  dividendYield: number;
  yieldOnCost: number;
  contributionRatio: number;
}

export interface DividendPaymentResponse {
  id: number;
  userId: number;
  accountId: number;
  accountName: string;
  securityItemId: number;
  ticker: string;
  securityName: string;
  dividendEventId?: number;
  quantityAtRecordDate: number;
  dividendPerShare: number;
  grossAmount: number;
  taxAmount: number;
  netAmount: number;
  currency: string;
  exchangeRate: number;
  grossAmountKrw: number;
  netAmountKrw: number;
  paymentDate?: string;
  status: "EXPECTED" | "CONFIRMED" | "PAID";
  createdAt: string;
  updatedAt: string;
}

export interface DividendEventResponse {
  id: number;
  securityItemId: number;
  ticker: string;
  securityName: string;
  dividendYear: number;
  declarationDate?: string;
  exDividendDate?: string;
  recordDate: string;
  paymentDate?: string;
  eventType?: "CASH_DIVIDEND" | "ETF_DISTRIBUTION";
  dividendPerShare: number;
  currency: string;
  source: "MANUAL" | "API" | "CSV" | "CRAWLING";
}

export interface DividendEventCreateRequest {
  securityItemId: number;
  dividendYear: number;
  declarationDate?: string;
  exDividendDate?: string;
  recordDate: string;
  paymentDate?: string;
  eventType?: "CASH_DIVIDEND" | "ETF_DISTRIBUTION";
  dividendPerShare: number;
  currency: string;
  source: "MANUAL";
}

export interface DividendPaymentGenerateResponse {
  dividendEventId: number;
  generatedCount: number;
  totalGrossAmount: number;
  totalNetAmount: number;
  payments: DividendPaymentResponse[];
}

export const getDividendSummary = async (): Promise<DividendSummary> => {
  const summary = await fetchApi<DividendSummaryResponse>("/api/dividends/summary");
  return {
    expectedAnnualDividend: summary.expectedAnnualDividendKrw,
    monthlyAverageDividend: summary.averageMonthlyDividendKrw,
    dividendYieldOnEvaluated: summary.portfolioDividendYield,
    dividendYieldOnInvested: summary.yieldOnCost,
    currentYearReceived: summary.currentYearReceivedDividendKrw,
    totalReceived: summary.totalReceivedDividendKrw,
  };
};

export const getMonthlyDividends = async (year?: number): Promise<MonthlyDividend[]> => {
  const monthly = await fetchApi<MonthlyDividendResponse[]>(`/api/dividends/monthly${year ? `?year=${year}` : ""}`);
  return monthly.map((item) => ({
    month: item.month,
    amount: item.amountKrw,
  }));
};

export const getSecurityDividends = async (): Promise<SecurityDividend[]> => {
  const securities = await fetchApi<SecurityDividendResponse[]>("/api/dividends/by-security");
  return securities.map((item) => ({
    securityItemId: item.securityItemId,
    ticker: item.ticker,
    securityName: item.securityName,
    expectedAnnual: item.expectedAnnualDividendKrw,
    receivedTotal: item.receivedDividendKrw,
    dividendYieldOnEvaluated: item.dividendYield,
    dividendYieldOnInvested: item.yieldOnCost,
    contributionRatio: item.contributionRatio,
  }));
};

export const getDividendEvents = () => fetchApi<DividendEventResponse[]>("/api/dividends/events");

export const createDividendEvent = (request: DividendEventCreateRequest) =>
  fetchApi<DividendEventResponse>("/api/dividends/events", {
    method: "POST",
    body: JSON.stringify(request),
  });

export const generateMyDividendPayments = (eventId: number) =>
  fetchApi<DividendPaymentGenerateResponse>(`/api/dividends/payments/generate/me/${eventId}`, {
    method: "POST",
  });

export const getMyDividendPayments = (userId: number) =>
  fetchApi<DividendPaymentResponse[]>(`/api/dividends/payments/user/${userId}`);
