"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/lib/auth-provider";
import { getAccounts } from "@/lib/api/accounts";
import { searchSecurityItems } from "@/lib/api/security-items";
import { createHolding } from "@/lib/api/holdings";

export default function AddHoldingForm({ onSuccess }: { onSuccess: () => void }) {
  const { userId } = useAuth();
  const queryClient = useQueryClient();
  const [keyword, setKeyword] = useState("");
  const [form, setForm] = useState({
    accountId: "",
    securityItemId: "",
    quantity: "",
    averagePrice: "",
    currentPrice: "",
    currency: "KRW",
  });

  const { data: accounts } = useQuery({ queryKey: ["accounts"], queryFn: getAccounts });
  const { data: searchResults } = useQuery({
    queryKey: ["securities", keyword],
    queryFn: () => searchSecurityItems(keyword),
    enabled: keyword.length > 1,
  });

  const mutation = useMutation({
    mutationFn: createHolding,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["holdings"] });
      onSuccess();
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    mutation.mutate({
      userId,
      accountId: Number(form.accountId),
      securityItemId: Number(form.securityItemId),
      quantity: Number(form.quantity),
      averagePrice: Number(form.averagePrice),
      currentPrice: Number(form.currentPrice),
      currency: form.currency,
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 rounded-2xl border border-gray-100 bg-white p-6 shadow-sm">
      <h2 className="text-xl font-bold">보유 종목 추가</h2>
      
      <div>
        <label className="block text-sm font-medium text-gray-700">계좌 선택</label>
        <select 
          required 
          className="mt-1 block w-full rounded-lg border border-gray-300 p-2"
          onChange={(e) => setForm({...form, accountId: e.target.value})}
        >
          <option value="">계좌 선택</option>
          {accounts?.map(acc => <option key={acc.id} value={acc.id}>{acc.name}</option>)}
        </select>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">종목 검색</label>
        <input 
          type="text" 
          className="mt-1 block w-full rounded-lg border border-gray-300 p-2"
          placeholder="티커 또는 이름 검색"
          onChange={(e) => setKeyword(e.target.value)}
        />
        {searchResults && (
          <select 
            className="mt-1 block w-full rounded-lg border border-gray-300 p-2"
            onChange={(e) => setForm({...form, securityItemId: e.target.value})}
          >
            <option value="">종목 선택</option>
            {searchResults.map(item => <option key={item.id} value={item.id}>{item.ticker} - {item.name}</option>)}
          </select>
        )}
      </div>

      <div className="grid grid-cols-2 gap-4">
        <input type="number" placeholder="수량" className="rounded-lg border border-gray-300 p-2" onChange={(e) => setForm({...form, quantity: e.target.value})} />
        <input type="number" placeholder="평균단가" className="rounded-lg border border-gray-300 p-2" onChange={(e) => setForm({...form, averagePrice: e.target.value})} />
        <input type="number" placeholder="현재가" className="rounded-lg border border-gray-300 p-2" onChange={(e) => setForm({...form, currentPrice: e.target.value})} />
        <input type="text" placeholder="통화" value={form.currency} className="rounded-lg border border-gray-300 p-2" onChange={(e) => setForm({...form, currency: e.target.value})} />
      </div>

      <button type="submit" className="w-full rounded-xl bg-emerald-600 p-2 font-bold text-white hover:bg-emerald-700">등록</button>
    </form>
  );
}
