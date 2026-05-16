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
  // ... rest of mockHoldings
];

export const mockTimeline = [
  { date: "2024-01-01", totalAssetAmount: 85000000 },
  { date: "2024-02-01", totalAssetAmount: 88000000 },
  { date: "2024-03-01", totalAssetAmount: 92000000 },
  { date: "2024-04-01", totalAssetAmount: 95000000 },
  { date: "2024-05-01", totalAssetAmount: 115000000 },
];
