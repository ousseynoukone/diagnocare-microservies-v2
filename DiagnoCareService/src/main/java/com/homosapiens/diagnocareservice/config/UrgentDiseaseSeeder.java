package com.homosapiens.diagnocareservice.config;

import com.homosapiens.diagnocareservice.model.entity.UrgentDisease;
import com.homosapiens.diagnocareservice.repository.UrgentDiseaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UrgentDiseaseSeeder implements CommandLineRunner {

    private final UrgentDiseaseRepository urgentDiseaseRepository;

    @Override
    public void run(String... args) {
        List<String> seedDiseases = List.of(
                "Heart attack",
                "Paralysis (brain hemorrhage)",
                "Pneumonia",
                "Tuberculosis"
        );

        for (String diseaseName : seedDiseases) {
            if (!urgentDiseaseRepository.existsByDiseaseNameIgnoreCase(diseaseName)) {
                UrgentDisease urgentDisease = new UrgentDisease();
                urgentDisease.setDiseaseName(diseaseName);
                urgentDiseaseRepository.save(urgentDisease);
            }
        }
    }
}
