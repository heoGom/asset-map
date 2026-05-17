"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  getDividendSummary,
  getMonthlyDividends,
  getSecurityDividends,
} from "@/lib/api/dividends";
import SummaryCard from "@/components/dashboard/SummaryCard";
import MonthlyDividendChart from "@/components/dashboard/MonthlyDividendChart";
import AuthGate from "@/components/auth/AuthGate";
import { formatCurrency, formatPercent } from "@/lib/format";
import { useLanguage } from "@/lib/language-provider";

export default function DividendsPage() {
  const { t } = useLanguage();
  const { data: summary } = useQuery({
    queryKey: ["dividend-summary"],
    queryFn: getDividendSummary,
    retry: false,
  });

  const [selectedYear, setSelectedYear] = useState(2025);

  const { data: monthly } = useQuery({
    queryKey: ["dividend-monthly", selectedYear],
    queryFn: () => getMonthlyDividends(selectedYear),
    retry: false,
  });

  const { data: securities } = useQuery({
    queryKey: ["dividend-securities"],
    queryFn: getSecurityDividends,
    retry: false,
  });

  const displaySummary = summary || {
    expectedAnnualDividend: 0,
    monthlyAverageDividend: 0,
    dividendYieldOnEvaluated: 0,
    dividendYieldOnInvested: 0,
    currentYearReceived: 0,
    totalReceived: 0,
  };
  const displayMonthly = monthly || [];
  const displaySecurities = securities || [];
  const hasDataLoadIssue = !summary || !monthly;

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
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">{t("dividends.title")}</h1>
          <select
            value={selectedYear}
            onChange={(event) => setSelectedYear(Number(event.target.value))}
            className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 shadow-sm"
          >
            {[2024, 2025, 2026].map((year) => (
              <option key={year} value={year}>{year}</option>
            ))}
          </select>
        </div>

        {/* Summary Cards */}
        <div className="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
          <SummaryCard
            title={t("dividends.expectedAnnual")}
            value={formatCurrency(displaySummary.expectedAnnualDividend)}
            subValue={`${t("dividends.monthlyAverage")} ${formatCurrency(displaySummary.monthlyAverageDividend)}`}
          />
          <SummaryCard
            title={t("dividends.yieldMarket")}
            value={formatPercent(displaySummary.dividendYieldOnEvaluated)}
            subValue={`${t("dividends.yieldCost")} ${formatPercent(displaySummary.dividendYieldOnInvested)}`}
          />
          <SummaryCard
            title={t("dividends.currentYearReceived")}
            value={formatCurrency(displaySummary.currentYearReceived)}
          />
          <SummaryCard
            title={t("dividends.totalReceived")}
            value={formatCurrency(displaySummary.totalReceived)}
          />
        </div>

        {/* Monthly Chart */}
        <div className="mb-8">
          <MonthlyDividendChart data={displayMonthly} />
        </div>

        {/* Security Dividend Table */}
        <div className="mb-8">
          <h2 className="mb-4 text-xl font-bold text-gray-900 dark:text-white">{t("dividends.bySecurity")}</h2>
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
    </AuthGate>
  );
}
