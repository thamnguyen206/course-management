package com.example.coursemanagement.repository;

import com.example.coursemanagement.entity.CourseImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseImageRepository extends JpaRepository<CourseImage, Long> {

    /** Bỏ ảnh đại diện cũ của khóa học (đảm bảo tối đa 1 ảnh đại diện) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CourseImage i set i.thumbnail = false where i.course.id = :courseId and i.thumbnail = true")
    void clearThumbnail(@Param("courseId") Long courseId);
}
