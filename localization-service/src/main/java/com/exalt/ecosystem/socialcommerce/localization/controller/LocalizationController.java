package com.exalt.ecosystem.socialcommerce.localization.controller;

import com.exalt.ecosystem.socialcommerce.localization.service.LocalizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;

/**
 * Localization Controller
 * REST API for localization services
 */
@RestController
@RequestMapping("/api/localization")
public class LocalizationController {
    
    @Autowired
    private LocalizationService localizationService;
    
    /**
     * Get localized message
     */
    @GetMapping("/message")
    public String getMessage(@RequestParam String key, 
                           @RequestParam(defaultValue = "en") String locale) {
        return localizationService.getMessage(key, new Locale(locale));
    }
    
    /**
     * Get supported locales
     */
    @GetMapping("/locales")
    public String[] getSupportedLocales() {
        return localizationService.getSupportedLocales();
    }
    
    /**
     * Check if locale is supported
     */
    @GetMapping("/locales/{locale}/supported")
    public boolean isLocaleSupported(@PathVariable String locale) {
        return localizationService.isLocaleSupported(new Locale(locale));
    }
}