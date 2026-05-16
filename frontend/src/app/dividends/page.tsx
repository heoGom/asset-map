"use client";

import { useQuery } from "@tanstack/react-query";
import {
  getDividendSummary,
  getMonthlyDividends,
  getSecurityDividends,
} from "@/lib/api/dividends";
import SummaryCard from "@/components/dashboard/SummaryCard";
import MonthlyDividendChart from "@/components/dashboard/MonthlyDividendChart";
import {
  mockDividendSummary,
  mockMonthlyDividends,
  mockSecurityDividends,
} from "@/lib/mock/dividends";

export default function DividendsPage() {
  const { data: summary } = useQuery({
    queryKey: ["dividend-summary"],
    queryFn: () => getDividendSummary(1),
    retry: false,
  });

  const { data: monthly } = useQuery({
    queryKey: ["dividend-monthly"],
    queryFn: () => getMonthlyDividends(1),
    retry: false,
  });

  const { data: securities } = useQuery({
    queryKey: ["dividend-securities"],
    queryFn: () => getSecurityDividends(1),
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

  const displaySummary = summary || mockDividendSummary;
  const displayMonthly = monthly || mockMonthlyDividends;
  const displaySecurities = securities || mockSecurityDividends;

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="mx-auto max-w-7xl">
        <h1 className="mb-8 text-3xl font-bold text-gray-900">배당 대시보드</h1>

        {/* Summary Cards */}
        <div className="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
          <SummaryCard
            title="연간 예상 배당금"
            value={formatCurrency(displaySummary.expectedAnnualDividend)}
            subValue={`월평균 ${formatCurrency(displaySummary.monthlyAverageDividend)}`}
          />
          <SummaryCard
            title="배당 수익률 (시가)"
            value={formatPercent(displaySummary.dividendYieldOnEvaluated)}
            subValue={`평단대비 ${formatPercent(displaySummary.dividendYieldOnInvested)}`}
          />
          <SummaryCard
            title="올해 받은 배당금"
            value={formatCurrency(displaySummary.currentYearReceived)}
          />
          <SummaryCard
            title="누적 배당금"
            value={formatCurrency(displaySummary.totalReceived)}
          />
        </div>

        {/* Monthly Chart */}
        <div className="mb-8">
          <MonthlyDividendChart data={displayMonthly} />
        </div>

        {/* Security Dividend Table */}
        <div className="mb-8">
          <h2 className="mb-4 text-xl font-bold text-gray-900">종목별 배당 상세</h2>
          <div className="overflow-hidden rounded-xl border border-gray-100 bg-white shadow-sm">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    {["종목명", "티커", "예상 연배당", "누적 배당금", "배당수익률(시가)", "배당수익률(평단)", "기여도"].map((h) => (
                      <th key={h} className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 bg-white">
                  {displaySecurities.map((s) => (
                    <tr key={s.securityItemId} className="hover:bg-gray-50">
                      <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">{s.securityName}</td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{s.ticker}</td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{formatCurrency(s.expectedAnnual)}</td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{formatCurrency(s.receivedTotal)}</td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{formatPercent(s.dividendYieldOnEvaluated)}</td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{formatPercent(s.dividendYieldOnInvested)}</td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm font-semibold text-emerald-600">{formatPercent(s.contributionRatio)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
