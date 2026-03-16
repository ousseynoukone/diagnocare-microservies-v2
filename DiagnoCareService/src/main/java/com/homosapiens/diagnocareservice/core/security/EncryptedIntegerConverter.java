package com.homosapiens.diagnocareservice.core.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that automatically encrypts/decrypts Integer fields.
 * Converts Integer to String, encrypts it, and stores as String in database.
 * Also handles primitive int types.
 */
@Converter
@Component
public class EncryptedIntegerConverter implements AttributeConverter<Integer, String>, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EncryptedIntegerConverter.applicationContext = applicationContext;
    }

    private DataEncryptionService getEncryptionService() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not initialized");
        }
        return applicationContext.getBean(DataEncryptionService.class);
    }

    @Override
    public String convertToDatabaseColumn(Integer attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return getEncryptionService().encrypt(attribute.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt Integer field", e);
        }
    }

    @Override
    public Integer convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            // Check if the data is already encrypted
            if (dbData.length() > 20 && dbData.matches("^[A-Za-z0-9+/=]+$")) {
                String decrypted = getEncryptionService().decrypt(dbData);
                return Integer.parseInt(decrypted);
            }
            // If not encrypted, parse directly (for migration)
            return Integer.parseInt(dbData);
        } catch (Exception e) {
            // If decryption fails, try to parse as-is (for backward compatibility)
            try {
                return Integer.parseInt(dbData);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }
}
