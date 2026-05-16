import { TradeTransactionResponse } from "@/lib/api/trades";

interface TradeTableProps {
  trades: TradeTransactionResponse[];
}

export default function TradeTable({ trades }: TradeTableProps) {
  const formatCurrency = (value: number) =>
    new Intl.NumberFormat("ko-KR", {
      style: "currency",
      currency: "KRW",
      maximumFractionDigits: 0,
    }).format(value);

  if (trades.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-gray-200 bg-white p-6 text-center text-sm text-gray-500">
        등록된 거래내역이 없습니다.
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-xl border border-gray-100 bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              {["거래일", "계좌", "종목", "유형", "수량", "단가", "금액"].map((header) => (
                <th key={header} className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  {header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white">
            {trades.map((trade) => (
              <tr key={trade.id} className="hover:bg-gray-50">
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{trade.tradeDate}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">{trade.accountName}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                  {trade.ticker} <span className="text-gray-500">{trade.securityName}</span>
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm font-semibold text-gray-900">{trade.tradeType}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{trade.quantity}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{formatCurrency(trade.price)}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">{formatCurrency(trade.netAmount)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
