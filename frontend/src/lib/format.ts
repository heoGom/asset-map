export const toFiniteNumber = (value: unknown, fallback = 0) => {
  const numberValue = typeof value === "number" ? value : Number(value);
  return Number.isFinite(numberValue) ? numberValue : fallback;
};

export const formatCurrency = (value: unknown, options?: Intl.NumberFormatOptions) =>
  new Intl.NumberFormat("ko-KR", {
    style: "currency",
    currency: "KRW",
    maximumFractionDigits: 0,
    ...options,
  }).format(toFiniteNumber(value));

export const formatCompactCurrency = (value: unknown) =>
  formatCurrency(value, { notation: "compact" });

export const formatPercent = (value: unknown) => {
  const percentValue = toFiniteNumber(value);
  return `${new Intl.NumberFormat("ko-KR", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(percentValue)}%`;
};
