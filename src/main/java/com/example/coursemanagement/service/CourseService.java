package com.example.coursemanagement.service;

import com.example.coursemanagement.dto.CourseFilter;
import com.example.coursemanagement.dto.CourseForm;
import com.example.coursemanagement.entity.Course;
import com.example.coursemanagement.entity.CourseCategory;
import com.example.coursemanagement.entity.CourseImage;
import com.example.coursemanagement.exception.ResourceNotFoundException;
import com.example.coursemanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private static final Pattern URL_PATTERN = Pattern.compile("^https?://\\S+$", Pattern.CASE_INSENSITIVE);

    private final CourseRepository courseRepo;
    private final CourseCategoryRepository categoryRepo;
    private final CourseImageRepository imageRepo;

    @Transactional(readOnly = true)
    public List<Course> search(CourseFilter filter) {
        return courseRepo.findAll(CourseSpecs.build(filter), CourseSpecs.sort(filter.getSort()));
    }

    @Transactional(readOnly = true)
    public Course getById(Long id) {
        return courseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học có Id = " + id));
    }

    public CourseForm toForm(Course c) {
        CourseForm f = new CourseForm();
        f.setName(c.getName());
        f.setTuitionFee(c.getTuitionFee());
        f.setDuration(c.getDuration());
        f.setActive(c.isActive());
        f.setDescription(c.getDescription());
        f.setCategoryId(c.getCategory().getId());
        return f;
    }

    /** Kiểm tra các quy tắc nghiệp vụ: CourseCategoryId tồn tại, tên không trùng trong cùng loại. */
    @Transactional(readOnly = true)
    public void validateBusiness(CourseForm form, Long currentCourseId, BindingResult br) {
        if (form.getCategoryId() != null && !categoryRepo.existsById(form.getCategoryId())) {
            br.rejectValue("categoryId", "category.notfound", "Loại khóa học không tồn tại");
            return;
        }
        if (form.getName() != null && !form.getName().isBlank() && form.getCategoryId() != null) {
            String name = form.getName().trim();
            boolean duplicated = currentCourseId == null
                    ? courseRepo.existsByNameIgnoreCaseAndCategoryId(name, form.getCategoryId())
                    : courseRepo.existsByNameIgnoreCaseAndCategoryIdAndIdNot(name, form.getCategoryId(), currentCourseId);
            if (duplicated) {
                br.rejectValue("name", "name.duplicate", "Tên khóa học đã tồn tại trong loại khóa học này");
            }
        }
    }

    public Course create(CourseForm form) {
        Course c = new Course();
        apply(c, form);
        return courseRepo.save(c);
    }

    public Course update(Long id, CourseForm form) {
        Course c = getById(id);
        apply(c, form);
        return courseRepo.save(c);
    }

    private void apply(Course c, CourseForm form) {
        CourseCategory category = categoryRepo.findById(form.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại khóa học có Id = " + form.getCategoryId()));
        c.setName(form.getName().trim());
        c.setTuitionFee(form.getTuitionFee());
        c.setDuration(form.getDuration());
        c.setActive(form.isActive());
        c.setDescription(form.getDescription());
        c.setCategory(category);
    }

    // ---------------- Ảnh khóa học ----------------

    /** Thêm ảnh bằng URL. Trả về thông báo lỗi hoặc null nếu thành công. */
    public String addImage(Long courseId, String url, boolean thumbnail) {
        Course course = getById(courseId);
        if (url == null || url.isBlank()) return "Đường dẫn ảnh không được để trống";
        url = url.trim();
        if (url.length() > 1000 || !URL_PATTERN.matcher(url).matches()) {
            return "Đường dẫn ảnh không hợp lệ (phải bắt đầu bằng http:// hoặc https://)";
        }
        if (thumbnail) {
            imageRepo.clearThumbnail(courseId);
        }
        CourseImage img = new CourseImage();
        img.setImageUrl(url);
        img.setThumbnail(thumbnail);
        img.setCourse(course);
        imageRepo.save(img);
        return null;
    }

    /** Chọn / đổi ảnh đại diện: ảnh cũ tự động thành ảnh thường. */
    public void setThumbnail(Long courseId, Long imageId) {
        getById(courseId);
        CourseImage img = getImage(courseId, imageId);
        imageRepo.clearThumbnail(courseId);
        // clearAutomatically đã làm mới persistence context nên nạp lại ảnh
        CourseImage fresh = imageRepo.findById(img.getId()).orElseThrow();
        fresh.setThumbnail(true);
        imageRepo.save(fresh);
    }

    public void deleteImage(Long courseId, Long imageId) {
        getById(courseId);
        imageRepo.delete(getImage(courseId, imageId));
    }

    private CourseImage getImage(Long courseId, Long imageId) {
        CourseImage img = imageRepo.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ảnh có Id = " + imageId));
        if (!img.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Ảnh Id = " + imageId + " không thuộc khóa học này");
        }
        return img;
    }
}
