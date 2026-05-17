"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { dictionary, Language, TranslationKey } from "@/lib/i18n";

interface LanguageContextType {
  language: Language;
  setLanguage: (language: Language) => void;
  toggleLanguage: () => void;
  t: (key: TranslationKey) => string;
}

const LANGUAGE_STORAGE_KEY = "asset_map_language";

const LanguageContext = createContext<LanguageContextType>({
  language: "ko",
  setLanguage: () => {},
  toggleLanguage: () => {},
  t: (key) => key,
});

function resolveInitialLanguage(): Language {
  if (typeof window === "undefined") {
    return "ko";
  }

  const savedLanguage = window.localStorage.getItem(LANGUAGE_STORAGE_KEY);
  return savedLanguage === "en" ? "en" : "ko";
}

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  const [language, setLanguageState] = useState<Language>("ko");

  useEffect(() => {
    const initialLanguage = resolveInitialLanguage();
    setLanguageState(initialLanguage);
    document.documentElement.lang = initialLanguage;
  }, []);

  const setLanguage = (nextLanguage: Language) => {
    setLanguageState(nextLanguage);
    window.localStorage.setItem(LANGUAGE_STORAGE_KEY, nextLanguage);
    document.documentElement.lang = nextLanguage;
  };

  const toggleLanguage = () => {
    setLanguage(language === "ko" ? "en" : "ko");
  };

  const t = (key: TranslationKey) => {
    const [section, item] = key.split(".") as [keyof typeof dictionary.ko, string];
    const sectionDictionary = dictionary[language][section] as Record<string, string>;
    return sectionDictionary[item] ?? key;
  };

  return (
    <LanguageContext.Provider value={{ language, setLanguage, toggleLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  );
}

export const useLanguage = () => useContext(LanguageContext);
