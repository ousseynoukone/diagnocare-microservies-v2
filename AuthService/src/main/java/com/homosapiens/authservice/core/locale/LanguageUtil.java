package com.homosapiens.authservice.core.locale;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

public class LanguageUtil {
    private static final Map<String, String> FR_MESSAGES = new HashMap<>();

    static {
        FR_MESSAGES.put("Validation failed", "Validation échouée");
        FR_MESSAGES.put("User not found", "Utilisateur introuvable");
        FR_MESSAGES.put("Email already in use", "Email déjà utilisé");
        FR_MESSAGES.put("Email or password incorrect", "Email ou mot de passe incorrect");
        FR_MESSAGES.put("Email not verified", "Email non vérifié");
        FR_MESSAGES.put("Role not found", "Rôle introuvable");
        FR_MESSAGES.put("Refresh token is required", "Le jeton de rafraîchissement est requis");
        FR_MESSAGES.put("Invalid Authorization header format", "Format d'en-tête Authorization invalide");
        FR_MESSAGES.put("Missing Authorization header", "En-tête Authorization manquant");
        FR_MESSAGES.put("OTP sent", "OTP envoyé");
        FR_MESSAGES.put("OTP validated", "OTP validé");
        FR_MESSAGES.put("OTP not found", "OTP introuvable");
        FR_MESSAGES.put("OTP expired", "OTP expiré");
        FR_MESSAGES.put("OTP invalid", "OTP invalide");
    }

    public static String resolveLang(HttpServletRequest request) {
        if (request == null) {
            return "fr";
        }
        String headerLang = request.getHeader("x-auth-user-lang");
        if (headerLang != null && headerLang.toLowerCase().startsWith("en")) {
            return "en";
        }
        String acceptLang = request.getHeader("Accept-Language");
        if (acceptLang != null && acceptLang.toLowerCase().startsWith("en")) {
            return "en";
        }
        return "fr";
    }

    public static String translateMessage(String message, String lang) {
        if (message == null || lang == null || lang.equals("fr") == false && lang.equals("en") == false) {
            return message;
        }
        if ("en".equals(lang)) {
            return message;
        }
        return FR_MESSAGES.getOrDefault(message, message);
    }
}
