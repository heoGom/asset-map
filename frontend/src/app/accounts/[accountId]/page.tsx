"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import {
  getAccount,
  getAccountDividendPayments,
  getAccountHoldings,
  getAccountTrades,
} from "@/lib/api/accounts";
import AuthGate from "@/components/auth/AuthGate";
import HoldingTable from "@/components/dashboard/HoldingTable";
import TradeTable from "@/components/trades/TradeTable";
import SummaryCard from "@/components/dashboard/SummaryCard";
import { formatCurrency, formatPercent, toFiniteNumber } from "@/lib/format";

export default function AccountDetailPage() {
  const params = useParams<{ accountId: string }>();
  const accountId = Number(params.accountId);
  const enabled = Number.isFinite(accountId) && accountId > 0;

  const { data: account } = useQuery({
    queryKey: ["account", accountId],
    queryFn: () => getAccount(accountId),
    enabled,
    retry: false,
  });

  const { data: holdings = [] } = useQuery({
    queryKey: ["account-holdings", accountId],
    queryFn: () => getAccountHoldings(accountId),
    enabled,
    retry: false,
  });

  const { data: trades = [] } = useQuery({
    queryKey: ["account-trades", accountId],
    queryFn: () => getAccountTrades(accountId),
    enabled,
    retry: false,
  });

  const { data: dividendPayments = [] } = useQuery({
    queryKey: ["account-dividend-payments", accountId],
    queryFn: () => getAccountDividendPayments(accountId),
    enabled,
    retry: false,
  });

  const totalInvested = holdings.reduce((sum, holding) => sum + toFiniteNumber(holding.investedAmount), 0);
  const totalEvaluated = holdings.reduce((sum, holding) => sum + toFiniteNumber(holding.evaluatedAmount), 0);
  const profitLoss = totalEvaluated - totalInvested;
  const profitLossRate = totalInvested > 0 ? (profitLoss / totalInvested) * 100 : 0;

  return (
    <AuthGate>
      <div className="min-h-screen bg-gray-50 p-8">
        <div className="mx-auto max-w-7xl">
          <div className="mb-6 flex items-center justify-between">
            <div>
              <Link href="/accounts" className="text-sm font-semibold text-emerald-700 hover:text-emerald-800">
                계좌 목록
              </Link>
              <h1 className="mt-2 text-3xl font-bold text-gray-900">{account?.name || "계좌 상세"}</h1>
            </div>
          </div>

          {account && (
            <div className="mb-8 rounded-xl border border-gray-100 bg-white p-6 shadow-sm">
              <dl className="grid grid-cols-1 gap-4 text-sm md:grid-cols-5">
                <div>
                  <dt className="text-gray-500">증권사명</dt>
                  <dd className="mt-1 font-semibold text-gray-900">{account.brokerName || "-"}</dd>
                </div>
                <div>
                  <dt className="text-gray-500">계좌 유형</dt>
                  <dd className="mt-1 font-semibold text-gray-900">{account.accountType}</dd>
                </div>
                <div>
                  <dt className="text-gray-500">통화</dt>
                  <dd className="mt-1 font-semibold text-gray-900">{account.currency}</dd>
                </div>
                <div className="md:col-span-2">
                  <dt className="text-gray-500">메모</dt>
                  <dd className="mt-1 font-semibold text-gray-900">{account.memo || "-"}</dd>
                </div>
              </dl>
            </div>
          )}

          <div className="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-5">
            <SummaryCard title="총 평가금액" value={formatCurrency(totalEvaluated)} />
            <SummaryCard title="총 투자원금" value={formatCurrency(totalInvested)} />
            <SummaryCard
              title="손익"
              value={formatCurrency(profitLoss)}
              subValue={formatPercent(profitLossRate)}
              isPositive={profitLoss >= 0}
            />
            <SummaryCard title="보유 종목 수" value={`${holdings.length}개`} />
            <SummaryCard title="배당 내역" value={`${dividendPayments.length}건`} />
          </div>

          <section className="mb-8">
            <h2 className="mb-4 text-xl font-bold text-gray-900">보유 종목</h2>
            {holdings.length === 0 ? (
              <EmptyState message="이 계좌에 등록된 보유 종목이 없습니다." />
            ) : (
              <HoldingTable holdings={holdings} />
            )}
          </section>

          <section className="mb-8">
            <h2 className="mb-4 text-xl font-bold text-gray-900">거래내역</h2>
            {trades.length === 0 ? (
              <EmptyState message="아직 거래내역이 없습니다. 거래를 입력하면 보유 종목이 자동으로 계산됩니다." />
            ) : (
              <TradeTable trades={trades} />
            )}
          </section>

          <section className="mb-8">
            <h2 className="mb-4 text-xl font-bold text-gray-900">배당내역</h2>
            <DividendPaymentTable payments={dividendPayments} />
          </section>
        </div>
      </div>
    </AuthGate>
  );
}

function EmptyState({ message }: { message: string }) {
  return (
    <div className="rounded-xl border border-dashed border-gray-200 bg-white p-6 text-center text-sm text-gray-500">
      {message}
    </div>
  );
}

function DividendPaymentTable({ payments }: { payments: Awaited<ReturnType<typeof getAccountDividendPayments>> }) {
  if (payments.length === 0) {
    return <EmptyState message="아직 배당 내역이 없습니다." />;
  }

  return (
    <div className="overflow-hidden rounded-xl border border-gray-100 bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              {["지급일", "종목명", "기준일 보유수량", "주당 배당금", "세전 금액", "세금", "세후 금액", "상태"].map((header) => (
                <th key={header} className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  {header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white">
            {payments.map((payment) => (
              <tr key={payment.id} className="hover:bg-gray-50">
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{payment.paymentDate || "-"}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                  {payment.securityName} <span className="text-gray-500">{payment.ticker}</span>
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{toFiniteNumber(payment.quantityAtRecordDate)}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{formatCurrency(payment.dividendPerShare)}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{formatCurrency(payment.grossAmountKrw)}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{formatCurrency(payment.taxAmount)}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm font-semibold text-gray-900">{formatCurrency(payment.netAmountKrw)}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{payment.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
