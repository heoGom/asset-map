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
import { useLanguage } from "@/lib/language-provider";

export default function AccountDetailPage() {
  const params = useParams<{ accountId: string }>();
  const { t } = useLanguage();
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
      <div className="min-h-screen bg-gray-50 p-8 dark:bg-gray-950">
        <div className="mx-auto max-w-7xl">
          <div className="mb-6 flex items-center justify-between">
            <div>
              <Link href="/accounts" className="text-sm font-semibold text-emerald-700 hover:text-emerald-800">
                {t("accounts.list")}
              </Link>
              <h1 className="mt-2 text-3xl font-bold text-gray-900 dark:text-white">{account?.name || t("accounts.detail")}</h1>
            </div>
          </div>

          {account && (
            <div className="mb-8 rounded-xl border border-gray-100 bg-white p-6 shadow-sm">
              <dl className="grid grid-cols-1 gap-4 text-sm md:grid-cols-5">
                <div>
                  <dt className="text-gray-500">{t("accounts.brokerName")}</dt>
                  <dd className="mt-1 font-semibold text-gray-900">{account.brokerName || "-"}</dd>
                </div>
                <div>
                  <dt className="text-gray-500">{t("accounts.type")}</dt>
                  <dd className="mt-1 font-semibold text-gray-900">{account.accountType}</dd>
                </div>
                <div>
                  <dt className="text-gray-500">{t("accounts.currency")}</dt>
                  <dd className="mt-1 font-semibold text-gray-900">{account.currency}</dd>
                </div>
                <div className="md:col-span-2">
                  <dt className="text-gray-500">{t("accounts.memo")}</dt>
                  <dd className="mt-1 font-semibold text-gray-900">{account.memo || "-"}</dd>
                </div>
              </dl>
            </div>
          )}

          <div className="mb-8 grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-5">
            <SummaryCard title={t("assets.totalEvaluated")} value={formatCurrency(totalEvaluated)} />
            <SummaryCard title={t("assets.totalInvested")} value={formatCurrency(totalInvested)} />
            <SummaryCard
              title={t("accounts.profitLoss")}
              value={formatCurrency(profitLoss)}
              subValue={formatPercent(profitLossRate)}
              isPositive={profitLoss >= 0}
            />
            <SummaryCard title={t("assets.holdingCount")} value={`${holdings.length}개`} />
            <SummaryCard title={t("accounts.dividendPayments")} value={`${dividendPayments.length}건`} />
          </div>

          <section className="mb-8">
            <h2 className="mb-4 text-xl font-bold text-gray-900 dark:text-white">{t("accounts.holdings")}</h2>
            {holdings.length === 0 ? (
              <EmptyState message={t("accounts.noHoldings")} />
            ) : (
              <HoldingTable holdings={holdings} />
            )}
          </section>

          <section className="mb-8">
            <h2 className="mb-4 text-xl font-bold text-gray-900 dark:text-white">{t("assets.trades")}</h2>
            {trades.length === 0 ? (
              <EmptyState message={t("accounts.noTrades")} />
            ) : (
              <TradeTable trades={trades} />
            )}
          </section>

          <section className="mb-8">
            <h2 className="mb-4 text-xl font-bold text-gray-900 dark:text-white">{t("accounts.dividendPayments")}</h2>
            <DividendPaymentTable payments={dividendPayments} />
          </section>
        </div>
      </div>
    </AuthGate>
  );
}

function EmptyState({ message }: { message: string }) {
  return (
    <div className="rounded-xl border border-dashed border-gray-200 bg-white p-6 text-center text-sm text-gray-500 dark:border-gray-700 dark:bg-gray-900">
      {message}
    </div>
  );
}

function DividendPaymentTable({ payments }: { payments: Awaited<ReturnType<typeof getAccountDividendPayments>> }) {
  const { t } = useLanguage();

  if (payments.length === 0) {
    return <EmptyState message={t("accounts.noDividends")} />;
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
