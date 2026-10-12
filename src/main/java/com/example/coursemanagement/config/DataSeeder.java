package com.example.coursemanagement.config;

import com.example.coursemanagement.entity.Course;
import com.example.coursemanagement.entity.CourseCategory;
import com.example.coursemanagement.entity.CourseImage;
import com.example.coursemanagement.repository.CourseCategoryRepository;
import com.example.coursemanagement.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Dữ liệu mẫu: 3 loại khóa học, 6 khóa học (1 khóa ngừng tuyển sinh), nhiều ảnh. Chỉ chạy khi DB còn trống. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CourseCategoryRepository categoryRepo;
    private final CourseRepository courseRepo;

    @Override
    public void run(String... args) {
        if (categoryRepo.count() > 0) return;

        CourseCategory web = category("Lập trình Web", "Các khóa học phát triển ứng dụng web");
        CourseCategory mobile = category("Lập trình Di động", "Các khóa học phát triển ứng dụng di động");
        CourseCategory db = category("Cơ sở dữ liệu", "Các khóa học về thiết kế và quản trị CSDL");

        course("ASP.NET Core", "3500000", 45, true, "Xây dựng web với ASP.NET Core MVC", web, "aspnet", "aspnet2");
        course("ReactJS", "2800000", 30, true, "Thư viện UI phổ biến", web, "react");
        course("Flutter", "3200000", 40, true, "Lập trình đa nền tảng với Flutter", mobile, "flutter", "flutter2");
        course("Android Kotlin", "2500000", 25, false, "Lập trình Android với Kotlin", mobile, "kotlin");
        course("SQL Server cơ bản", "1500000", 20, true, "Nhập môn SQL Server", db, "sqlserver");
        course("PostgreSQL nâng cao", "2000000", 36, true, "Tối ưu truy vấn và quản trị PostgreSQL", db);
    }

    private CourseCategory category(String name, String desc) {
        CourseCategory c = new CourseCategory();
        c.setName(name);
        c.setDescription(desc);
        return categoryRepo.save(c);
    }

    private void course(String name, String fee, int duration, boolean active, String desc,
                        CourseCategory category, String... imageSeeds) {
        Course c = new Course();
        c.setName(name);
        c.setTuitionFee(new BigDecimal(fee));
        c.setDuration(duration);
        c.setActive(active);
        c.setDescription(desc);
        c.setCategory(category);
        for (int i = 0; i < imageSeeds.length; i++) {
            CourseImage img = new CourseImage();
            img.setImageUrl("https://picsum.photos/seed/" + imageSeeds[i] + "/600/360");
            img.setThumbnail(i == 0); // ảnh đầu tiên là ảnh đại diện
            img.setCourse(c);
            c.getImages().add(img);
        }
        courseRepo.save(c);
    }
}
