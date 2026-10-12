package com.example.coursemanagement.controller;

import com.example.coursemanagement.dto.CourseFilter;
import com.example.coursemanagement.dto.CourseForm;
import com.example.coursemanagement.entity.Course;
import com.example.coursemanagement.service.CourseCategoryService;
import com.example.coursemanagement.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CourseCategoryService categoryService;

    @GetMapping("/")
    public String home() {
        return "redirect:/courses";
    }

    // ---------- Danh sách + tìm kiếm / lọc / sắp xếp ----------
    @GetMapping("/courses")
    public String index(@ModelAttribute("filter") CourseFilter filter, BindingResult binding, Model model) {
        List<Course> courses = Collections.emptyList();
        String error = null;

        if (binding.hasErrors()) {
            error = "Dữ liệu tìm kiếm không hợp lệ (học phí phải là số).";
            filter.setMinFee(null);
            filter.setMaxFee(null);
        } else if (filter.getMinFee() != null && filter.getMaxFee() != null
                && filter.getMinFee().compareTo(filter.getMaxFee()) > 0) {
            error = "Học phí tối thiểu không được lớn hơn học phí tối đa.";
        } else {
            courses = courseService.search(filter);
        }

        model.addAttribute("courses", courses);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("error", error);
        return "course/index";
    }

    // ---------- Chi tiết ----------
    @GetMapping("/courses/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.getById(id));
        return "course/detail";
    }

    // ---------- Create ----------
    @GetMapping("/courses/create")
    public String createForm(Model model) {
        model.addAttribute("form", new CourseForm());
        prepareForm(model, "Thêm khóa học", "/courses/create");
        return "course/form";
    }

    @PostMapping("/courses/create")
    public String create(@Valid @ModelAttribute("form") CourseForm form, BindingResult br,
                         Model model, RedirectAttributes ra) {
        courseService.validateBusiness(form, null, br);
        if (br.hasErrors()) {
            prepareForm(model, "Thêm khóa học", "/courses/create");
            return "course/form";
        }
        Course c = courseService.create(form);
        ra.addFlashAttribute("success", "Thêm khóa học thành công.");
        return "redirect:/courses/" + c.getId();
    }

    // ---------- Edit ----------
    @GetMapping("/courses/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", courseService.toForm(courseService.getById(id)));
        prepareForm(model, "Sửa khóa học", "/courses/" + id + "/edit");
        return "course/form";
    }

    @PostMapping("/courses/{id}/edit")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("form") CourseForm form,
                       BindingResult br, Model model, RedirectAttributes ra) {
        courseService.getById(id); // ném 404 nếu không tồn tại
        courseService.validateBusiness(form, id, br);
        if (br.hasErrors()) {
            prepareForm(model, "Sửa khóa học", "/courses/" + id + "/edit");
            return "course/form";
        }
        courseService.update(id, form);
        ra.addFlashAttribute("success", "Cập nhật khóa học thành công.");
        return "redirect:/courses/" + id;
    }

    // ---------- Ảnh khóa học ----------
    @PostMapping("/courses/{id}/images")
    public String addImage(@PathVariable Long id, @RequestParam("imageUrl") String imageUrl,
                           @RequestParam(value = "thumbnail", defaultValue = "false") boolean thumbnail,
                           RedirectAttributes ra) {
        String error = courseService.addImage(id, imageUrl, thumbnail);
        if (error != null) ra.addFlashAttribute("error", error);
        else ra.addFlashAttribute("success", "Đã thêm ảnh.");
        return "redirect:/courses/" + id;
    }

    @PostMapping("/courses/{id}/images/{imageId}/thumbnail")
    public String setThumbnail(@PathVariable Long id, @PathVariable Long imageId, RedirectAttributes ra) {
        courseService.setThumbnail(id, imageId);
        ra.addFlashAttribute("success", "Đã đổi ảnh đại diện.");
        return "redirect:/courses/" + id;
    }

    @PostMapping("/courses/{id}/images/{imageId}/delete")
    public String deleteImage(@PathVariable Long id, @PathVariable Long imageId, RedirectAttributes ra) {
        courseService.deleteImage(id, imageId);
        ra.addFlashAttribute("success", "Đã xóa ảnh.");
        return "redirect:/courses/" + id;
    }

    private void prepareForm(Model model, String title, String action) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("title", title);
        model.addAttribute("action", action);
    }
}
