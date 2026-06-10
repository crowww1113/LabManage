-- 实验教学任务测试数据
-- 基于数据库中已有的学期、课程、班级数据

-- 1. Java程序设计实验 - 计算机2023级1班
INSERT INTO exp_task (term, major, class_id, student_count, student_level, course_name, course_type, independent_course, total_exp_hour, current_exp_hour, total_practice_hour, current_practice_hour, total_training_hour, current_training_hour, organization, department, teacher, teacher_title, technician, technician_title, textbook_name, guidebook_name, created_at, updated_at)
VALUES ('2025-2026学年第二学期', '计算机科学与技术', 1, 45, '本科', 'Java程序设计', '专业必修', true, 12, 12, 0, 0, 0, 0, '计算机学院', '软件工程系', '王华', '副教授', '李工', '实验师', 'Java程序设计教程', 'Java实验指导书', NOW(), NOW());

-- 2. 数据库原理与应用实验 - 软件工程2024级1班
INSERT INTO exp_task (term, major, class_id, student_count, student_level, course_name, course_type, independent_course, total_exp_hour, current_exp_hour, total_practice_hour, current_practice_hour, total_training_hour, current_training_hour, organization, department, teacher, teacher_title, technician, technician_title, textbook_name, guidebook_name, created_at, updated_at)
VALUES ('2025-2026学年第二学期', '软件工程', 3, 48, '本科', '数据库原理与应用', '专业必修', true, 12, 12, 0, 0, 0, 0, '计算机学院', '软件工程系', '张明', '教授', '赵工', '高级实验师', '数据库系统概论', '数据库实验指导书', NOW(), NOW());

-- 3. Python程序设计实验 - 大数据2024级1班
INSERT INTO exp_task (term, major, class_id, student_count, student_level, course_name, course_type, independent_course, total_exp_hour, current_exp_hour, total_practice_hour, current_practice_hour, total_training_hour, current_training_hour, organization, department, teacher, teacher_title, technician, technician_title, textbook_name, guidebook_name, created_at, updated_at)
VALUES ('2025-2026学年第二学期', '数据科学与大数据技术', 4, 52, '本科', 'Python程序设计', '专业必修', true, 8, 8, 0, 0, 0, 0, '计算机学院', '大数据系', '刘强', '讲师', '陈工', '实验师', 'Python编程从入门到实践', 'Python实验指导书', NOW(), NOW());

-- 4. Web前端开发实验 - 计算机2023级2班
INSERT INTO exp_task (term, major, class_id, student_count, student_level, course_name, course_type, independent_course, total_exp_hour, current_exp_hour, total_practice_hour, current_practice_hour, total_training_hour, current_training_hour, organization, department, teacher, teacher_title, technician, technician_title, textbook_name, guidebook_name, created_at, updated_at)
VALUES ('2025-2026学年第二学期', '计算机科学与技术', 2, 42, '本科', 'Web前端开发', '专业必修', true, 16, 16, 0, 0, 0, 0, '计算机学院', '软件工程系', '赵丽', '副教授', '周工', '实验师', 'Web前端开发实战', 'Web前端实验指导书', NOW(), NOW());

-- 5. 算法与数据结构实验 - 计算机2023级1班
INSERT INTO exp_task (term, major, class_id, student_count, student_level, course_name, course_type, independent_course, total_exp_hour, current_exp_hour, total_practice_hour, current_practice_hour, total_training_hour, current_training_hour, organization, department, teacher, teacher_title, technician, technician_title, textbook_name, guidebook_name, created_at, updated_at)
VALUES ('2025-2026学年第二学期', '计算机科学与技术', 1, 45, '本科', '算法与数据结构', '专业必修', false, 8, 8, 0, 0, 0, 0, '计算机学院', '计算机系', '孙伟', '教授', '吴工', '高级实验师', '算法导论', '算法实验指导书', NOW(), NOW());

-- 6. 课外自主实验 - 软件工程2024级1班
INSERT INTO exp_task (term, major, class_id, student_count, student_level, course_name, course_type, independent_course, total_exp_hour, current_exp_hour, total_practice_hour, current_practice_hour, total_training_hour, current_training_hour, organization, department, teacher, teacher_title, technician, technician_title, textbook_name, guidebook_name, created_at, updated_at)
VALUES ('2025-2026学年第二学期', '软件工程', 3, 48, '本科', '课外自主实验', '选修', true, 16, 16, 0, 0, 0, 0, '计算机学院', '软件工程系', '钱进', '讲师', '郑工', '实验师', '自主实验指导', '自主实验指导书', NOW(), NOW());
