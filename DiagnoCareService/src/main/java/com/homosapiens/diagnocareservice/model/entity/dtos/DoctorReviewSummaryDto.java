package com.homosapiens.diagnocareservice.model.entity.dtos;

import lombok.Data;
import java.util.List;

@Data
public class DoctorReviewSummaryDto {
    private Long doctorId;
    private String doctorName;
    private Double averageRating;
    private Long totalReviews;
    private Long fiveStarReviews;
    private Long fourStarReviews;
    private Long threeStarReviews;
    private Long twoStarReviews;
    private Long oneStarReviews;
    private List<ReviewResponseDto> recentReviews;
    
    public static DoctorReviewSummaryDto create(Long doctorId, String doctorName, Double averageRating, 
                                               Long totalReviews, List<ReviewResponseDto> recentReviews) {
        DoctorReviewSummaryDto dto = new DoctorReviewSummaryDto();
        dto.setDoctorId(doctorId);
        dto.setDoctorName(doctorName);
        dto.setAverageRating(averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);
        dto.setTotalReviews(totalReviews != null ? totalReviews : 0L);
        dto.setRecentReviews(recentReviews);
        return dto;
    }
} 