package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.model.entity.AppSetting;
import com.homosapiens.diagnocareservice.repository.AppSettingRepository;
import com.homosapiens.diagnocareservice.service.AppSettingService;
import com.homosapiens.diagnocareservice.dto.AppSettingDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppSettingServiceImpl implements AppSettingService {

    private final AppSettingRepository appSettingRepository;

    @Override
    public String getValue(String key, String defaultValue) {
        return appSettingRepository.findBySettingKey(key)
                .map(AppSetting::getSettingValue)
                .filter(value -> value != null && !value.trim().isEmpty())
                .orElse(defaultValue);
    }

    @Override
    public String setValue(String key, String value) {
        AppSetting setting = appSettingRepository.findBySettingKey(key)
                .orElseGet(() -> {
                    AppSetting created = new AppSetting();
                    created.setSettingKey(key);
                    return created;
                });
        setting.setSettingValue(value);
        return appSettingRepository.save(setting).getSettingValue();
    }

    @Override
    public List<AppSettingDTO> getAllSettings() {
        return appSettingRepository.findAll().stream().map(setting -> {
            AppSettingDTO dto = new AppSettingDTO();
            dto.setKey(setting.getSettingKey());
            dto.setValue(setting.getSettingValue());
            return dto;
        }).toList();
    }
}
