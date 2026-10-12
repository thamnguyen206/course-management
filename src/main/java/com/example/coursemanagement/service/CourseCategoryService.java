package com.example.coursemanagement.service;

import com.example.coursemanagement.entity.CourseCategory;
import com.example.coursemanagement.exception.ResourceNotFoundException;
import com.example.coursemanagement.repository.CourseCategoryRepository;
import com.example.coursemanagement.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseCategoryService {

    private final CourseCategoryRepository categoryRepo;
    private final CourseRepository courseRepo;

    @Transactional(readOnly = true)
    public List<CourseCategory> findAll() {
        return categoryRepo.findAll();
    }

    @Transactional(readOnly = true)
    public CourseCategory getById(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại khóa học có Id = " + id));
    }

    @Transactional(readOnly = true)
    public boolean isNameDuplicated(String name, Long currentId) {
        if (name == null || name.isBlank()) return false;
        return currentId == null
                ? categoryRepo.existsByNameIgnoreCase(name.trim())
                : categoryRepo.existsByNameIgnoreCaseAndIdNot(name.trim(), currentId);
    }

    public CourseCategory save(CourseCategory c) {
        c.setName(c.getName().trim());
        return categoryRepo.save(c);
    }

    /** Ràng buộc 1: không xóa loại khóa học nếu còn khóa học. Trả về thông báo lỗi hoặc null nếu xóa được. */
    public String delete(Long id) {
        CourseCategory c = getById(id);
        if (courseRepo.existsByCategoryId(id)) {
            return "Không thể xóa loại khóa học \"" + c.getName() + "\" vì vẫn còn khóa học thuộc loại này.";
        }
        categoryRepo.delete(c);
        return null;
    }
}
