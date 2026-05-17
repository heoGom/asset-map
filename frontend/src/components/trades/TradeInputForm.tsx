"use client";

import { FormEvent, useEffect, useState } from "react";
import Link from "next/link";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getAccounts } from "@/lib/api/accounts";
import { getSecurityItems } from "@/lib/api/security-items";
import { createTrade, TransactionSource, TradeType } from "@/lib/api/trades";

interface TradeInputFormProps {
  onSuccess: () => void;
  initialAccountId?: number;
  initialSecurityItemId?: number;
}

export default function TradeInputForm({ onSuccess, initialAccountId, initialSecurityItemId }: TradeInputFormProps) {
  const queryClient = useQueryClient();
  const today = new Date().toISOString().slice(0, 10);
  const [form, setForm] = useState({
    accountId: "",
    securityItemId: "",
    tradeDate: today,
    tradeType: TradeType.INITIAL,
    quantity: "",
    price: "",
    fee: "0",
    tax: "0",
    memo: "",
  });

  const { data: accounts = [], isLoading: isAccountsLoading } = useQuery({
    queryKey: ["accounts"],
    queryFn: getAccounts,
    retry: false,
  });

  const { data: securities = [], isLoading: isSecuritiesLoading } = useQuery({
    queryKey: ["securities"],
    queryFn: getSecurityItems,
    retry: false,
  });

  const selectedSecurity = securities.find((item) => item.id === Number(form.securityItemId));
  const currency = selectedSecurity?.currency || "KRW";
  const hasAccounts = accounts.length > 0;
  const hasSecurities = securities.length > 0;

  const mutation = useMutation({
    mutationFn: createTrade,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["trades"] });
      queryClient.invalidateQueries({ queryKey: ["holdings"] });
      queryClient.invalidateQueries({ queryKey: ["asset-summary"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-account"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-country"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-type"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-sector"] });
      queryClient.invalidateQueries({ queryKey: ["asset-by-strategy"] });
      queryClient.invalidateQueries({ queryKey: ["asset-timeline"] });
      onSuccess();
    },
  });

  const isSubmitDisabled = mutation.isPending || !hasAccounts || !hasSecurities;

  useEffect(() => {
    setForm((prev) => ({
      ...prev,
      accountId: initialAccountId ? String(initialAccountId) : prev.accountId,
      securityItemId: initialSecurityItemId ? String(initialSecurityItemId) : prev.securityItemId,
      tradeType: TradeType.BUY,
    }));
  }, [initialAccountId, initialSecurityItemId]);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isSubmitDisabled) {
      return;
    }

    mutation.mutate({
      accountId: Number(form.accountId),
      securityItemId: Number(form.securityItemId),
      tradeDate: form.tradeDate,
      tradeType: form.tradeType,
      quantity: Number(form.quantity),
      price: Number(form.price),
      fee: Number(form.fee || 0),
      tax: Number(form.tax || 0),
      currency,
      source: form.tradeType === TradeType.INITIAL ? TransactionSource.INITIAL : TransactionSource.MANUAL,
      memo: form.memo || undefined,
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 rounded-xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-900">
      <div>
        <h2 className="text-xl font-bold text-gray-900 dark:text-white">거래 입력</h2>
        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">초기 보유 등록은 거래유형 초기 보유를 사용합니다.</p>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
          계좌
          <select
            required
            value={form.accountId}
            disabled={!hasAccounts}
            onChange={(event) => setForm({ ...form, accountId: event.target.value })}
            className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
          >
            <option value="">계좌 선택</option>
            {accounts.map((account) => (
              <option key={account.id} value={account.id}>{account.name}</option>
            ))}
          </select>
          {!isAccountsLoading && !hasAccounts && (
            <p className="mt-1 text-xs text-amber-700">등록된 계좌가 없습니다</p>
          )}
        </label>

        <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
          <span className="flex items-center justify-between gap-2">
            <span>종목</span>
            <Link href="/securities" className="text-xs font-semibold text-emerald-600 hover:text-emerald-700">
              새 종목 등록
            </Link>
          </span>
          <select
            required
            value={form.securityItemId}
            disabled={!hasSecurities}
            onChange={(event) => setForm({ ...form, securityItemId: event.target.value })}
            className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
          >
            <option value="">종목 선택</option>
            {securities.map((security) => (
              <option key={security.id} value={security.id}>
                {security.ticker} - {security.name}
              </option>
            ))}
          </select>
          {!isSecuritiesLoading && !hasSecurities && (
            <p className="mt-1 text-xs text-amber-700">등록된 종목이 없습니다</p>
          )}
        </label>

        <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
          거래일
          <input
            required
            type="date"
            value={form.tradeDate}
            onChange={(event) => setForm({ ...form, tradeDate: event.target.value })}
            className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
          />
        </label>

        <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
          거래유형
          <select
            required
            value={form.tradeType}
            onChange={(event) => setForm({ ...form, tradeType: event.target.value as TradeType })}
            className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
          >
            {Object.values(TradeType).map((type) => (
              <option key={type} value={type}>
                {type === TradeType.INITIAL ? "초기 보유" : type === TradeType.BUY ? "매수" : "매도"}
              </option>
            ))}
          </select>
        </label>

        <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
          수량
          <input
            required
            min="0"
            step="0.000001"
            type="number"
            value={form.quantity}
            onChange={(event) => setForm({ ...form, quantity: event.target.value })}
            className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
          />
        </label>

        <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
          단가
          <input
            required
            min="0"
            step="0.000001"
            type="number"
            value={form.price}
            onChange={(event) => setForm({ ...form, price: event.target.value })}
            className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
          />
        </label>

        <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
          수수료
          <input
            min="0"
            step="0.01"
            type="number"
            value={form.fee}
            onChange={(event) => setForm({ ...form, fee: event.target.value })}
            className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
          />
        </label>

        <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
          세금
          <input
            min="0"
            step="0.01"
            type="number"
            value={form.tax}
            onChange={(event) => setForm({ ...form, tax: event.target.value })}
            className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
          />
        </label>

        <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
          통화
          <input
            readOnly
            value={currency}
            className="mt-1 block w-full rounded-lg border border-gray-200 bg-gray-50 p-2 text-gray-600 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-300"
          />
        </label>
      </div>

      <label className="block text-sm font-medium text-gray-700 dark:text-gray-200">
        메모
        <input
          type="text"
          value={form.memo}
          onChange={(event) => setForm({ ...form, memo: event.target.value })}
          className="mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-gray-900 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
        />
      </label>

      {mutation.isError && (
        <p className="text-sm text-rose-600">거래 등록에 실패했습니다. 입력값과 보유수량을 확인하세요.</p>
      )}

      <button
        type="submit"
        disabled={isSubmitDisabled}
        className="w-full rounded-xl bg-emerald-600 p-2 font-bold text-white hover:bg-emerald-700 disabled:bg-gray-300"
      >
        {mutation.isPending ? "등록 중" : "거래 등록"}
      </button>
    </form>
  );
}
