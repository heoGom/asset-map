import { fetchApi } from "../api-client";

export interface AssetSummary {
  totalInvested: number;
  totalEvaluated: number;
  profitLoss: number;
  profitLossRate: number;
  holdingCount: number;
}

interface AssetSummaryResponse {
  totalInvestedAmount: number;
  totalEvaluatedAmount: number;
  totalProfitLoss: number;
  totalProfitLossRate: number;
  holdingCount: number;
}

export interface AssetRatio {
  category: string;
  amount: number;
  ratio: number;
}

export interface AccountAssetRatio {
  accountId: number;
  accountName: string;
  amount: number;
  ratio: number;
}

export interface AssetTimeline {
  date: string;
  totalAssetAmount: number;
}

export const getAssetSummary = async (): Promise<AssetSummary> => {
  const summary = await fetchApi<AssetSummaryResponse>("/api/assets/summary");
  return {
    totalInvested: summary.totalInvestedAmount,
    totalEvaluated: summary.totalEvaluatedAmount,
    profitLoss: summary.totalProfitLoss,
    profitLossRate: summary.totalProfitLossRate,
    holdingCount: summary.holdingCount,
  };
};
export const getAssetByAccount = () => fetchApi<AccountAssetRatio[]>("/api/assets/by-account");
export const getAssetByCountry = () => fetchApi<AssetRatio[]>("/api/assets/by-country");
export const getAssetByType = () => fetchApi<AssetRatio[]>("/api/assets/by-type");
export const getAssetBySector = () => fetchApi<AssetRatio[]>("/api/assets/by-sector");
export const getAssetByStrategy = () => fetchApi<AssetRatio[]>("/api/assets/by-strategy");
export const getAssetTimeline = () => fetchApi<AssetTimeline[]>("/api/snapshots/timeline");
