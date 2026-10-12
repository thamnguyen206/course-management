SELECT @@SERVERNAME

USE master;
GO
IF DB_ID(N'CourseDB') IS NULL
    CREATE DATABASE CourseDB;
GO
USE CourseDB;
GO

/* Xóa bảng cũ (nếu có) theo thứ tự con -> cha */
IF OBJECT_ID(N'dbo.CourseImages', N'U')     IS NOT NULL DROP TABLE dbo.CourseImages;
IF OBJECT_ID(N'dbo.Courses', N'U')          IS NOT NULL DROP TABLE dbo.Courses;
IF OBJECT_ID(N'dbo.CourseCategories', N'U') IS NOT NULL DROP TABLE dbo.CourseCategories;
GO

/* ---------- 1. Loại khóa học ---------- */
CREATE TABLE dbo.CourseCategories (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    name        NVARCHAR(200)  NOT NULL,
    description NVARCHAR(1000) NULL,
    CONSTRAINT PK_CourseCategories PRIMARY KEY (id)
);
GO

/* ---------- 2. Khóa học ---------- */
CREATE TABLE dbo.Courses (
    id                 BIGINT IDENTITY(1,1) NOT NULL,
    name               NVARCHAR(200)  NOT NULL,
    tuition_fee        NUMERIC(18,2)  NOT NULL,
    duration           INT            NOT NULL,   -- số giờ
    is_active          BIT            NOT NULL CONSTRAINT DF_Courses_IsActive DEFAULT 1,
    description        NVARCHAR(2000) NULL,
    course_category_id BIGINT         NOT NULL,
    CONSTRAINT PK_Courses PRIMARY KEY (id),
    CONSTRAINT FK_Courses_CourseCategories
        FOREIGN KEY (course_category_id) REFERENCES dbo.CourseCategories(id),   -- không cho xóa loại nếu còn khóa học
    CONSTRAINT CK_Courses_TuitionFee CHECK (tuition_fee > 0),
    CONSTRAINT CK_Courses_Duration   CHECK (duration > 0),
    CONSTRAINT UQ_Course_Name_Category UNIQUE (name, course_category_id)       -- tên không trùng trong cùng loại
);
GO

/* ---------- 3. Ảnh khóa học ---------- */
CREATE TABLE dbo.CourseImages (
    id           BIGINT IDENTITY(1,1) NOT NULL,
    image_url    NVARCHAR(1000) NOT NULL,
    is_thumbnail BIT            NOT NULL CONSTRAINT DF_CourseImages_IsThumbnail DEFAULT 0,
    course_id    BIGINT         NOT NULL,
    CONSTRAINT PK_CourseImages PRIMARY KEY (id),
    CONSTRAINT FK_CourseImages_Courses
        FOREIGN KEY (course_id) REFERENCES dbo.Courses(id) ON DELETE CASCADE
);
GO

/* Mỗi khóa học chỉ có tối đa 1 ảnh đại diện */
CREATE UNIQUE INDEX UX_CourseImages_OneThumbnail
    ON dbo.CourseImages(course_id) WHERE is_thumbnail = 1;
GO

/* ===== DỮ LIỆU MẪU ===== */
GO

INSERT INTO dbo.CourseCategories (name, description) VALUES
(N'Lập trình Web',      N'Các khóa học phát triển ứng dụng web'),
(N'Lập trình Di động',  N'Các khóa học phát triển ứng dụng di động'),
(N'Cơ sở dữ liệu',      N'Các khóa học về thiết kế và quản trị CSDL');
GO

DECLARE @web BIGINT    = (SELECT id FROM dbo.CourseCategories WHERE name = N'Lập trình Web');
DECLARE @mobile BIGINT = (SELECT id FROM dbo.CourseCategories WHERE name = N'Lập trình Di động');
DECLARE @db BIGINT     = (SELECT id FROM dbo.CourseCategories WHERE name = N'Cơ sở dữ liệu');

INSERT INTO dbo.Courses (name, tuition_fee, duration, is_active, description, course_category_id) VALUES
(N'ASP.NET Core',        3500000, 45, 1, N'Xây dựng web với ASP.NET Core MVC',              @web),
(N'ReactJS',             2800000, 30, 1, N'Thư viện UI phổ biến',                           @web),
(N'Flutter',             3200000, 40, 1, N'Lập trình đa nền tảng với Flutter',              @mobile),
(N'Android Kotlin',      2500000, 25, 0, N'Lập trình Android với Kotlin',                   @mobile),  -- Ngừng tuyển sinh
(N'SQL Server cơ bản',   1500000, 20, 1, N'Nhập môn SQL Server',                            @db),
(N'PostgreSQL nâng cao', 2000000, 36, 1, N'Tối ưu truy vấn và quản trị PostgreSQL',         @db);
GO

/* Ảnh: ảnh đầu tiên của mỗi khóa là ảnh đại diện (is_thumbnail = 1) */
INSERT INTO dbo.CourseImages (image_url, is_thumbnail, course_id)
SELECT v.url, v.thumb, c.id
FROM (VALUES
    (N'ASP.NET Core',      N'https://picsum.photos/seed/aspnet/600/360',   1),
    (N'ASP.NET Core',      N'https://picsum.photos/seed/aspnet2/600/360',  0),
    (N'ReactJS',           N'https://picsum.photos/seed/react/600/360',    1),
    (N'Flutter',           N'https://picsum.photos/seed/flutter/600/360',  1),
    (N'Flutter',           N'https://picsum.photos/seed/flutter2/600/360', 0),
    (N'Android Kotlin',    N'https://picsum.photos/seed/kotlin/600/360',   1),
    (N'SQL Server cơ bản', N'https://picsum.photos/seed/sqlserver/600/360',1)
) AS v(course_name, url, thumb)
JOIN dbo.Courses c ON c.name = v.course_name;
-- PostgreSQL nâng cao: cố ý chưa có ảnh để demo trường hợp "Khóa học chưa có ảnh"
GO

/* Kiểm tra nhanh */
SELECT * FROM dbo.CourseCategories;
SELECT * FROM dbo.Courses;
SELECT * FROM dbo.CourseImages;
GO