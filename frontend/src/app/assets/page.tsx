"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
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
import AddHoldingForm from "@/components/holdings/AddHoldingForm";
import {
  mockAssetSummary,
  mockAccountRatios,
  mockCountryRatios,
  mockTypeRatios,
  mockSectorRatios,
  mockStrategyRatios,
  mockHoldings,
  mockTimeline,
} from "@/lib/mock/assets";

export default function AssetsPage() {
  const [isAddFormOpen, setIsAddFormOpen] = useState(false);
  
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
    queryFn: () => getAssetTimeline(1),
    retry: false,
  });

  const formatCurrency = (value: number) =>
    new Intl.NumberFormat("ko-KR", {
      style: "currency",
      currency: "KRW",
      maximumFractionDigits: 0,
    }).format(value);

  const formatPercent = (value: number) =>
    new Intl.NumberFormat("ko-KR", {
      style: "percent",
      minimumFractionDigits: 2,
    }).format(value);

  const displaySummary = summary || mockAssetSummary;
  const displayAccountRatios = (accountRatios || mockAccountRatios).map(r => ({ category: r.accountName, amount: r.amount, ratio: r.ratio }));
  const displayCountryRatios = countryRatios || mockCountryRatios;
  const displayTypeRatios = typeRatios || mockTypeRatios;
  const displaySectorRatios = sectorRatios || mockSectorRatios;
  const displayStrategyRatios = strategyRatios || mockStrategyRatios;
  const displayHoldings = holdings || mockHoldings;
  const displayTimeline = timeline || mockTimeline;

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="mx-auto max-w-7xl">
        <div className="mb-8 flex items-center justify-between">
          <h1 className="text-3xl font-bold text-gray-900">자산 대시보드</h1>
          <button 
            onClick={() => setIsAddFormOpen(!isAddFormOpen)}
            className="rounded-xl bg-emerald-600 px-4 py-2 font-bold text-white hover:bg-emerald-700 transition-all"
          >
            {isAddFormOpen ? "닫기" : "보유 종목 추가"}
          </button>
        </div>

        {isAddFormOpen && (
          <div className="mb-8">
            <AddHoldingForm onSuccess={() => setIsAddFormOpen(false)} />
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
            isPositive={displaySummary.profitLoss >= 0}
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
      </div>
    </div>
  );
}
