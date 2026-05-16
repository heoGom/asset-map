import { DividendSummary, MonthlyDividend, SecurityDividend } from "../api/dividends";

export const mockDividendSummary: DividendSummary = {
  expectedAnnualDividend: 4500000,
  monthlyAverageDividend: 375000,
  dividendYieldOnEvaluated: 0.039,
  dividendYieldOnInvested: 0.045,
  currentYearReceived: 1200000,
  totalReceived: 3500000,
};

export const mockMonthlyDividends: MonthlyDividend[] = [
  { month: 1, amount: 150000 },
  { month: 2, amount: 200000 },
  { month: 3, amount: 450000 },
  { month: 4, amount: 800000 },
  { month: 5, amount: 150000 },
  { month: 6, amount: 200000 },
  { month: 7, amount: 150000 },
  { month: 8, amount: 200000 },
  { month: 9, amount: 450000 },
  { month: 10, amount: 800000 },
  { month: 11, amount: 150000 },
  { month: 12, amount: 200000 },
];

export const mockSecurityDividends: SecurityDividend[] = [
  {
    securityItemId: 1,
    ticker: "AAPL",
    securityName: "Apple Inc.",
    expectedAnnual: 400000,
    receivedTotal: 1200000,
    dividendYieldOnEvaluated: 0.005,
    dividendYieldOnInvested: 0.006,
    contributionRatio: 0.09,
  },
  {
    securityItemId: 2,
    ticker: "SCHD",
    securityName: "Schwab US Dividend Equity ETF",
    expectedAnnual: 2500000,
    receivedTotal: 1500000,
    dividendYieldOnEvaluated: 0.034,
    dividendYieldOnInvested: 0.038,
    contributionRatio: 0.55,
  },
];
