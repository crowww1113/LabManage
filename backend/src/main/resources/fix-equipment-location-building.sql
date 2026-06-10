-- =====================================================
-- 修复设备位置所属楼栋关联
-- 执行此脚本将设备位置与场馆管理的楼宇表关联
-- =====================================================

-- 更新设备位置的 building_id 字段
-- 设备仓库和仪器室 -> 实验大楼
UPDATE equipment_location 
SET building_id = (SELECT id FROM sys_building WHERE name = '实验大楼' LIMIT 1) 
WHERE code IN ('LOC-WH-01', 'LOC-WH-02', 'LOC-LAB-01', 'LOC-LAB-02', 'LOC-STORAGE');

-- 维修间 -> 理工楼
UPDATE equipment_location 
SET building_id = (SELECT id FROM sys_building WHERE name = '理工楼' LIMIT 1) 
WHERE code IN ('LOC-REP-01', 'LOC-REP-02');

-- 验证更新结果
SELECT el.code, el.name, el.building_id, sb.name as building_name 
FROM equipment_location el 
LEFT JOIN sys_building sb ON el.building_id = sb.id
ORDER BY el.code;
