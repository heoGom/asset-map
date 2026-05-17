interface SummaryCardProps {
  title: string;
  value: string;
  subValue?: string;
  isPositive?: boolean;
}

export default function SummaryCard({
  title,
  value,
  subValue,
  isPositive,
}: SummaryCardProps) {
  return (
    <div className="rounded-xl border border-gray-100 bg-white p-6 shadow-sm dark:border-gray-700 dark:bg-gray-900">
      <p className="text-sm font-medium text-gray-500">{title}</p>
      <div className="mt-2 flex items-baseline gap-2">
        <h3 className="text-2xl font-bold text-gray-900 dark:text-white">{value}</h3>
        {subValue && (
          <span
            className={`text-sm font-semibold ${
              isPositive === true
                ? "text-emerald-600"
                : isPositive === false
                ? "text-rose-600"
                : "text-gray-500"
            }`}
          >
            {subValue}
          </span>
        )}
      </div>
    </div>
  );
}
