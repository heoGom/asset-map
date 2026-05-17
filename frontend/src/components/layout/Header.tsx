"use client";

import Link from "next/link";
import { useAuth } from "@/lib/auth-provider";
import { useLanguage } from "@/lib/language-provider";
import { useTheme } from "@/lib/theme-provider";

export default function Header() {
  const { user, isAuthenticated, logout } = useAuth();
  const { language, toggleLanguage, t } = useLanguage();
  const { theme, toggleTheme } = useTheme();

  return (
    <nav className="border-b border-gray-200 bg-white dark:border-gray-700 dark:bg-gray-900">
      <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-4 px-6 py-4">
        <Link href="/" className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-lg bg-emerald-600"></div>
          <span className="text-xl font-bold tracking-tight text-gray-900 dark:text-white">Asset Map</span>
        </Link>
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex gap-6 text-sm font-medium text-gray-600 dark:text-gray-300">
            <Link href="/assets" className="hover:text-emerald-600">{t("nav.assets")}</Link>
            <Link href="/dividends" className="hover:text-emerald-600">{t("nav.dividends")}</Link>
            <Link href="/accounts" className="hover:text-emerald-600">{t("nav.accounts")}</Link>
          </div>
          <div className="flex items-center gap-2 border-l border-gray-200 pl-4 dark:border-gray-700">
            <button
              type="button"
              onClick={toggleTheme}
              className="rounded-lg border border-gray-200 px-3 py-1 text-sm font-semibold text-gray-600 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-800"
              aria-label={theme === "dark" ? t("nav.switchToLight") : t("nav.switchToDark")}
              title={theme === "dark" ? t("nav.switchToLight") : t("nav.switchToDark")}
            >
              {theme === "dark" ? "Light" : "Dark"}
            </button>
            <button
              type="button"
              onClick={toggleLanguage}
              className="rounded-lg border border-gray-200 px-3 py-1 text-sm font-semibold text-gray-600 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-800"
              aria-label={language === "ko" ? t("nav.switchToEnglish") : t("nav.switchToKorean")}
              title={language === "ko" ? t("nav.switchToEnglish") : t("nav.switchToKorean")}
            >
              {language === "ko" ? "EN" : "KO"}
            </button>
            {isAuthenticated ? (
              <>
                <span className="text-sm font-medium text-gray-700 dark:text-gray-200">{user?.nickname}</span>
                <button
                  type="button"
                  onClick={logout}
                  className="rounded-lg border border-gray-200 px-3 py-1 text-sm font-medium text-gray-600 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-800"
                >
                  {t("nav.logout")}
                </button>
              </>
            ) : (
              <>
                <Link href="/login" className="text-sm font-medium text-gray-600 hover:text-emerald-600 dark:text-gray-200">{t("nav.login")}</Link>
                <Link href="/signup" className="rounded-lg bg-emerald-600 px-3 py-1 text-sm font-bold text-white hover:bg-emerald-700">{t("nav.signup")}</Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
