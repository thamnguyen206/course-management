# Quản lý khóa học (Spring Boot + Thymeleaf + JPA)

## Chạy
1. Cài JDK 17+ và Maven.
2. Tạo database rỗng `CourseDB` (SQL Server) - hoặc sửa `application.properties` sang PostgreSQL.
3. Sửa username/password trong `src/main/resources/application.properties`.
4. `mvn spring-boot:run` rồi mở http://localhost:8080

Hibernate tự tạo 3 bảng: `Courses`, `CourseCategories`, `CourseImages`.
`DataSeeder` tự thêm 3 loại khóa học và 6 khóa học mẫu khi DB còn trống.
