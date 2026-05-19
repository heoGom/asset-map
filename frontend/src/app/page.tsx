"use client";

import Link from "next/link";
import { useLanguage } from "@/lib/language-provider";

export default function Home() {
  const { t } = useLanguage();

  const workspaces = [
    {
      href: "/assets",
      title: t("home.assetsTitle"),
      description: t("home.assetsDescription"),
      meta: t("assets.holdingDetail"),
    },
    {
      href: "/dividends",
      title: t("home.dividendsTitle"),
      description: t("home.dividendsDescription"),
      meta: t("dividends.monthlyStatus"),
    },
    {
      href: "/accounts",
      title: t("home.accountsTitle"),
      description: t("home.accountsDescription"),
      meta: t("accounts.list"),
    },
  ];

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950">
      <section className="border-b border-gray-200 bg-white dark:border-gray-800 dark:bg-gray-950">
        <div className="mx-auto grid max-w-7xl gap-8 px-4 py-10 sm:px-6 lg:grid-cols-[1.3fr_0.7fr] lg:items-end lg:py-14">
          <div>
            <p className="text-sm font-bold uppercase text-emerald-700 dark:text-emerald-400">
              Asset Map
            </p>
            <h1 className="mt-3 max-w-3xl text-4xl font-black text-gray-950 dark:text-white sm:text-5xl">
              {t("home.title")}
            </h1>
            <p className="mt-4 max-w-2xl text-base leading-7 text-gray-600 dark:text-gray-300">
              {t("home.subtitle")}
            </p>
          </div>
          <div className="flex flex-wrap gap-3 lg:justify-end">
            <Link
              href="/assets"
              className="rounded-md bg-gray-950 px-4 py-2.5 text-sm font-bold text-white transition hover:bg-gray-800 dark:bg-white dark:text-gray-950 dark:hover:bg-gray-200"
            >
              {t("home.primaryAction")}
            </Link>
            <Link
              href="/dividends"
              className="rounded-md border border-gray-200 bg-white px-4 py-2.5 text-sm font-bold text-gray-700 transition hover:bg-gray-50 hover:text-gray-950 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-200 dark:hover:bg-gray-800 dark:hover:text-white"
            >
              {t("home.secondaryAction")}
            </Link>
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-8 sm:px-6">
        <div className="grid gap-4 md:grid-cols-3">
          {workspaces.map((workspace) => (
            <Link
              key={workspace.href}
              href={workspace.href}
              className="group rounded-lg border border-gray-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-emerald-200 hover:shadow-md dark:border-gray-800 dark:bg-gray-900 dark:hover:border-emerald-700"
            >
              <div className="flex items-center justify-between gap-3">
                <h2 className="text-lg font-bold text-gray-950 dark:text-white">{workspace.title}</h2>
                <span className="rounded-md bg-gray-100 px-2 py-1 text-xs font-bold text-gray-500 dark:bg-gray-800 dark:text-gray-300">
                  {workspace.meta}
                </span>
              </div>
              <p className="mt-4 min-h-14 text-sm leading-6 text-gray-600 dark:text-gray-300">
                {workspace.description}
              </p>
              <span className="mt-5 inline-flex text-sm font-bold text-emerald-700 group-hover:text-emerald-800 dark:text-emerald-400">
                {t("common.details")}
              </span>
            </Link>
          ))}
        </div>

        <div className="mt-5 rounded-lg border border-dashed border-gray-300 bg-white p-5 dark:border-gray-700 dark:bg-gray-900">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="text-base font-bold text-gray-950 dark:text-white">{t("home.operationsTitle")}</h2>
              <p className="mt-1 text-sm leading-6 text-gray-600 dark:text-gray-300">
                {t("home.operationsDescription")}
              </p>
            </div>
            <Link
              href="/securities"
              className="shrink-0 rounded-md border border-gray-200 px-3 py-2 text-sm font-bold text-gray-600 transition hover:bg-gray-50 hover:text-gray-950 dark:border-gray-700 dark:text-gray-200 dark:hover:bg-gray-800 dark:hover:text-white"
            >
              {t("home.openSecurities")}
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
