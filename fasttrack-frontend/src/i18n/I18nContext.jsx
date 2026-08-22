import React, { createContext, useContext, useState, useMemo } from 'react';
import { ToggleButtonGroup, ToggleButton } from '@mui/material';
import translations from './translations.js';

const I18nContext = createContext(null);

const LANGUAGES = [
  { code: 'en', label: 'English' },
  { code: 'gu', label: 'ગુજરાતી' },
  { code: 'hi', label: 'हिंदी' },
];

export function I18nProvider({ children }) {
  const [language, setLanguage] = useState(() => localStorage.getItem('ft_language') || 'en');

  const setLang = (code) => {
    setLanguage(code);
    localStorage.setItem('ft_language', code);
  };

  const t = useMemo(() => {
    const dict = translations[language] || translations.en;
    return (key) => dict[key] || translations.en[key] || key;
  }, [language]);

  return (
    <I18nContext.Provider value={{ language, setLanguage: setLang, t }}>{children}</I18nContext.Provider>
  );
}

export function useTranslation() {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error('useTranslation must be used within I18nProvider');
  return ctx;
}

export function LanguageSwitcher() {
  const { language, setLanguage } = useTranslation();
  return (
    <ToggleButtonGroup
      value={language}
      exclusive
      size="small"
      onChange={(event, newLang) => newLang && setLanguage(newLang)}
      sx={{ bgcolor: 'background.paper' }}
    >
      {LANGUAGES.map((lang) => (
        <ToggleButton key={lang.code} value={lang.code} sx={{ px: 1.5, py: 0.5, fontSize: '0.8rem' }}>
          {lang.label}
        </ToggleButton>
      ))}
    </ToggleButtonGroup>
  );
}
