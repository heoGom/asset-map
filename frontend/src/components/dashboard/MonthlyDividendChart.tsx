"use client";

import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from "recharts";
import { formatCompactCurrency, formatCurrency, toFiniteNumber } from "@/lib/format";
import { useLanguage } from "@/lib/language-provider";

interface MonthlyDividendChartProps {
  data: { month: number; amount: number }[];
}

export default function MonthlyDividendChart({ data }: MonthlyDividendChartProps) {
  const { t } = useLanguage();
  const chartData = data.map((item) => ({
    month: toFiniteNumber(item.month),
    amount: toFiniteNumber(item.amount),
  }));
  const hasData = chartData.some((item) => item.amount > 0);

  return (
    <div className="rounded-xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-900">
      <h3 className="mb-6 text-lg font-bold text-gray-900 dark:text-white">{t("dividends.monthlyStatus")}</h3>
      {!hasData && (
        <div className="flex h-80 items-center justify-center rounded-lg border border-dashed border-gray-200 text-sm text-gray-500 dark:border-gray-700">
          {t("empty.monthlyDividend")}
        </div>
      )}
      {hasData && (
      <div className="h-80 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="currentColor" className="text-gray-100 dark:text-gray-700" />
            <XAxis
              dataKey="month"
              axisLine={false}
              tickLine={false}
              tick={{ fill: "#9ca3af", fontSize: 12 }}
              tickFormatter={(value) => `${value}월`}
              dy={10}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tick={{ fill: "#9ca3af", fontSize: 12 }}
              tickFormatter={formatCompactCurrency}
            />
            <Tooltip
              cursor={{ fill: "#f9fafb" }}
              formatter={(value: any) => [
                formatCurrency(value),
                t("accounts.dividendPayments"),
              ]}
              labelFormatter={(label) => `${label}월`}
              contentStyle={{
                borderRadius: "12px",
                border: "none",
                backgroundColor: "var(--background)",
                color: "var(--foreground)",
                boxShadow: "0 4px 6px -1px rgb(0 0 0 / 0.1)",
              }}
            />
            <Bar
              dataKey="amount"
              fill="#3b82f6"
              radius={[4, 4, 0, 0]}
              barSize={32}
            >
              {chartData.map((entry, index) => (
                <Cell 
                  key={`cell-${index}`} 
                  fill={entry.amount > 0 ? "#3b82f6" : "#e5e7eb"} 
                />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
      )}
    </div>
  );
}
