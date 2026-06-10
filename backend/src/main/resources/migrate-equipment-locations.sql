-- 更新设备位置数据，使其符合命名规范
-- 将教室编号改为设备存放位置名称

UPDATE equipment_location SET code = 'LOC-WH-01', name = '设备仓库-电子类', room_number = 'A101', description = '存放电子类设备' WHERE code = 'LOC-A101';
UPDATE equipment_location SET code = 'LOC-WH-02', name = '设备仓库-机械类', room_number = 'A102', description = '存放机械类设备' WHERE code = 'LOC-A102';
UPDATE equipment_location SET code = 'LOC-LAB-01', name = '仪器室-光学类', room_number = 'A201', description = '存放光学仪器' WHERE code = 'LOC-A201';
UPDATE equipment_location SET code = 'LOC-LAB-02', name = '仪器室-测量类', room_number = 'A301', description = '存放测量仪器' WHERE code = 'LOC-A301';
UPDATE equipment_location SET code = 'LOC-REP-01', name = '维修间-电子类', room_number = 'B101', description = '电子设备维修间' WHERE code = 'LOC-B101';
UPDATE equipment_location SET code = 'LOC-REP-02', name = '维修间-机械类', room_number = 'B201', description = '机械设备维修间' WHERE code = 'LOC-B201';
UPDATE equipment_location SET code = 'LOC-STORAGE', name = '设备仓库-耗材区', room_number = 'A100', description = '设备存放仓库' WHERE code = 'LOC-STORAGE';
