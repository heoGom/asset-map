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

interface MonthlyDividendChartProps {
  data: { month: number; amount: number }[];
}

export default function MonthlyDividendChart({ data }: MonthlyDividendChartProps) {
  const chartData = data.map((item) => ({
    month: toFiniteNumber(item.month),
    amount: toFiniteNumber(item.amount),
  }));
  const hasData = chartData.some((item) => item.amount > 0);

  return (
    <div className="rounded-xl border border-gray-100 bg-white p-6 shadow-sm">
      <h3 className="mb-6 text-lg font-bold text-gray-900">월별 배당금 현황</h3>
      {!hasData && (
        <div className="flex h-80 items-center justify-center rounded-lg border border-dashed border-gray-200 text-sm text-gray-500">
          해당 연도의 지급 배당금 데이터가 없습니다.
        </div>
      )}
      {hasData && (
      <div className="h-80 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f3f4f6" />
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
                "배당금",
              ]}
              labelFormatter={(label) => `${label}월`}
              contentStyle={{
                borderRadius: "12px",
                border: "none",
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
