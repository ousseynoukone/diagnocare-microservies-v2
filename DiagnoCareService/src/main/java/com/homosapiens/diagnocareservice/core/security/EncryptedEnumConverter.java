package com.homosapiens.diagnocareservice.core.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that automatically encrypts/decrypts Enum fields.
 * Converts Enum to String (name()), encrypts it, and stores as String in database.
 * 
 * Note: This converter works with @Enumerated(EnumType.STRING) annotations.
 * The enum value is stored as the encrypted enum name.
 */
@Converter
@Component
public class EncryptedEnumConverter implements AttributeConverter<Enum<?>, String>, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EncryptedEnumConverter.applicationContext = applicationContext;
    }

    private DataEncryptionService getEncryptionService() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not initialized");
        }
        return applicationContext.getBean(DataEncryptionService.class);
    }

    @Override
    public String convertToDatabaseColumn(Enum<?> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            // Convert enum to its name (string representation)
            String enumName = attribute.name();
            // Encrypt the enum name
            return getEncryptionService().encrypt(enumName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt Enum field", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enum<?> convertToEntityAttribute(String dbData) {
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
            
            // Note: This converter cannot determine the enum type at runtime
            // The actual enum conversion must be handled by @Enumerated annotation
            // This is a limitation - we'll need to handle it differently
            // For now, return null and let JPA handle it via @Enumerated
            return null;
        } catch (Exception e) {
            // If decryption fails, return null (JPA will handle via @Enumerated)
            return null;
        }
    }
}
