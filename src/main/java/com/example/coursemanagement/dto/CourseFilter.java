package com.example.coursemanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CourseFilter {
    private String keyword;
    private Long categoryId;
    /** "" = tất cả, "active" = Đang tuyển sinh, "inactive" = Ngừng tuyển sinh */
    private String status;
    private BigDecimal minFee;
    private BigDecimal maxFee;
    /** fee_asc | fee_desc | duration_asc */
    private String sort;
}
