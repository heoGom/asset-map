"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/lib/auth-provider";
import { useLanguage } from "@/lib/language-provider";
import { useTheme } from "@/lib/theme-provider";

export default function Header() {
  const pathname = usePathname();
  const { user, isAuthenticated, logout } = useAuth();
  const { language, setLanguage, t } = useLanguage();
  const { theme, setTheme } = useTheme();
  const primaryLinks = [
    { href: "/assets", label: t("nav.assets") },
    { href: "/dividends", label: t("nav.dividends") },
    { href: "/accounts", label: t("nav.accounts") },
  ];
  const securitiesActive = pathname === "/securities" || pathname.startsWith("/securities/");

  return (
    <nav className="sticky top-0 z-40 border-b border-gray-200/80 bg-white/95 backdrop-blur dark:border-gray-800 dark:bg-gray-950/95">
      <div className="mx-auto flex max-w-7xl flex-col gap-3 px-4 py-3 sm:px-6 lg:flex-row lg:items-center lg:justify-between">
        <Link href="/assets" className="flex min-w-0 items-center gap-3">
          <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-md bg-emerald-600 text-sm font-black text-white">
            AM
          </span>
          <span className="truncate text-lg font-bold text-gray-950 dark:text-white">Asset Map</span>
        </Link>

        <div className="flex flex-col gap-3 lg:flex-row lg:items-center">
          <div className="flex flex-wrap items-center gap-1 rounded-lg bg-gray-100 p-1 text-sm font-semibold text-gray-600 dark:bg-gray-900 dark:text-gray-300">
            {primaryLinks.map((link) => {
              const active = pathname === link.href || pathname.startsWith(`${link.href}/`);
              return (
                <Link
                  key={link.href}
                  href={link.href}
                  className={`rounded-md px-3 py-2 transition ${
                    active
                      ? "bg-white text-gray-950 shadow-sm dark:bg-gray-800 dark:text-white"
                      : "hover:bg-white/70 hover:text-gray-950 dark:hover:bg-gray-800/70 dark:hover:text-white"
                  }`}
                >
                  {link.label}
                </Link>
              );
            })}
            <div className="group relative">
              <Link
                href="/securities"
                className={`block rounded-md px-3 py-2 transition ${
                  securitiesActive
                    ? "bg-white text-gray-950 shadow-sm dark:bg-gray-800 dark:text-white"
                    : "hover:bg-white/70 hover:text-gray-950 dark:hover:bg-gray-800/70 dark:hover:text-white"
                }`}
              >
                {t("nav.management")}
              </Link>
              <div className="invisible absolute right-0 top-full z-50 w-44 pt-2 opacity-0 transition group-hover:visible group-hover:opacity-100 group-focus-within:visible group-focus-within:opacity-100">
                <div className="rounded-lg border border-gray-200 bg-white p-1 shadow-lg dark:border-gray-700 dark:bg-gray-900">
                  <Link
                    href="/securities"
                    className="block rounded-md px-3 py-2 text-sm text-gray-600 hover:bg-gray-50 hover:text-gray-950 dark:text-gray-300 dark:hover:bg-gray-800 dark:hover:text-white"
                  >
                    {t("nav.securities")}
                  </Link>
                </div>
              </div>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-2 lg:justify-end">
            <div className="flex rounded-lg border border-gray-200 bg-white p-1 text-xs font-bold text-gray-500 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-400">
              <button
                type="button"
                onClick={() => setTheme("light")}
                className={`rounded-md px-2.5 py-1.5 transition ${theme === "light" ? "bg-gray-900 text-white dark:bg-gray-100 dark:text-gray-950" : "hover:bg-gray-100 dark:hover:bg-gray-800"}`}
                aria-label={t("nav.switchToLight")}
              >
                Light
              </button>
              <button
                type="button"
                onClick={() => setTheme("dark")}
                className={`rounded-md px-2.5 py-1.5 transition ${theme === "dark" ? "bg-gray-900 text-white dark:bg-gray-100 dark:text-gray-950" : "hover:bg-gray-100 dark:hover:bg-gray-800"}`}
                aria-label={t("nav.switchToDark")}
              >
                Dark
              </button>
            </div>

            <div className="flex rounded-lg border border-gray-200 bg-white p-1 text-xs font-bold text-gray-500 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-400">
              <button
                type="button"
                onClick={() => setLanguage("ko")}
                className={`rounded-md px-2.5 py-1.5 transition ${language === "ko" ? "bg-emerald-600 text-white" : "hover:bg-gray-100 dark:hover:bg-gray-800"}`}
                aria-label={t("nav.switchToKorean")}
              >
                KO
              </button>
              <button
                type="button"
                onClick={() => setLanguage("en")}
                className={`rounded-md px-2.5 py-1.5 transition ${language === "en" ? "bg-emerald-600 text-white" : "hover:bg-gray-100 dark:hover:bg-gray-800"}`}
                aria-label={t("nav.switchToEnglish")}
              >
                EN
              </button>
            </div>

            {isAuthenticated ? (
              <div className="flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-2 py-1.5 dark:border-gray-700 dark:bg-gray-900">
                <span className="max-w-28 truncate text-sm font-semibold text-gray-700 dark:text-gray-200">{user?.nickname}</span>
                <button
                  type="button"
                  onClick={logout}
                  className="rounded-md px-2 py-1 text-sm font-semibold text-gray-500 hover:bg-gray-100 hover:text-gray-950 dark:text-gray-300 dark:hover:bg-gray-800 dark:hover:text-white"
                >
                  {t("nav.logout")}
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link href="/login" className="rounded-md px-3 py-2 text-sm font-semibold text-gray-600 hover:bg-gray-100 hover:text-gray-950 dark:text-gray-200 dark:hover:bg-gray-800">{t("nav.login")}</Link>
                <Link href="/signup" className="rounded-md bg-emerald-600 px-3 py-2 text-sm font-bold text-white hover:bg-emerald-700">{t("nav.signup")}</Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
