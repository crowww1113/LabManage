-- 修改 schedule_application 表，让 experiment_content 和 student_count 可空
ALTER TABLE schedule_application ALTER COLUMN experiment_content SET NULL;
ALTER TABLE schedule_application ALTER COLUMN student_count SET NULL;

-- 通用预约不绑定课程，使用记录 course_id 允许为空
ALTER TABLE schedule_usage_record ALTER COLUMN course_id SET NULL;
