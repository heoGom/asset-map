"use client";

import { useEffect, useId, useMemo, useRef, useState } from "react";

export interface SecurityComboboxOption {
  id: number;
  ticker: string;
  name: string;
  market?: string;
  currency?: string;
  securityType?: string;
}

interface SecurityComboboxProps {
  options: SecurityComboboxOption[];
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
  placeholder?: string;
  emptyMessage?: string;
  maxVisible?: number;
}

const inputClass =
  "mt-1 block w-full rounded-lg border border-gray-300 bg-white p-2 text-sm text-gray-900 shadow-sm outline-none transition focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100 disabled:bg-gray-100 disabled:text-gray-400 dark:border-gray-700 dark:bg-gray-800 dark:text-white dark:focus:border-emerald-400 dark:focus:ring-emerald-900 dark:disabled:bg-gray-800";

export default function SecurityCombobox({
  options,
  value,
  onChange,
  disabled = false,
  placeholder = "종목 검색",
  emptyMessage = "일치하는 종목이 없습니다",
  maxVisible = 50,
}: SecurityComboboxProps) {
  const listboxId = useId();
  const wrapperRef = useRef<HTMLDivElement>(null);
  const [query, setQuery] = useState("");
  const [isOpen, setIsOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(0);

  const selectedOption = useMemo(
    () => options.find((option) => option.id === Number(value)),
    [options, value]
  );
  const inputValue = isOpen ? query : selectedOption ? optionLabel(selectedOption) : "";

  useEffect(() => {
    const handlePointerDown = (event: PointerEvent) => {
      if (!wrapperRef.current?.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener("pointerdown", handlePointerDown);
    return () => document.removeEventListener("pointerdown", handlePointerDown);
  }, []);

  const filteredOptions = useMemo(() => {
    const normalizedQuery = normalize(query);
    const sortedOptions = [...options].sort(compareByTicker);
    if (!normalizedQuery) {
      return sortedOptions.slice(0, maxVisible);
    }
    return sortedOptions
      .filter((option) => {
        const target = normalize([
          option.ticker,
          option.name,
          option.market,
          option.currency,
          option.securityType,
        ].filter(Boolean).join(" "));
        return target.includes(normalizedQuery);
      })
      .slice(0, maxVisible);
  }, [maxVisible, options, query]);

  const chooseOption = (option: SecurityComboboxOption) => {
    onChange(String(option.id));
    setQuery(optionLabel(option));
    setIsOpen(false);
  };
  const activeOption = filteredOptions[Math.min(activeIndex, filteredOptions.length - 1)];

  return (
    <div ref={wrapperRef} className="relative">
      <input
        type="text"
        value={inputValue}
        disabled={disabled}
        placeholder={placeholder}
        autoComplete="off"
        role="combobox"
        aria-controls={listboxId}
        aria-expanded={isOpen}
        aria-autocomplete="list"
        aria-activedescendant={isOpen && activeOption ? `${listboxId}-${activeOption.id}` : undefined}
        onFocus={() => {
          setQuery("");
          setActiveIndex(0);
          setIsOpen(true);
        }}
        onChange={(event) => {
          setQuery(event.target.value);
          setActiveIndex(0);
          onChange("");
          setIsOpen(true);
        }}
        onKeyDown={(event) => {
          if (!isOpen && (event.key === "ArrowDown" || event.key === "Enter")) {
            setIsOpen(true);
            return;
          }
          if (event.key === "ArrowDown") {
            event.preventDefault();
            setActiveIndex((index) => Math.min(index + 1, filteredOptions.length - 1));
          }
          if (event.key === "ArrowUp") {
            event.preventDefault();
            setActiveIndex((index) => Math.max(index - 1, 0));
          }
          if (event.key === "Enter" && isOpen && activeOption) {
            event.preventDefault();
            chooseOption(activeOption);
          }
          if (event.key === "Escape") {
            setIsOpen(false);
          }
        }}
        className={inputClass}
      />
      {isOpen && !disabled && (
        <div
          id={listboxId}
          role="listbox"
          className="absolute z-30 mt-1 max-h-72 w-full overflow-auto rounded-lg border border-gray-200 bg-white shadow-lg dark:border-gray-700 dark:bg-gray-900"
        >
          {filteredOptions.length === 0 ? (
            <div className="px-3 py-2 text-sm text-gray-500 dark:text-gray-400">{emptyMessage}</div>
          ) : (
            filteredOptions.map((option, index) => (
              <button
                key={option.id}
                id={`${listboxId}-${option.id}`}
                type="button"
                role="option"
                aria-selected={index === activeIndex}
                onMouseDown={(event) => event.preventDefault()}
                onClick={() => chooseOption(option)}
                className={`block w-full px-3 py-2 text-left text-sm transition ${
                  index === activeIndex
                    ? "bg-emerald-50 text-emerald-900 dark:bg-emerald-950 dark:text-emerald-100"
                    : "text-gray-900 hover:bg-gray-50 dark:text-white dark:hover:bg-gray-800"
                }`}
              >
                <span className="block font-semibold">{option.ticker} - {option.name}</span>
                <span className="mt-0.5 block text-xs text-gray-500 dark:text-gray-400">
                  {[option.market, option.securityType, option.currency].filter(Boolean).join(" · ") || "-"}
                </span>
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
}

function optionLabel(option: SecurityComboboxOption) {
  return `${option.ticker} - ${option.name}`;
}

function normalize(value: string) {
  return value.replace(/\s+/g, "").toLowerCase();
}

function compareByTicker(left: SecurityComboboxOption, right: SecurityComboboxOption) {
  const leftNumber = Number(left.ticker);
  const rightNumber = Number(right.ticker);
  if (Number.isFinite(leftNumber) && Number.isFinite(rightNumber) && leftNumber !== rightNumber) {
    return leftNumber - rightNumber;
  }
  return left.ticker.localeCompare(right.ticker, "ko", { numeric: true });
}
