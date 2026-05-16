import { AssetSummary } from "../api/assets";

export const mockAssetSummary: AssetSummary = {
  totalInvested: 100000000,
  totalEvaluated: 115000000,
  profitLoss: 15000000,
  profitLossRate: 0.15,
  holdingCount: 12,
};

export const mockAccountRatios = [
  { accountId: 1, accountName: "신한 주식 계좌", amount: 60000000, ratio: 0.52 },
  { accountId: 2, accountName: "KB ISA 계좌", amount: 40000000, ratio: 0.35 },
  { accountId: 3, accountName: "미래에셋 연금", amount: 15000000, ratio: 0.13 },
];

export const mockCountryRatios = [
  { category: "USA", amount: 70000000, ratio: 0.61 },
  { category: "KOREA", amount: 30000000, ratio: 0.26 },
  { category: "JAPAN", amount: 15000000, ratio: 0.13 },
];

export const mockTypeRatios = [
  { category: "STOCK", amount: 80000000, ratio: 0.70 },
  { category: "ETF", amount: 25000000, ratio: 0.22 },
  { category: "CASH", amount: 10000000, ratio: 0.08 },
];

export const mockSectorRatios = [
  { category: "TECHNOLOGY", amount: 50000000, ratio: 0.43 },
  { category: "FINANCE", amount: 20000000, ratio: 0.17 },
  { category: "CONSUMER", amount: 15000000, ratio: 0.13 },
  { category: "OTHERS", amount: 30000000, ratio: 0.26 },
];

export const mockStrategyRatios = [
  { category: "GROWTH", amount: 60000000, ratio: 0.52 },
  { category: "DIVIDEND", amount: 40000000, ratio: 0.35 },
  { category: "VALUE", amount: 15000000, ratio: 0.13 },
];

export const mockHoldings = [
  {
    id: 1,
    accountId: 1,
    accountName: "신한 주식 계좌",
    securityItemId: 1,
    ticker: "AAPL",
    securityName: "Apple Inc.",
    quantity: 100,
    averagePrice: 150,
    currentPrice: 185,
    currency: "USD",
    investedAmount: 15000,
    evaluatedAmount: 18500,
    profitLoss: 3500,
    profitLossRate: 0.23,
  },
  {
    id: 2,
    accountId: 1,
    accountName: "신한 주식 계좌",
    securityItemId: 2,
    ticker: "MSFT",
    securityName: "Microsoft Corp.",
    quantity: 50,
    averagePrice: 250,
    currentPrice: 310,
    currency: "USD",
    investedAmount: 12500,
    evaluatedAmount: 15500,
    profitLoss: 3000,
    profitLossRate: 0.24,
  },
];
