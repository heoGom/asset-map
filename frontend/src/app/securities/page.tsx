"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import AuthGate from "@/components/auth/AuthGate";
import { getCategoryLabel } from "@/lib/category-labels";
import {
  createSecurityClassification,
  getSecurityClassifications,
  updateSecurityClassification,
} from "@/lib/api/security-classifications";
import { createSecurityItem, getSecurityItems } from "@/lib/api/security-items";
import { useLanguage } from "@/lib/language-provider";
import SecurityCombobox from "@/components/securities/SecurityCombobox";

const securityTypes = ["STOCK", "ETF", "REIT", "BOND", "CASH", "CRYPTO"];
const countryGroups = ["DOMESTIC", "OVERSEAS", "UNKNOWN"];
const assetGroups = ["STOCK", "ETF", "CASH", "BOND", "REIT", "CRYPTO", "UNKNOWN"];
const sectors = [
  "SEMICONDUCTOR",
  "DIVIDEND_ETF",
  "FINANCE",
  "INFRASTRUCTURE",
  "CONSUMER",
  "TECH",
  "HOLDING_COMPANY",
  "CASH",
  "OTHER",
  "UNKNOWN",
];
const strategyTypes = ["DIVIDEND", "GROWTH", "VALUE", "INDEX", "MIXED", "CASH", "UNKNOWN"];

const inputClass =
  "mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-sm text-gray-900 shadow-sm dark:border-gray-700 dark:bg-gray-800 dark:text-white";
const labelClass = "block text-sm font-medium text-gray-700 dark:text-gray-200";

