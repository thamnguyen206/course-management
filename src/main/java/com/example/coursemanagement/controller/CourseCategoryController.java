package com.example.coursemanagement.controller;

import com.example.coursemanagement.entity.CourseCategory;
import com.example.coursemanagement.service.CourseCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CourseCategoryController {

    private final CourseCategoryService service;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("categories", service.findAll());
        return "category/index";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new CourseCategory());
        model.addAttribute("title", "Thêm loại khóa học");
        model.addAttribute("action", "/categories/create");
        return "category/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("category") CourseCategory category, BindingResult br,
                         Model model, RedirectAttributes ra) {
        return save(category, null, "/categories/create", "Thêm loại khóa học", model, ra);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", service.getById(id));
        model.addAttribute("title", "Sửa loại khóa học");
        model.addAttribute("action", "/categories/" + id + "/edit");
        return "category/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @ModelAttribute("category") CourseCategory category,
                       Model model, RedirectAttributes ra) {
        service.getById(id);
        category.setId(id);
        return save(category, id, "/categories/" + id + "/edit", "Sửa loại khóa học", model, ra);
    }

    private String save(CourseCategory category, Long id, String action, String title,
                        Model model, RedirectAttributes ra) {
        String error = null;
        if (category.getName() == null || category.getName().isBlank()) {
            error = "Tên loại khóa học không được để trống";
        } else if (category.getName().trim().length() > 200) {
            error = "Tên loại khóa học tối đa 200 ký tự";
        } else if (service.isNameDuplicated(category.getName(), id)) {
            error = "Tên loại khóa học đã tồn tại";
        }
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("title", title);
            model.addAttribute("action", action);
            return "category/form";
        }
        service.save(category);
        ra.addFlashAttribute("success", "Lưu loại khóa học thành công.");
        return "redirect:/categories";
    }

    /** Ràng buộc 1: không xóa nếu còn khóa học thuộc loại này */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        String error = service.delete(id);
        if (error != null) ra.addFlashAttribute("error", error);
        else ra.addFlashAttribute("success", "Đã xóa loại khóa học.");
        return "redirect:/categories";
    }
}
