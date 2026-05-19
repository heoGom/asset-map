"use client";

import { FormEvent, useMemo, useRef, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
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
import { createMarketPrice } from "@/lib/api/market-prices";
import { HoldingResponse } from "@/lib/api/holdings";
import AuthGate from "@/components/auth/AuthGate";
import { formatCurrency, formatPercent, toFiniteNumber } from "@/lib/format";
import { useLanguage } from "@/lib/language-provider";
import { getCategoryLabel } from "@/lib/category-labels";
import SecurityCombobox from "@/components/securities/SecurityCombobox";

export default function AssetsPage() {
  const [isTradeFormOpen, setIsTradeFormOpen] = useState(false);
  const [tradeTarget, setTradeTarget] = useState<{
    accountId?: number;
    accountName?: string;
    securityItemId?: number;
    securityName?: string;
  } | null>(null);
  const tradeFormRef = useRef<HTMLDivElement>(null);
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
            onClick={() => {
              setTradeTarget(null);
              setIsTradeFormOpen(!isTradeFormOpen);
            }}
            className="rounded-xl bg-emerald-600 px-4 py-2 font-bold text-white hover:bg-emerald-700 transition-all"
          >
            {isTradeFormOpen ? t("common.close") : t("assets.tradeInput")}
          </button>
        </div>

        {isTradeFormOpen && (
          <div ref={tradeFormRef} className="mb-8">
            {tradeTarget?.securityName && (
              <div className="mb-3 rounded-lg border border-emerald-100 bg-emerald-50 px-4 py-3 text-sm text-emerald-800 dark:border-emerald-900 dark:bg-emerald-950 dark:text-emerald-200">
                {tradeTarget.accountName ? `${tradeTarget.accountName} / ` : ""}
                {tradeTarget.securityName} 거래내역을 추가합니다.
              </div>
            )}
            <TradeInputForm
              onSuccess={() => setIsTradeFormOpen(false)}
              initialAccountId={tradeTarget?.accountId}
              initialSecurityItemId={tradeTarget?.securityItemId}
            />
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
          <MarketPriceInput holdings={displayHoldings} />
        </div>

        <div className="mb-8">
          <h2 className="mb-4 text-xl font-bold text-gray-900 dark:text-white">{t("assets.holdingDetail")}</h2>
          <HoldingTable
            holdings={displayHoldings}
            onSecurityClick={(holding) => {
              setTradeTarget({
                accountId: holding.accountId,
                accountName: holding.accountName,
                securityItemId: holding.securityItemId,
                securityName: holding.securityName,
              });
              setIsTradeFormOpen(true);
              window.requestAnimationFrame(() => {
                tradeFormRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
              });
            }}
          />
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

function MarketPriceInput({ holdings }: { holdings: HoldingResponse[] }) {
  const queryClient = useQueryClient();
  const today = new Date().toISOString().slice(0, 10);
  const uniqueHoldings = useMemo(() => {
    const bySecurity = new Map<number, HoldingResponse>();
    holdings.forEach((holding) => {
      if (!bySecurity.has(holding.securityItemId)) {
        bySecurity.set(holding.securityItemId, holding);
      }
    });
    return Array.from(bySecurity.values());
  }, [holdings]);
  const [form, setForm] = useState({
    securityItemId: "",
    priceDate: today,
    currentPrice: "",
    closePrice: "",
    changeAmount: "",
    changeRate: "",
    volume: "",
  });

  const mutation = useMutation({
    mutationFn: createMarketPrice,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["holdings"] });
      queryClient.invalidateQueries({ queryKey: ["asset-summary"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-account"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-country"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-type"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-sector"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-strategy"] });
      setForm((prev) => ({
        ...prev,
        currentPrice: "",
        closePrice: "",
        changeAmount: "",
        changeRate: "",
        volume: "",
      }));
    },
  });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const currentPrice = Number(form.currentPrice);
    const closePrice = form.closePrice ? Number(form.closePrice) : currentPrice;
    if (!form.securityItemId || !Number.isFinite(currentPrice) || currentPrice < 0 || !Number.isFinite(closePrice) || closePrice < 0) {
      return;
    }
    mutation.mutate({
      securityItemId: Number(form.securityItemId),
      priceDate: form.priceDate,
      currentPrice,
      closePrice,
      changeAmount: form.changeAmount ? Number(form.changeAmount) : undefined,
      changeRate: form.changeRate ? Number(form.changeRate) : undefined,
      volume: form.volume ? Number(form.volume) : undefined,
      source: "MANUAL",
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 rounded-xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-900">
      <div>
        <h2 className="text-xl font-bold text-gray-900 dark:text-white">현재가 직접 입력</h2>
        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">보유 종목의 수동 가격을 저장하고 평가금액을 갱신합니다.</p>
      </div>
      {uniqueHoldings.length === 0 ? (
        <p className="rounded-lg bg-gray-50 p-4 text-sm text-gray-500 dark:bg-gray-800 dark:text-gray-400">
          보유 종목이 생기면 현재가를 입력할 수 있습니다.
        </p>
      ) : (
        <>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
              종목
              <SecurityCombobox
                options={uniqueHoldings.map((holding) => ({
                  id: holding.securityItemId,
                  ticker: holding.ticker,
                  name: holding.securityName,
                  currency: holding.currency,
                }))}
                value={form.securityItemId}
                placeholder="보유 종목 검색"
                onChange={(securityItemId) => setForm({ ...form, securityItemId })}
              />
            </label>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
              가격일
              <input
                required
                type="date"
                value={form.priceDate}
                onChange={(event) => setForm({ ...form, priceDate: event.target.value })}
                className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
              />
            </label>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
              현재가
              <input
                required
                min="0"
                step="0.000001"
                type="number"
                value={form.currentPrice}
                onChange={(event) => setForm({ ...form, currentPrice: event.target.value })}
                className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
              />
            </label>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
              종가
              <input
                min="0"
                step="0.000001"
                type="number"
                value={form.closePrice}
                onChange={(event) => setForm({ ...form, closePrice: event.target.value })}
                className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
              />
            </label>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
              등락금액
              <input
                step="0.000001"
                type="number"
                value={form.changeAmount}
                onChange={(event) => setForm({ ...form, changeAmount: event.target.value })}
                className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
              />
            </label>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
              등락률
              <input
                step="0.000001"
                type="number"
                value={form.changeRate}
                onChange={(event) => setForm({ ...form, changeRate: event.target.value })}
                className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
              />
            </label>
          </div>
          {mutation.isError && <p className="text-sm text-rose-600">현재가 저장에 실패했습니다.</p>}
          {mutation.isSuccess && <p className="text-sm text-emerald-600">현재가를 저장했습니다.</p>}
          <button type="submit" disabled={mutation.isPending} className="w-full rounded-xl bg-emerald-600 p-2 font-bold text-white hover:bg-emerald-700 disabled:bg-gray-300">
            {mutation.isPending ? "저장 중" : "현재가 저장"}
          </button>
        </>
      )}
    </form>
  );
}
