import { HoldingResponse } from "@/lib/api/holdings";

interface HoldingTableProps {
  holdings: HoldingResponse[];
}

export default function HoldingTable({ holdings }: HoldingTableProps) {
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

  return (
    <div className="overflow-hidden rounded-xl border border-gray-100 bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              {[
                "종목명",
                "티커",
                "계좌",
                "수량",
                "평균단가",
                "현재가",
                "투자원금",
                "평가금액",
                "손익",
                "수익률",
              ].map((header) => (
                <th
                  key={header}
                  className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500"
                >
                  {header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white">
            {holdings.map((holding) => (
              <tr key={holding.id} className="hover:bg-gray-50">
                <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">
                  {holding.securityName}
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                  {holding.ticker}
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                  {holding.accountName}
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                  {holding.quantity}
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                  {formatCurrency(holding.averagePrice)}
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                  {formatCurrency(holding.currentPrice)}
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                  {formatCurrency(holding.investedAmount)}
                </td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                  {formatCurrency(holding.evaluatedAmount)}
                </td>
                <td
                  className={`whitespace-nowrap px-6 py-4 text-sm font-semibold ${
                    holding.profitLoss >= 0 ? "text-emerald-600" : "text-rose-600"
                  }`}
                >
                  {formatCurrency(holding.profitLoss)}
                </td>
                <td
                  className={`whitespace-nowrap px-6 py-4 text-sm font-semibold ${
                    holding.profitLossRate >= 0 ? "text-emerald-600" : "text-rose-600"
                  }`}
                >
                  {formatPercent(holding.profitLossRate)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
