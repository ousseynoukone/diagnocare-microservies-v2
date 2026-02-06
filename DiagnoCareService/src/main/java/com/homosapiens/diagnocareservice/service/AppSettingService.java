package com.homosapiens.diagnocareservice.service;

public interface AppSettingService {
    String getValue(String key, String defaultValue);
    String setValue(String key, String value);
}
