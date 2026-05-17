"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createDividendEvent,
  generateMyDividendPayments,
  getDividendEvents,
  getDividendSummary,
  getMonthlyDividends,
  getMyDividendPayments,
  getSecurityDividends,
} from "@/lib/api/dividends";
import { getSecurityItems } from "@/lib/api/security-items";
import { importMyStockDividends, DividendImportResult } from "@/lib/api/dividend-imports";
import SummaryCard from "@/components/dashboard/SummaryCard";
import MonthlyDividendChart from "@/components/dashboard/MonthlyDividendChart";
import AuthGate from "@/components/auth/AuthGate";
import { formatCurrency, formatPercent } from "@/lib/format";
import { useAuth } from "@/lib/auth-provider";
import { useLanguage } from "@/lib/language-provider";

const inputClass =
  "mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-sm text-gray-900 shadow-sm dark:border-gray-700 dark:bg-gray-800 dark:text-white";
const labelClass = "block text-sm font-medium text-gray-700 dark:text-gray-200";

export default function DividendsPage() {
  const { t } = useLanguage();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [selectedYear, setSelectedYear] = useState(2025);
  const [isEventPanelOpen, setIsEventPanelOpen] = useState(false);
  const currentYear = new Date().getFullYear();
  const [importForm, setImportForm] = useState({
    fromYear: "2020",
    toYear: String(currentYear),
  });
  const [importResult, setImportResult] = useState<DividendImportResult | null>(null);
  const [eventForm, setEventForm] = useState({
    securityItemId: "",
    eventType: "CASH_DIVIDEND" as "CASH_DIVIDEND" | "ETF_DISTRIBUTION",
    declarationDate: "",
    exDividendDate: "",
    recordDate: "",
    paymentDate: "",
    dividendPerShare: "",
    currency: "KRW",
  });
  const [generateMessage, setGenerateMessage] = useState("");

  const { data: summary } = useQuery({
    queryKey: ["dividend-summary"],
    queryFn: getDividendSummary,
    retry: false,
  });

  const { data: monthly } = useQuery({
    queryKey: ["dividend-monthly", selectedYear],
    queryFn: () => getMonthlyDividends(selectedYear),
    retry: false,
  });

  const { data: securities = [] } = useQuery({
    queryKey: ["securities"],
    queryFn: getSecurityItems,
    retry: false,
  });

  const { data: dividendEvents = [] } = useQuery({
    queryKey: ["dividend-events"],
    queryFn: getDividendEvents,
    retry: false,
  });

  const { data: dividendPayments = [] } = useQuery({
    queryKey: ["dividend-payments", user?.id],
    queryFn: () => getMyDividendPayments(user!.id),
    enabled: Boolean(user?.id),
    retry: false,
  });

  const { data: securitiesDividend } = useQuery({
    queryKey: ["dividend-securities"],
    queryFn: getSecurityDividends,
    retry: false,
  });

  const createEventMutation = useMutation({
    mutationFn: createDividendEvent,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["dividend-events"] });
      setEventForm((prev) => ({
        ...prev,
        declarationDate: "",
        exDividendDate: "",
        recordDate: "",
        paymentDate: "",
        dividendPerShare: "",
      }));
    },
  });

  const generateMutation = useMutation({
    mutationFn: generateMyDividendPayments,
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: ["dividend-summary"] });
      queryClient.invalidateQueries({ queryKey: ["dividend-monthly"] });
      queryClient.invalidateQueries({ queryKey: ["dividend-securities"] });
      queryClient.invalidateQueries({ queryKey: ["dividend-payments"] });
      const amount = formatCurrency(result.totalNetAmount);
      setGenerateMessage(
        result.generatedCount > 0
          ? `${result.generatedCount}건의 내 배당금을 생성했습니다. 세후 합계 ${amount}`
          : "기준일 보유수량이 없어 생성된 배당금이 없습니다."
      );
    },
  });

  const importMutation = useMutation({
    mutationFn: importMyStockDividends,
    onSuccess: (result) => {
      setImportResult(result);
      queryClient.invalidateQueries({ queryKey: ["dividend-summary"] });
      queryClient.invalidateQueries({ queryKey: ["dividend-monthly"] });
      queryClient.invalidateQueries({ queryKey: ["dividend-securities"] });
      queryClient.invalidateQueries({ queryKey: ["dividend-events"] });
      queryClient.invalidateQueries({ queryKey: ["dividend-payments"] });
    },
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
  const displaySecurities = securitiesDividend || [];
  const hasDataLoadIssue = !summary || !monthly;

  const handleSecurityChange = (securityItemId: string) => {
    const selectedSecurity = securities.find((item) => item.id === Number(securityItemId));
    setEventForm((prev) => ({
      ...prev,
      securityItemId,
      currency: selectedSecurity?.currency || "KRW",
    }));
  };

  const openEventFormForSecurity = (securityItemId: number) => {
    handleSecurityChange(String(securityItemId));
    setIsEventPanelOpen(true);
  };

  const handleEventSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const dividendPerShare = Number(eventForm.dividendPerShare);
    if (!eventForm.securityItemId || !eventForm.recordDate || !Number.isFinite(dividendPerShare) || dividendPerShare < 0) {
      return;
    }
    createEventMutation.mutate({
      securityItemId: Number(eventForm.securityItemId),
      dividendYear: new Date(eventForm.recordDate).getFullYear(),
      declarationDate: eventForm.declarationDate || undefined,
      exDividendDate: eventForm.exDividendDate || undefined,
      recordDate: eventForm.recordDate,
      paymentDate: eventForm.paymentDate || undefined,
      eventType: eventForm.eventType,
      dividendPerShare,
      currency: eventForm.currency || "KRW",
      source: "MANUAL",
    });
  };

  const handleImportSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setImportResult(null);
    importMutation.mutate({
      fromYear: Number(importForm.fromYear || 2020),
      toYear: Number(importForm.toYear || currentYear),
    });
  };

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
              className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 shadow-sm dark:border-gray-700 dark:bg-gray-800 dark:text-white"
            >
              {[2024, 2025, 2026].map((year) => (
                <option key={year} value={year}>{year}</option>
              ))}
            </select>
          </div>

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

          <div className="mb-8">
            <MonthlyDividendChart data={displayMonthly} />
          </div>

          <form onSubmit={handleImportSubmit} className="mb-8 space-y-4 rounded-xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-900">
            <div className="flex flex-col gap-2 md:flex-row md:items-start md:justify-between">
              <div>
                <h2 className="text-xl font-bold text-gray-900 dark:text-white">내 종목 배당정보 가져오기</h2>
                <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                  국내 개별주/우선주 배당정보만 가져옵니다. ETF 분배금은 자동 연동 대상이 아니며 수동 입력을 사용합니다.
                </p>
              </div>
              <div className="grid grid-cols-2 gap-3 md:w-64">
                <label className={labelClass}>
                  시작연도
                  <input
                    type="number"
                    min="2020"
                    value={importForm.fromYear}
                    onChange={(event) => setImportForm({ ...importForm, fromYear: event.target.value })}
                    className={inputClass}
                  />
                </label>
                <label className={labelClass}>
                  종료연도
                  <input
                    type="number"
                    min="2020"
                    value={importForm.toYear}
                    onChange={(event) => setImportForm({ ...importForm, toYear: event.target.value })}
                    className={inputClass}
                  />
                </label>
              </div>
            </div>
            {importResult && (
              <div className="rounded-lg bg-emerald-50 p-4 text-sm text-emerald-800 dark:bg-emerald-950 dark:text-emerald-200">
                대상 {importResult.targetSecurityCount}개, 저장 {importResult.importedEventCount}건, 건너뜀 {importResult.skippedEventCount}건,
                내 배당금 생성 {importResult.generatedPaymentCount}건, 실패 종목 {importResult.failedSecurityCount}개
              </div>
            )}
            {importMutation.isError && (
              <p className="text-sm text-rose-600">
                배당정보 가져오기에 실패했습니다. API 키 설정과 대상 종목을 확인하세요.
              </p>
            )}
            <button type="submit" disabled={importMutation.isPending} className="rounded-xl bg-emerald-600 px-4 py-2 font-bold text-white transition-all hover:bg-emerald-700 disabled:bg-gray-300">
              {importMutation.isPending ? "가져오는 중" : "내 종목 배당정보 가져오기"}
            </button>
          </form>

          <div className="mb-8">
            <button
              type="button"
              onClick={() => setIsEventPanelOpen((open) => !open)}
              className="rounded-xl bg-emerald-600 px-4 py-2 font-bold text-white transition-all hover:bg-emerald-700"
            >
              {isEventPanelOpen ? "배당 이벤트 닫기" : "배당 이벤트 관리"}
            </button>
          </div>

          {isEventPanelOpen && (
          <div className="mb-8 grid grid-cols-1 gap-6 xl:grid-cols-2">
            <form onSubmit={handleEventSubmit} className="space-y-4 rounded-xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-900">
              <div>
                <h2 className="text-xl font-bold text-gray-900 dark:text-white">배당/분배금 이벤트 추가</h2>
                <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">직접 입력한 이벤트를 기준으로 내 배당금을 생성합니다.</p>
              </div>
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <label className={labelClass}>
                  종목
                  <select required value={eventForm.securityItemId} onChange={(event) => handleSecurityChange(event.target.value)} className={inputClass}>
                    <option value="">종목 선택</option>
                    {securities.map((security) => (
                      <option key={security.id} value={security.id}>{security.ticker} - {security.name}</option>
                    ))}
                  </select>
                  {securities.length === 0 && (
                    <Link href="/securities" className="mt-1 inline-block text-xs font-semibold text-emerald-600 hover:text-emerald-700">
                      새 종목 등록
                    </Link>
                  )}
                </label>
                <label className={labelClass}>
                  이벤트 유형
                  <select value={eventForm.eventType} onChange={(event) => setEventForm({ ...eventForm, eventType: event.target.value as "CASH_DIVIDEND" | "ETF_DISTRIBUTION" })} className={inputClass}>
                    <option value="CASH_DIVIDEND">주식 배당</option>
                    <option value="ETF_DISTRIBUTION">ETF 분배금</option>
                  </select>
                </label>
                <label className={labelClass}>
                  공시일
                  <input type="date" value={eventForm.declarationDate} onChange={(event) => setEventForm({ ...eventForm, declarationDate: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  배당락일
                  <input type="date" value={eventForm.exDividendDate} onChange={(event) => setEventForm({ ...eventForm, exDividendDate: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  기준일
                  <input required type="date" value={eventForm.recordDate} onChange={(event) => setEventForm({ ...eventForm, recordDate: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  지급일
                  <input type="date" value={eventForm.paymentDate} onChange={(event) => setEventForm({ ...eventForm, paymentDate: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  주당 배당금/분배금
                  <input required min="0" step="0.000001" type="number" value={eventForm.dividendPerShare} onChange={(event) => setEventForm({ ...eventForm, dividendPerShare: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  통화
                  <input required value={eventForm.currency} onChange={(event) => setEventForm({ ...eventForm, currency: event.target.value })} className={inputClass} />
                </label>
              </div>
              {createEventMutation.isError && <p className="text-sm text-rose-600">배당 이벤트 저장에 실패했습니다.</p>}
              {createEventMutation.isSuccess && <p className="text-sm text-emerald-600">배당 이벤트를 저장했습니다.</p>}
              <button type="submit" disabled={createEventMutation.isPending} className="w-full rounded-xl bg-emerald-600 p-2 font-bold text-white hover:bg-emerald-700 disabled:bg-gray-300">
                {createEventMutation.isPending ? "저장 중" : "이벤트 저장"}
              </button>
            </form>

            <div className="rounded-xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-900">
              <h2 className="text-xl font-bold text-gray-900 dark:text-white">배당/분배금 이벤트</h2>
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                “내 배당금 생성”은 이 이벤트의 기준일에 내가 보유한 수량을 계산해 실제 내 계좌의 DividendPayment를 만드는 작업입니다.
              </p>
              {generateMessage && <p className="mt-2 text-sm text-emerald-600">{generateMessage}</p>}
              {generateMutation.isError && <p className="mt-2 text-sm text-rose-600">내 배당금 생성에 실패했습니다.</p>}
              {dividendEvents.length === 0 ? (
                <p className="mt-4 rounded-lg bg-gray-50 p-4 text-sm text-gray-500 dark:bg-gray-800 dark:text-gray-400">등록된 배당 이벤트가 없습니다.</p>
              ) : (
                <div className="mt-4 max-h-[420px] overflow-auto">
                  <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                    <thead className="bg-gray-50 dark:bg-gray-800">
                      <tr>
                        {["지급일", "종목", "유형", "주당금액", "출처", ""].map((header) => (
                          <th key={header} className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-300">{header}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 bg-white dark:divide-gray-700 dark:bg-gray-900">
                      {dividendEvents.map((item) => (
                        <tr key={item.id} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                          <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-900 dark:text-white">{item.paymentDate || "-"}</td>
                          <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-900 dark:text-white">{item.securityName}</td>
                          <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-500 dark:text-gray-300">{item.eventType === "ETF_DISTRIBUTION" ? "ETF 분배금" : "주식 배당"}</td>
                          <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-900 dark:text-white">{formatCurrency(item.dividendPerShare)}</td>
                          <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-500 dark:text-gray-300">{item.source}</td>
                          <td className="whitespace-nowrap px-4 py-3 text-right">
                            <button
                              type="button"
                              onClick={() => generateMutation.mutate(item.id)}
                              disabled={generateMutation.isPending}
                              className="rounded-lg bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-700 disabled:bg-gray-300"
                            >
                              내 배당금 계산
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
          )}

          <div className="mb-8">
            <h2 className="mb-4 text-xl font-bold text-gray-900 dark:text-white">{t("dividends.bySecurity")}</h2>
            <div className="overflow-hidden rounded-xl border border-gray-100 bg-white shadow-sm dark:border-gray-700 dark:bg-gray-900">
              {displaySecurities.length === 0 ? (
                <p className="p-6 text-sm text-gray-500 dark:text-gray-400">종목별 배당 데이터가 없습니다.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                    <thead className="bg-gray-50 dark:bg-gray-800">
                      <tr>
                        {["종목명", "티커", "예상 연배당", "누적 배당금", "배당수익률(시가)", "배당수익률(평단)", "기여도"].map((h) => (
                          <th key={h} className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-300">{h}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 bg-white dark:divide-gray-700 dark:bg-gray-900">
                      {displaySecurities.map((s) => (
                        <tr key={s.securityItemId} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                          <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900 dark:text-white">
                            <button
                              type="button"
                              onClick={() => openEventFormForSecurity(s.securityItemId)}
                              className="font-semibold text-emerald-700 hover:text-emerald-800 hover:underline dark:text-emerald-400"
                            >
                              {s.securityName}
                            </button>
                          </td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-300">{s.ticker}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{formatCurrency(s.expectedAnnual)}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{formatCurrency(s.receivedTotal)}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{formatPercent(s.dividendYieldOnEvaluated)}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{formatPercent(s.dividendYieldOnInvested)}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm font-semibold text-emerald-600">{formatPercent(s.contributionRatio)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>

          <div className="mb-8">
            <h2 className="mb-4 text-xl font-bold text-gray-900 dark:text-white">내 배당금 내역</h2>
            <div className="overflow-hidden rounded-xl border border-gray-100 bg-white shadow-sm dark:border-gray-700 dark:bg-gray-900">
              {dividendPayments.length === 0 ? (
                <p className="p-6 text-sm text-gray-500 dark:text-gray-400">아직 생성된 배당 내역이 없습니다.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                    <thead className="bg-gray-50 dark:bg-gray-800">
                      <tr>
                        {["지급일", "계좌", "종목명", "기준일 수량", "주당금액", "세전", "세후", "상태"].map((h) => (
                          <th key={h} className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-300">{h}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 bg-white dark:divide-gray-700 dark:bg-gray-900">
                      {dividendPayments.map((payment) => (
                        <tr key={payment.id} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{payment.paymentDate || "-"}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-300">{payment.accountName}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900 dark:text-white">{payment.securityName}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{payment.quantityAtRecordDate}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{formatCurrency(payment.dividendPerShare)}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{formatCurrency(payment.grossAmountKrw)}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{formatCurrency(payment.netAmountKrw)}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-300">{payment.status}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </AuthGate>
  );
}
