"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  getAssetSummary,
  getAssetByAccount,
  getAssetByCountry,
  getAssetByType,
  getAssetBySector,
  getAssetByStrategy,
  getAssetTimeline,
  AssetRatio,
} from "@/lib/api/assets";
import { getHoldings } from "@/lib/api/holdings";
import SummaryCard from "@/components/dashboard/SummaryCard";
import RatioChart from "@/components/dashboard/RatioChart";
import TimelineChart from "@/components/dashboard/TimelineChart";
import HoldingTable from "@/components/dashboard/HoldingTable";
import TradeInputForm from "@/components/trades/TradeInputForm";
import TradeTable from "@/components/trades/TradeTable";
import { getTrades } from "@/lib/api/trades";
import AuthGate from "@/components/auth/AuthGate";
import { formatCurrency, formatPercent, toFiniteNumber } from "@/lib/format";
import { useLanguage } from "@/lib/language-provider";
import { getCategoryLabel } from "@/lib/category-labels";

export default function AssetsPage() {
  const [isTradeFormOpen, setIsTradeFormOpen] = useState(false);
  const { language, t } = useLanguage();
  
  const { data: summary } = useQuery({
    queryKey: ["asset-summary"],
    queryFn: getAssetSummary,
    retry: false,
  });

  const { data: accountRatios } = useQuery({
    queryKey: ["asset-by-account"],
    queryFn: getAssetByAccount,
    retry: false,
  });

  const { data: countryRatios } = useQuery({
    queryKey: ["asset-by-country"],
    queryFn: getAssetByCountry,
    retry: false,
  });

  const { data: typeRatios } = useQuery({
    queryKey: ["asset-by-type"],
    queryFn: getAssetByType,
    retry: false,
  });

  const { data: sectorRatios } = useQuery({
    queryKey: ["asset-by-sector"],
    queryFn: getAssetBySector,
    retry: false,
  });

  const { data: strategyRatios } = useQuery({
    queryKey: ["asset-by-strategy"],
    queryFn: getAssetByStrategy,
    retry: false,
  });

  const { data: holdings } = useQuery({
    queryKey: ["holdings"],
    queryFn: getHoldings,
    retry: false,
  });

  const { data: timeline } = useQuery({
    queryKey: ["asset-timeline"],
    queryFn: getAssetTimeline,
    retry: false,
  });

  const { data: trades } = useQuery({
    queryKey: ["trades"],
    queryFn: getTrades,
    retry: false,
  });

  const displaySummary = summary || {
    totalInvested: 0,
    totalEvaluated: 0,
    profitLoss: 0,
    profitLossRate: 0,
    holdingCount: 0,
  };
  const displayAccountRatios = (accountRatios || []).map(r => ({ category: r.accountName, amount: toFiniteNumber(r.amount), ratio: toFiniteNumber(r.ratio) }));
  const localizeRatios = (ratios: AssetRatio[] = []) =>
    ratios.map((ratio) => ({
      ...ratio,
      category: getCategoryLabel(ratio.category, language),
    }));
  const displayCountryRatios = localizeRatios(countryRatios);
  const displayTypeRatios = localizeRatios(typeRatios);
  const displaySectorRatios = localizeRatios(sectorRatios);
  const displayStrategyRatios = localizeRatios(strategyRatios);
  const displayHoldings = holdings || [];
  const displayTimeline = timeline || [];
  const displayTrades = trades || [];
  const hasDataLoadIssue = !summary || !holdings;

  return (
    <AuthGate>
    <div className="min-h-screen bg-gray-50 p-8 dark:bg-gray-950">
      {hasDataLoadIssue && (
        <div className="mb-4 rounded-lg bg-amber-100 p-2 text-center text-sm text-amber-800">
          {t("common.noLoad")}
        </div>
      )}
      <div className="mx-auto max-w-7xl">
        <div className="mb-8 flex items-center justify-between">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">{t("assets.title")}</h1>
          <button 
            onClick={() => setIsTradeFormOpen(!isTradeFormOpen)}
            className="rounded-xl bg-emerald-600 px-4 py-2 font-bold text-white hover:bg-emerald-700 transition-all"
          >
            {isTradeFormOpen ? t("common.close") : t("assets.tradeInput")}
          </button>
        </div>

        {isTradeFormOpen && (
          <div className="mb-8">
            <TradeInputForm onSuccess={() => setIsTradeFormOpen(false)} />
          </div>
        )}

        {/* Summary Cards */}
        <div className="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
          <SummaryCard
            title={t("assets.totalEvaluated")}
            value={formatCurrency(displaySummary.totalEvaluated)}
          />
          <SummaryCard
            title={t("assets.totalInvested")}
            value={formatCurrency(displaySummary.totalInvested)}
          />
          <SummaryCard
            title={t("assets.totalProfitLoss")}
            value={formatCurrency(displaySummary.profitLoss)}
            subValue={formatPercent(displaySummary.profitLossRate)}
            isPositive={toFiniteNumber(displaySummary.profitLoss) >= 0}
          />
          <SummaryCard
            title={t("assets.holdingCount")}
            value={`${displaySummary.holdingCount}개`}
          />
        </div>

        {/* Timeline Chart */}
        <div className="mb-8">
          <TimelineChart data={displayTimeline} />
        </div>

        {/* Charts Grid */}
        <div className="mb-8 grid grid-cols-1 gap-6 lg:grid-cols-2 xl:grid-cols-3">
          <RatioChart title={t("assets.accountRatio")} data={displayAccountRatios} />
          <RatioChart title={t("assets.countryRatio")} data={displayCountryRatios} />
          <RatioChart title={t("assets.typeRatio")} data={displayTypeRatios} />
          <RatioChart title={t("assets.sectorRatio")} data={displaySectorRatios} />
          <RatioChart title={t("assets.strategyRatio")} data={displayStrategyRatios} />
        </div>

        {/* Holdings Table */}
        <div className="mb-8">
          <h2 className="mb-4 text-xl font-bold text-gray-900 dark:text-white">{t("assets.holdingDetail")}</h2>
          <HoldingTable holdings={displayHoldings} />
        </div>

        <div className="mb-8">
          <h2 className="mb-4 text-xl font-bold text-gray-900 dark:text-white">{t("assets.trades")}</h2>
          <TradeTable trades={displayTrades} />
        </div>
      </div>
    </div>
    </AuthGate>
  );
}
