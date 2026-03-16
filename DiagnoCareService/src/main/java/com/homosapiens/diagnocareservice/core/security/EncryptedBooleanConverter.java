package com.homosapiens.diagnocareservice.core.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that automatically encrypts/decrypts Boolean fields.
 * Converts Boolean to String, encrypts it, and stores as String in database.
 */
@Converter
@Component
public class EncryptedBooleanConverter implements AttributeConverter<Boolean, String>, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EncryptedBooleanConverter.applicationContext = applicationContext;
    }

    private DataEncryptionService getEncryptionService() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not initialized");
        }
        return applicationContext.getBean(DataEncryptionService.class);
    }

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return getEncryptionService().encrypt(attribute.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt Boolean field", e);
        }
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            // Verifier si c'est deja encrypté
            if (dbData.length() > 20 && dbData.matches("^[A-Za-z0-9+/=]+$")) {
                String decrypted = getEncryptionService().decrypt(dbData);
                return Boolean.parseBoolean(decrypted);
            }
            // Si c'est pas le cas parse directly (pour  migration)
            return Boolean.parseBoolean(dbData);
        } catch (Exception e) {

            try {
                return Boolean.parseBoolean(dbData);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
