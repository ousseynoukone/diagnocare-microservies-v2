package com.homosapiens.diagnocareservice.core.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that automatically encrypts/decrypts String fields.
 * This converter encrypts data when persisting to the database and decrypts when reading.
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String>, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EncryptedStringConverter.applicationContext = applicationContext;
    }

    private DataEncryptionService getEncryptionService() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not initialized");
        }
        return applicationContext.getBean(DataEncryptionService.class);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }
        try {
            return getEncryptionService().encrypt(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt field", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        try {
            // Check if the data is already encrypted (starts with base64 pattern)
            // If it looks like encrypted data, decrypt it; otherwise return as-is (for migration)
            if (dbData.length() > 20 && dbData.matches("^[A-Za-z0-9+/=]+$")) {
                return getEncryptionService().decrypt(dbData);
            }
            return dbData;
        } catch (Exception e) {
            // If decryption fails, return the original value (for backward compatibility during migration)
            return dbData;
        }
    }
}
