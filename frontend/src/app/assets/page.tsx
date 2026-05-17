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

export default function AssetsPage() {
  const [isTradeFormOpen, setIsTradeFormOpen] = useState(false);
  
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
  const displayCountryRatios = countryRatios || [];
  const displayTypeRatios = typeRatios || [];
  const displaySectorRatios = sectorRatios || [];
  const displayStrategyRatios = strategyRatios || [];
  const displayHoldings = holdings || [];
  const displayTimeline = timeline || [];
  const displayTrades = trades || [];
  const hasDataLoadIssue = !summary || !holdings;

  return (
    <AuthGate>
    <div className="min-h-screen bg-gray-50 p-8">
      {hasDataLoadIssue && (
        <div className="mb-4 rounded-lg bg-amber-100 p-2 text-center text-sm text-amber-800">
          실제 데이터를 불러오지 못했습니다. 서버 상태와 입력 데이터를 확인하세요.
        </div>
      )}
      <div className="mx-auto max-w-7xl">
        <div className="mb-8 flex items-center justify-between">
          <h1 className="text-3xl font-bold text-gray-900">자산 대시보드</h1>
          <button 
            onClick={() => setIsTradeFormOpen(!isTradeFormOpen)}
            className="rounded-xl bg-emerald-600 px-4 py-2 font-bold text-white hover:bg-emerald-700 transition-all"
          >
            {isTradeFormOpen ? "닫기" : "거래 입력"}
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
            title="총 평가금액"
            value={formatCurrency(displaySummary.totalEvaluated)}
          />
          <SummaryCard
            title="총 투자원금"
            value={formatCurrency(displaySummary.totalInvested)}
          />
          <SummaryCard
            title="총 손익"
            value={formatCurrency(displaySummary.profitLoss)}
            subValue={formatPercent(displaySummary.profitLossRate)}
            isPositive={toFiniteNumber(displaySummary.profitLoss) >= 0}
          />
          <SummaryCard
            title="보유 종목 수"
            value={`${displaySummary.holdingCount}개`}
          />
        </div>

        {/* Timeline Chart */}
        <div className="mb-8">
          <TimelineChart data={displayTimeline} />
        </div>

        {/* Charts Grid */}
        <div className="mb-8 grid grid-cols-1 gap-6 lg:grid-cols-2 xl:grid-cols-3">
          <RatioChart title="계좌별 비중" data={displayAccountRatios} />
          <RatioChart title="국가별 비중" data={displayCountryRatios} />
          <RatioChart title="자산유형별 비중" data={displayTypeRatios} />
          <RatioChart title="섹터별 비중" data={displaySectorRatios} />
          <RatioChart title="전략별 비중" data={displayStrategyRatios} />
        </div>

        {/* Holdings Table */}
        <div className="mb-8">
          <h2 className="mb-4 text-xl font-bold text-gray-900">보유 종목 상세</h2>
          <HoldingTable holdings={displayHoldings} />
        </div>

        <div className="mb-8">
          <h2 className="mb-4 text-xl font-bold text-gray-900">거래내역</h2>
          <TradeTable trades={displayTrades} />
        </div>
      </div>
    </div>
    </AuthGate>
  );
}
