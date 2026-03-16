package com.homosapiens.diagnocareservice.core.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * JPA AttributeConverter that automatically encrypts/decrypts Set<String> fields.
 * Converts Set<String> to JSON String, encrypts it, and stores as String in database.
 * Used for ElementCollection fields like familyAntecedents.
 */
@Converter
@Component
public class EncryptedSetStringConverter implements AttributeConverter<Set<String>, String>, ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EncryptedSetStringConverter.applicationContext = applicationContext;
    }

    private DataEncryptionService getEncryptionService() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not initialized");
        }
        return applicationContext.getBean(DataEncryptionService.class);
    }

    @Override
    public String convertToDatabaseColumn(Set<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            // Convert en String Json
            String json = objectMapper.writeValueAsString(attribute);
            // chiffrer le string
            return getEncryptionService().encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt Set<String> field", e);
        }
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashSet<>();
        }
        try {
            //Verifier si les donnés sont chiffrés
            if (dbData.length() > 20 && dbData.matches("^[A-Za-z0-9+/=]+$")) {
                // Déchiffrement
                String decrypted = getEncryptionService().decrypt(dbData);
                // Convertir en json
                return objectMapper.readValue(decrypted, new TypeReference<Set<String>>() {});
            }
            // si non chiffré la valeur est retournée sans modification
            try {
                return objectMapper.readValue(dbData, new TypeReference<Set<String>>() {});
            } catch (Exception e) {
                return new HashSet<>();
            }
        } catch (Exception e) {
            return new HashSet<>();
        }
    }
}
