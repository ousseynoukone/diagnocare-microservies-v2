-- Migration script to remove gender check constraint
-- This is needed because gender field is now encrypted and stores Base64 strings, not 'MALE'/'FEMALE'

-- Remove the check constraint on gender column
ALTER TABLE patient_medical_profiles 
DROP CONSTRAINT IF EXISTS patient_medical_profiles_gender_check;

-- Note: The gender column will now accept any VARCHAR(255) value (encrypted Base64 strings)
-- Validation is handled at application level via EncryptedGenderEnumConverter