export default function SecuritiesPage() {
  const queryClient = useQueryClient();
  const { language } = useLanguage();
  const [securityForm, setSecurityForm] = useState({
    name: "",
    ticker: "",
    market: "KOSPI",
    country: "KOREA",
    currency: "KRW",
    securityType: "STOCK",
  });
  const [classificationForm, setClassificationForm] = useState({
    securityItemId: "",
    countryGroup: "DOMESTIC",
    assetGroup: "STOCK",
    sector: "OTHER",
    strategyType: "UNKNOWN",
    listingCountry: "KR",
    exposureCountry: "KR",
    exposureRegion: "KR",
    tradingCurrency: "KRW",
    currencyExposure: "KRW",
    underlyingIndex: "",
    hedged: false,
  });

  const { data: securities = [] } = useQuery({
    queryKey: ["securities"],
    queryFn: getSecurityItems,
    retry: false,
  });

  const { data: classifications = [] } = useQuery({
    queryKey: ["security-classifications"],
    queryFn: getSecurityClassifications,
    retry: false,
  });

  const classificationBySecurityId = useMemo(
    () => new Map(classifications.map((item) => [item.securityItemId, item])),
    [classifications]
  );

  useEffect(() => {
    if (classificationForm.securityItemId || securities.length === 0) {
      return;
    }
    const firstSecurity = securities[0];
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setClassificationForm((prev) => ({
      ...prev,
      securityItemId: String(firstSecurity.id),
      tradingCurrency: firstSecurity.currency || prev.tradingCurrency,
      currencyExposure: firstSecurity.currency || prev.currencyExposure,
    }));
  }, [classificationForm.securityItemId, securities]);

  useEffect(() => {
    const securityId = Number(classificationForm.securityItemId);
    const existing = classificationBySecurityId.get(securityId);
    const security = securities.find((item) => item.id === securityId);
    if (!securityId) {
      return;
    }
    if (existing) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setClassificationForm((prev) => ({
        ...prev,
        countryGroup: existing.countryGroup,
        assetGroup: existing.assetGroup,
        sector: existing.sector,
        strategyType: existing.strategyType,
        listingCountry: existing.listingCountry || "",
        exposureCountry: existing.exposureCountry || "",
        exposureRegion: existing.exposureRegion || "",
        tradingCurrency: existing.tradingCurrency || security?.currency || "KRW",
        currencyExposure: existing.currencyExposure || security?.currency || "KRW",
        underlyingIndex: existing.underlyingIndex || "",
        hedged: Boolean(existing.hedged),
      }));
      return;
    }
    setClassificationForm((prev) => ({
      ...prev,
      tradingCurrency: security?.currency || "KRW",
      currencyExposure: security?.currency || "KRW",
    }));
  }, [classificationForm.securityItemId, classificationBySecurityId, securities]);

  const createSecurityMutation = useMutation({
    mutationFn: createSecurityItem,
    onSuccess: (created) => {
      queryClient.invalidateQueries({ queryKey: ["securities"] });
      setSecurityForm({
        name: "",
        ticker: "",
        market: "KOSPI",
        country: "KOREA",
        currency: "KRW",
        securityType: "STOCK",
      });
      setClassificationForm((prev) => ({
        ...prev,
        securityItemId: String(created.id),
        tradingCurrency: created.currency,
        currencyExposure: created.currency,
      }));
    },
  });

  const saveClassificationMutation = useMutation({
    mutationFn: () => {
      const securityItemId = Number(classificationForm.securityItemId);
      const existing = classificationBySecurityId.get(securityItemId);
      const payload = {
        countryGroup: classificationForm.countryGroup,
        assetGroup: classificationForm.assetGroup,
        sector: classificationForm.sector,
        strategyType: classificationForm.strategyType,
        listingCountry: classificationForm.listingCountry || undefined,
        exposureCountry: classificationForm.exposureCountry || undefined,
        exposureRegion: classificationForm.exposureRegion || undefined,
        tradingCurrency: classificationForm.tradingCurrency || undefined,
        currencyExposure: classificationForm.currencyExposure || undefined,
        underlyingIndex: classificationForm.underlyingIndex || undefined,
        hedged: classificationForm.hedged,
      };
      if (existing) {
        return updateSecurityClassification(existing.id, payload);
      }
      return createSecurityClassification({ securityItemId, ...payload });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["security-classifications"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-country"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-type"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-sector"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-strategy"] });
    },
  });

  const handleSecuritySubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    createSecurityMutation.mutate({
      ...securityForm,
      ticker: securityForm.ticker.trim(),
      name: securityForm.name.trim(),
      market: securityForm.market.trim() || undefined,
      country: securityForm.country.trim() || undefined,
      currency: securityForm.currency.trim() || "KRW",
    });
  };

  const handleClassificationSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!classificationForm.securityItemId) {
      return;
    }
    saveClassificationMutation.mutate();
  };

  return (
    <AuthGate>
      <div className="min-h-screen bg-gray-50 p-8 dark:bg-gray-950">
        <div className="mx-auto max-w-7xl">
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white">종목 관리</h1>
            <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
              외부 API 없이 종목 마스터와 분석 분류를 직접 관리합니다.
            </p>
          </div>

          <div className="mb-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
            <form onSubmit={handleSecuritySubmit} className="space-y-4 rounded-xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-900">
              <div>
                <h2 className="text-xl font-bold text-gray-900 dark:text-white">종목 등록</h2>
                <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">거래와 배당 입력에서 선택할 종목을 먼저 등록합니다.</p>
              </div>
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <label className={labelClass}>
                  종목명
                  <input required value={securityForm.name} onChange={(event) => setSecurityForm({ ...securityForm, name: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  종목코드
                  <input required value={securityForm.ticker} onChange={(event) => setSecurityForm({ ...securityForm, ticker: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  시장
                  <input value={securityForm.market} onChange={(event) => setSecurityForm({ ...securityForm, market: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  국가
                  <input value={securityForm.country} onChange={(event) => setSecurityForm({ ...securityForm, country: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  통화
                  <input required value={securityForm.currency} onChange={(event) => setSecurityForm({ ...securityForm, currency: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  종목유형
                  <select value={securityForm.securityType} onChange={(event) => setSecurityForm({ ...securityForm, securityType: event.target.value })} className={inputClass}>
                    {securityTypes.map((type) => (
                      <option key={type} value={type}>{getCategoryLabel(type, language)}</option>
                    ))}
                  </select>
                </label>
              </div>
              {createSecurityMutation.isError && <p className="text-sm text-rose-600">종목 등록에 실패했습니다.</p>}
              <button type="submit" disabled={createSecurityMutation.isPending} className="w-full rounded-xl bg-emerald-600 p-2 font-bold text-white hover:bg-emerald-700 disabled:bg-gray-300">
                {createSecurityMutation.isPending ? "등록 중" : "종목 등록"}
              </button>
            </form>

            <form onSubmit={handleClassificationSubmit} className="space-y-4 rounded-xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-900">
              <div>
                <h2 className="text-xl font-bold text-gray-900 dark:text-white">종목 분류 입력/수정</h2>
                <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">상장국가와 투자노출국가를 분리해 국내 상장 해외 ETF를 표현합니다.</p>
              </div>
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                <label className={labelClass}>
                  종목
                  <SecurityCombobox
                    options={securities}
                    value={classificationForm.securityItemId}
                    placeholder="분류할 종목 검색"
                    onChange={(securityItemId) => setClassificationForm({ ...classificationForm, securityItemId })}
                  />
                </label>
                <label className={labelClass}>
                  국가 구분
                  <select value={classificationForm.countryGroup} onChange={(event) => setClassificationForm({ ...classificationForm, countryGroup: event.target.value })} className={inputClass}>
                    {countryGroups.map((item) => <option key={item} value={item}>{getCategoryLabel(item, language)}</option>)}
                  </select>
                </label>
                <label className={labelClass}>
                  자산유형
                  <select value={classificationForm.assetGroup} onChange={(event) => setClassificationForm({ ...classificationForm, assetGroup: event.target.value })} className={inputClass}>
                    {assetGroups.map((item) => <option key={item} value={item}>{getCategoryLabel(item, language)}</option>)}
                  </select>
                </label>
                <label className={labelClass}>
                  섹터
                  <select value={classificationForm.sector} onChange={(event) => setClassificationForm({ ...classificationForm, sector: event.target.value })} className={inputClass}>
                    {sectors.map((item) => <option key={item} value={item}>{getCategoryLabel(item, language)}</option>)}
                  </select>
                </label>
                <label className={labelClass}>
                  전략
                  <select value={classificationForm.strategyType} onChange={(event) => setClassificationForm({ ...classificationForm, strategyType: event.target.value })} className={inputClass}>
                    {strategyTypes.map((item) => <option key={item} value={item}>{getCategoryLabel(item, language)}</option>)}
                  </select>
                </label>
                <label className={labelClass}>
                  상장국가
                  <input value={classificationForm.listingCountry} onChange={(event) => setClassificationForm({ ...classificationForm, listingCountry: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  투자노출국가
                  <input value={classificationForm.exposureCountry} onChange={(event) => setClassificationForm({ ...classificationForm, exposureCountry: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  투자노출지역
                  <input value={classificationForm.exposureRegion} onChange={(event) => setClassificationForm({ ...classificationForm, exposureRegion: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  거래통화
                  <input value={classificationForm.tradingCurrency} onChange={(event) => setClassificationForm({ ...classificationForm, tradingCurrency: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  환노출통화
                  <input value={classificationForm.currencyExposure} onChange={(event) => setClassificationForm({ ...classificationForm, currencyExposure: event.target.value })} className={inputClass} />
                </label>
                <label className={labelClass}>
                  기초지수
                  <input value={classificationForm.underlyingIndex} onChange={(event) => setClassificationForm({ ...classificationForm, underlyingIndex: event.target.value })} className={inputClass} />
                </label>
                <label className="flex items-center gap-2 pt-7 text-sm font-medium text-gray-700 dark:text-gray-200">
                  <input type="checkbox" checked={classificationForm.hedged} onChange={(event) => setClassificationForm({ ...classificationForm, hedged: event.target.checked })} />
                  환헤지
                </label>
              </div>
              {saveClassificationMutation.isError && <p className="text-sm text-rose-600">종목 분류 저장에 실패했습니다.</p>}
              {saveClassificationMutation.isSuccess && <p className="text-sm text-emerald-600">종목 분류를 저장했습니다.</p>}
              <button type="submit" disabled={saveClassificationMutation.isPending || !classificationForm.securityItemId} className="w-full rounded-xl bg-emerald-600 p-2 font-bold text-white hover:bg-emerald-700 disabled:bg-gray-300">
                {saveClassificationMutation.isPending ? "저장 중" : "분류 저장"}
              </button>
            </form>
          </div>

          <div className="overflow-hidden rounded-xl border border-gray-100 bg-white shadow-sm dark:border-gray-700 dark:bg-gray-900">
            <div className="border-b border-gray-100 p-6 dark:border-gray-700">
              <h2 className="text-xl font-bold text-gray-900 dark:text-white">종목 목록</h2>
            </div>
            {securities.length === 0 ? (
              <p className="p-6 text-sm text-gray-500 dark:text-gray-400">등록된 종목이 없습니다.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                  <thead className="bg-gray-50 dark:bg-gray-800">
                    <tr>
                      {["종목명", "코드", "시장", "국가", "유형", "국가 구분", "자산유형", "섹터", "전략", "상장/노출"].map((header) => (
                        <th key={header} className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-300">{header}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 bg-white dark:divide-gray-700 dark:bg-gray-900">
                    {securities.map((security) => {
                      const classification = classificationBySecurityId.get(security.id);
                      return (
                        <tr key={security.id} className="hover:bg-gray-50 dark:hover:bg-gray-800">
                          <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900 dark:text-white">{security.name}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-300">{security.ticker}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-300">{security.market || "-"}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-300">{security.country || "-"}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{getCategoryLabel(security.securityType, language)}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{classification ? getCategoryLabel(classification.countryGroup, language) : "미분류"}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{classification ? getCategoryLabel(classification.assetGroup, language) : "미분류"}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{classification ? getCategoryLabel(classification.sector, language) : "미분류"}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900 dark:text-white">{classification ? getCategoryLabel(classification.strategyType, language) : "미분류"}</td>
                          <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-300">
                            {classification ? `${classification.listingCountry || "-"} / ${classification.exposureCountry || "-"}` : "-"}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </AuthGate>
  );
}
