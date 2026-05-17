"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getAccounts, createAccount, deleteAccount, AccountType, AccountCreateRequest } from "@/lib/api/accounts";
import { useState } from "react";
import Link from "next/link";
import AuthGate from "@/components/auth/AuthGate";

export default function AccountsPage() {
  const queryClient = useQueryClient();
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [newAccount, setNewAccount] = useState<Partial<AccountCreateRequest>>({
    accountType: AccountType.GENERAL,
    currency: "KRW",
  });

  const { data: accounts } = useQuery({
    queryKey: ["accounts"],
    queryFn: getAccounts,
    retry: false,
  });

  const createMutation = useMutation({
    mutationFn: createAccount,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["accounts"] });
      setIsFormOpen(false);
      setNewAccount({ accountType: AccountType.GENERAL, currency: "KRW" });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteAccount,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["accounts"] });
    },
  });

  const displayAccounts = accounts || [];

  return (
    <AuthGate>
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="mx-auto max-w-5xl">
        <div className="mb-8 flex items-center justify-between">
          <h1 className="text-3xl font-bold text-gray-900">계좌 관리</h1>
          <button
            onClick={() => setIsFormOpen(!isFormOpen)}
            className="rounded-xl bg-emerald-600 px-4 py-2 font-bold text-white shadow-sm hover:bg-emerald-700 transition-all"
          >
            {isFormOpen ? "닫기" : "새 계좌 추가"}
          </button>
        </div>

        {isFormOpen && (
          <div className="mb-8 rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
            <h2 className="mb-4 text-xl font-bold text-gray-900">신규 계좌 등록</h2>
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <label className="block text-sm font-medium text-gray-700">계좌 이름</label>
                <input
                  type="text"
                  value={newAccount.name || ""}
                  onChange={(e) => setNewAccount({ ...newAccount, name: e.target.value })}
                  className="mt-1 block w-full rounded-lg border border-gray-300 p-2 shadow-sm focus:border-emerald-500 focus:ring-emerald-500"
                  placeholder="예: 메인 주식 계좌"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">금융사</label>
                <input
                  type="text"
                  value={newAccount.brokerName || ""}
                  onChange={(e) => setNewAccount({ ...newAccount, brokerName: e.target.value })}
                  className="mt-1 block w-full rounded-lg border border-gray-300 p-2 shadow-sm focus:border-emerald-500 focus:ring-emerald-500"
                  placeholder="예: 신한투자증권"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">계좌 유형</label>
                <select
                  value={newAccount.accountType}
                  onChange={(e) => setNewAccount({ ...newAccount, accountType: e.target.value as AccountType })}
                  className="mt-1 block w-full rounded-lg border border-gray-300 p-2 shadow-sm focus:border-emerald-500 focus:ring-emerald-500"
                >
                  {Object.values(AccountType).map((type) => (
                    <option key={type} value={type}>{type}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">통화</label>
                <input
                  type="text"
                  value={newAccount.currency || ""}
                  onChange={(e) => setNewAccount({ ...newAccount, currency: e.target.value })}
                  className="mt-1 block w-full rounded-lg border border-gray-300 p-2 shadow-sm focus:border-emerald-500 focus:ring-emerald-500"
                  placeholder="KRW, USD 등"
                />
              </div>
            </div>
            <div className="mt-6 flex justify-end">
              <button
                onClick={() => createMutation.mutate(newAccount as AccountCreateRequest)}
                className="rounded-xl bg-gray-900 px-6 py-2 font-bold text-white hover:bg-gray-800 transition-all"
                disabled={!newAccount.name}
              >
                등록하기
              </button>
            </div>
          </div>
        )}

        <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
          {displayAccounts.map((account) => (
            <div key={account.id} className="group relative rounded-2xl border border-gray-100 bg-white p-6 shadow-sm transition-all hover:border-emerald-100 hover:shadow-md">
              <div className="flex items-start justify-between">
                <div>
                  <span className="inline-flex items-center rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-medium text-gray-800">
                    {account.accountType}
                  </span>
                  <h3 className="mt-2 text-xl font-bold text-gray-900">{account.name}</h3>
                  <p className="mt-1 text-sm text-gray-500">{account.brokerName || "금융사 정보 없음"}</p>
                </div>
                <button
                  onClick={(event) => {
                    event.preventDefault();
                    deleteMutation.mutate(account.id);
                  }}
                  className="text-gray-400 hover:text-rose-600 transition-colors"
                  aria-label={`${account.name} 삭제`}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="h-5 w-5">
                    <path strokeLinecap="round" strokeLinejoin="round" d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0" />
                  </svg>
                </button>
              </div>
              {account.memo && (
                <div className="mt-4 border-t border-gray-50 pt-4 text-sm text-gray-600">
                  {account.memo}
                </div>
              )}
              <div className="mt-5">
                <Link
                  href={`/accounts/${account.id}`}
                  className="inline-flex rounded-lg bg-gray-900 px-3 py-2 text-sm font-semibold text-white hover:bg-gray-800"
                >
                  상세보기
                </Link>
              </div>
            </div>
          ))}
        </div>
        {displayAccounts.length === 0 && (
          <div className="rounded-xl border border-dashed border-gray-200 bg-white p-6 text-center text-sm text-gray-500">
            등록된 계좌가 없습니다.
          </div>
        )}
      </div>
    </div>
    </AuthGate>
  );
}
