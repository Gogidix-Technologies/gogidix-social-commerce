package com.exalt.ecosystem.socialcommerce.localization.service;

import org.springframework.stereotype.Service;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

/**
 * Localization Service
 * Handles internationalization and localization functionality
 */
@Service
public class LocalizationService {
    
    private Map<String, Map<String, String>> translations = new HashMap<>();
    
    /**
     * Get localized message
     */
    public String getMessage(String key, Locale locale) {
        String language = locale.getLanguage();
        Map<String, String> languageMap = translations.get(language);
        
        if (languageMap != null && languageMap.containsKey(key)) {
            return languageMap.get(key);
        }
        
        // Fallback to English
        Map<String, String> englishMap = translations.get("en");
        if (englishMap != null && englishMap.containsKey(key)) {
            return englishMap.get(key);
        }
        
        return key; // Return key if no translation found
    }
    
    /**
     * Get all supported locales
     */
    public String[] getSupportedLocales() {
        return new String[]{"en", "es", "fr", "de", "ar"};
    }
    
    /**
     * Check if locale is supported
     */
    public boolean isLocaleSupported(Locale locale) {
        String language = locale.getLanguage();
        for (String supported : getSupportedLocales()) {
            if (supported.equals(language)) {
                return true;
            }
        }
        return false;
    }
}