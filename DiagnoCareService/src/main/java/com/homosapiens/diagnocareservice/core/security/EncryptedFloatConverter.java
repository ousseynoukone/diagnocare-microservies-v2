package com.homosapiens.diagnocareservice.core.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that automatically encrypts/decrypts Float fields.
 * Converts Float to String, encrypts it, and stores as String in database.
 */
@Converter
@Component
public class EncryptedFloatConverter implements AttributeConverter<Float, String>, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EncryptedFloatConverter.applicationContext = applicationContext;
    }

    private DataEncryptionService getEncryptionService() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not initialized");
        }
        return applicationContext.getBean(DataEncryptionService.class);
    }

    @Override
    public String convertToDatabaseColumn(Float attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return getEncryptionService().encrypt(attribute.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt Float field", e);
        }
    }

    @Override
    public Float convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            // Check if the data is already encrypted
            if (dbData.length() > 20 && dbData.matches("^[A-Za-z0-9+/=]+$")) {
                String decrypted = getEncryptionService().decrypt(dbData);
                return Float.parseFloat(decrypted);
            }
            // If not encrypted, parse directly (for migration)
            return Float.parseFloat(dbData);
        } catch (Exception e) {
            // If decryption fails, try to parse as-is (for backward compatibility)
            try {
                return Float.parseFloat(dbData);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }
}
