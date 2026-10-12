package com.example.coursemanagement.repository;

import com.example.coursemanagement.dto.CourseFilter;
import com.example.coursemanagement.entity.Course;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Mọi điều kiện lọc được dịch thành câu SQL (WHERE / ORDER BY), không lọc bằng vòng lặp trong Java. */
public final class CourseSpecs {

    private CourseSpecs() {}

    public static Specification<Course> build(CourseFilter f) {
        return (root, query, cb) -> {
            // Nạp luôn category để tránh N+1 (chỉ khi không phải câu count)
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("category", JoinType.LEFT);
            }

            List<Predicate> ps = new ArrayList<>();

            if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
                ps.add(cb.like(cb.lower(root.get("name")), "%" + f.getKeyword().trim().toLowerCase() + "%"));
            }
            if (f.getCategoryId() != null) {
                ps.add(cb.equal(root.get("category").get("id"), f.getCategoryId()));
            }
            if ("active".equals(f.getStatus())) {
                ps.add(cb.isTrue(root.get("active")));
            } else if ("inactive".equals(f.getStatus())) {
                ps.add(cb.isFalse(root.get("active")));
            }
            if (f.getMinFee() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("tuitionFee"), f.getMinFee()));
            }
            if (f.getMaxFee() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("tuitionFee"), f.getMaxFee()));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    public static Sort sort(String sort) {
        if (sort == null) return Sort.by("id").ascending();
        return switch (sort) {
            case "fee_asc" -> Sort.by("tuitionFee").ascending();
            case "fee_desc" -> Sort.by("tuitionFee").descending();
            case "duration_asc" -> Sort.by("duration").ascending();
            default -> Sort.by("id").ascending();
        };
    }
}
