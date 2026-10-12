package com.example.coursemanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Courses",
        uniqueConstraints = @UniqueConstraint(name = "UQ_Course_Name_Category", columnNames = {"name", "course_category_id"}))
@Getter
@Setter
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "tuition_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal tuitionFee;

    // Số giờ
    @Column(nullable = false)
    private Integer duration;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_category_id", nullable = false)
    private CourseCategory category;

    // Một Course có nhiều CourseImage
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CourseImage> images = new ArrayList<>();

    // ---- Các giá trị chỉ tính khi hiển thị (không có cột trong DB) ----

    /** Ngắn hạn: Duration <= 30, Dài hạn: Duration > 30 */
    public String getDurationType() {
        return duration != null && duration <= 30 ? "Ngắn hạn" : "Dài hạn";
    }

    public String getStatusText() {
        return active ? "Đang tuyển sinh" : "Ngừng tuyển sinh";
    }

    /** Ảnh đại diện; nếu chưa chọn thì null */
    public CourseImage getThumbnailImage() {
        return images.stream().filter(CourseImage::isThumbnail).findFirst().orElse(null);
    }
}
