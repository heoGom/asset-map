"use client";

import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { formatCompactCurrency, formatCurrency, toFiniteNumber } from "@/lib/format";
import { useLanguage } from "@/lib/language-provider";

interface TimelineChartProps {
  data: { date: string; totalAssetAmount: number }[];
}

export default function TimelineChart({ data }: TimelineChartProps) {
  const { t } = useLanguage();
  const chartData = data
    .map((item) => ({
      date: item.date,
      totalAssetAmount: toFiniteNumber(item.totalAssetAmount),
    }))
    .filter((item) => item.date);

  const hasData = chartData.some((item) => item.totalAssetAmount > 0);

  return (
    <div className="rounded-xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-900">
      <h3 className="mb-6 text-lg font-bold text-gray-900 dark:text-white">{t("assets.timeline")}</h3>
      {!hasData && (
        <div className="flex h-80 items-center justify-center rounded-lg border border-dashed border-gray-200 text-sm text-gray-500 dark:border-gray-700">
          {t("empty.timeline")}
        </div>
      )}
      {hasData && (
      <div className="h-80 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="currentColor" className="text-gray-100 dark:text-gray-700" />
            <XAxis
              dataKey="date"
              axisLine={false}
              tickLine={false}
              tick={{ fill: "#9ca3af", fontSize: 12 }}
              dy={10}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tick={{ fill: "#9ca3af", fontSize: 12 }}
              tickFormatter={formatCompactCurrency}
            />
            <Tooltip
              formatter={(value: unknown) => [
                formatCurrency(value),
                t("assets.totalEvaluated"),
              ]}
              contentStyle={{
                borderRadius: "12px",
                border: "none",
                backgroundColor: "var(--background)",
                color: "var(--foreground)",
                boxShadow: "0 4px 6px -1px rgb(0 0 0 / 0.1)",
              }}
            />
            <Line
              type="monotone"
              dataKey="totalAssetAmount"
              stroke="#059669"
              strokeWidth={3}
              dot={{ r: 4, fill: "#059669", strokeWidth: 2, stroke: "#fff" }}
              activeDot={{ r: 6, strokeWidth: 0 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
      )}
    </div>
  );
}
