package com.example.coursemanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/** Không để lỗi hệ thống hiện thẳng ra màn hình: luôn trả về trang thông báo thân thiện. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Không tìm thấy", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleBadId(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.NOT_FOUND, "Không tìm thấy", "Mã (Id) không hợp lệ hoặc không tồn tại.");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleOther(Exception ex) {
        ex.printStackTrace();
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Có lỗi xảy ra",
                "Hệ thống gặp sự cố, vui lòng thử lại sau.");
    }

    private ModelAndView build(HttpStatus status, String title, String message) {
        ModelAndView mv = new ModelAndView("error");
        mv.setStatus(status);
        mv.addObject("title", title);
        mv.addObject("message", message);
        return mv;
    }
}
