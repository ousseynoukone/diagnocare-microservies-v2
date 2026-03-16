package com.homosapiens.diagnocareservice.core.security;

import com.homosapiens.diagnocareservice.model.entity.enums.GenderEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that automatically encrypts/decrypts GenderEnum fields.
 * Converts GenderEnum to String (name()), encrypts it, and stores as String in database.
 */
@Converter
@Component
public class EncryptedGenderEnumConverter implements AttributeConverter<GenderEnum, String>, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EncryptedGenderEnumConverter.applicationContext = applicationContext;
    }

    private DataEncryptionService getEncryptionService() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not initialized");
        }
        return applicationContext.getBean(DataEncryptionService.class);
    }

    @Override
    public String convertToDatabaseColumn(GenderEnum attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            // Convert enum to its name (string representation)
            String enumName = attribute.name();
            // Encrypt the enum name
            return getEncryptionService().encrypt(enumName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt GenderEnum field", e);
        }
    }

    @Override
    public GenderEnum convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            String decrypted;
            // Check if the data is already encrypted
            if (dbData.length() > 20 && dbData.matches("^[A-Za-z0-9+/=]+$")) {
                // Decrypt the enum name
                decrypted = getEncryptionService().decrypt(dbData);
            } else {
                // If not encrypted, use as-is (for migration)
                decrypted = dbData;
            }
            
            // Convert string back to enum
            return GenderEnum.valueOf(decrypted);
        } catch (Exception e) {
            // If decryption or parsing fails, return null (for backward compatibility)
            try {
                // Try to parse as-is in case it's not encrypted
                return GenderEnum.valueOf(dbData);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }
}
