package com.example.labmanage.controller;

import com.example.labmanage.dto.EquipmentCreateRequest;
import com.example.labmanage.dto.EquipmentDTO;
import com.example.labmanage.entity.EquipmentCategoryEntity;
import com.example.labmanage.entity.EquipmentLocationEntity;
import com.example.labmanage.entity.OrgEntity;
import com.example.labmanage.repository.EquipmentCategoryRepository;
import com.example.labmanage.repository.EquipmentLocationRepository;
import com.example.labmanage.repository.EquipmentRepository;
import com.example.labmanage.repository.OrgRepository;
import com.example.labmanage.service.EquipmentService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentImportController {

    private final EquipmentService equipmentService;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentCategoryRepository categoryRepository;
    private final EquipmentLocationRepository locationRepository;
    private final OrgRepository orgRepository;

    public EquipmentImportController(EquipmentService equipmentService,
                                     EquipmentRepository equipmentRepository,
                                     EquipmentCategoryRepository categoryRepository,
                                     EquipmentLocationRepository locationRepository,
                                     OrgRepository orgRepository) {
        this.equipmentService = equipmentService;
        this.equipmentRepository = equipmentRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.orgRepository = orgRepository;
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public Map<String, Object> importEquipment(@RequestParam("file") MultipartFile file) {
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failList = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            // 第1行是表头，从第2行开始读
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                int rowNum = i + 1;
                try {
                    EquipmentCreateRequest req = parseRow(row);
                    EquipmentDTO dto = equipmentService.create(req);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("row", rowNum);
                    item.put("assetNo", dto.getAssetNo());
                    item.put("name", dto.getName());
                    successList.add(item);
                } catch (Exception e) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("row", rowNum);
                    item.put("error", e.getMessage());
                    failList.add(item);
                }
            }
        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("code", 500);
            result.put("message", "文件解析失败：" + e.getMessage());
            return result;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("message", "导入完成");
        result.put("successCount", successList.size());
        result.put("failCount", failList.size());
        result.put("successList", successList);
        result.put("failList", failList);
        return result;
    }

    @GetMapping("/import-template")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public Map<String, Object> getTemplateInfo() {
        List<Map<String, String>> columns = Arrays.asList(
                col("资产编号*", "必填，唯一标识"),
                col("设备名称*", "必填"),
                col("型号", ""),
                col("类别编码", "如：MECHANICAL"),
                col("计量单位", "如：台"),
                col("品牌", ""),
                col("序列号", ""),
                col("规格", ""),
                col("价格", "数字"),
                col("经费来源", ""),
                col("购入日期", "格式：yyyy-MM-dd"),
                col("使用年限", "数字"),
                col("供应商", ""),
                col("保修期(月)", "数字"),
                col("存放位置编码", "如：LAB-101"),
                col("所属部门编码", "如：DEPT-CS"),
                col("是否重要设备", "是/否"),
                col("标签", "多个用逗号分隔"),
                col("备注", "")
        );
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("columns", columns);
        return result;
    }

    private static Map<String, String> col(String name, String note) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("note", note);
        return m;
    }

    private EquipmentCreateRequest parseRow(Row row) {
        EquipmentCreateRequest req = new EquipmentCreateRequest();

        String assetNo = getCellString(row, 0);
        if (assetNo == null || assetNo.trim().isEmpty()) {
            throw new IllegalArgumentException("资产编号不能为空");
        }
        if (equipmentRepository.existsByAssetNo(assetNo)) {
            throw new IllegalArgumentException("资产编号已存在：" + assetNo);
        }
        req.setAssetNo(assetNo.trim());

        String name = getCellString(row, 1);
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("设备名称不能为空（第" + (row.getRowNum() + 1) + "行）");
        }
        req.setName(name.trim());

        req.setModel(getCellString(row, 2));

        String categoryCode = getCellString(row, 3);
        if (categoryCode != null && !categoryCode.isEmpty()) {
            Optional<EquipmentCategoryEntity> cat = categoryRepository.findByCode(categoryCode.trim());
            if (cat.isPresent()) {
                req.setCategoryId(cat.get().getId());
            }
        }

        req.setUnit(getCellString(row, 4));
        req.setBrand(getCellString(row, 5));
        req.setSerialNo(getCellString(row, 6));
        req.setSpec(getCellString(row, 7));

        Double price = getCellDouble(row, 8);
        if (price != null) req.setPrice(BigDecimal.valueOf(price));

        req.setFundSource(getCellString(row, 9));

        String purchaseDate = getCellString(row, 10);
        if (purchaseDate != null && !purchaseDate.isEmpty()) {
            req.setPurchaseDate(LocalDate.parse(purchaseDate.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        Long useYears = getCellLong(row, 11);
        if (useYears != null) req.setUseYears(useYears.intValue());

        req.setSupplier(getCellString(row, 12));

        Long warranty = getCellLong(row, 13);
        if (warranty != null) req.setWarrantyMonths(warranty.intValue());

        String locationCode = getCellString(row, 14);
        if (locationCode != null && !locationCode.isEmpty()) {
            Optional<EquipmentLocationEntity> loc = locationRepository.findByCode(locationCode.trim());
            if (loc.isPresent()) {
                req.setLocationId(loc.get().getId());
            }
        }

        String deptCode = getCellString(row, 15);
        if (deptCode != null && !deptCode.isEmpty()) {
            Optional<OrgEntity> dept = orgRepository.findByCode(deptCode.trim());
            if (dept.isPresent()) {
                req.setDeptId(dept.get().getId());
            }
        }

        String important = getCellString(row, 16);
        req.setIsImportant("是".equals(important));

        req.setTags(getCellString(row, 17));
        req.setRemark(getCellString(row, 18));

        return req;
    }

    private String getCellString(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val)) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            case BOOLEAN:
                return cell.getBooleanCellValue() ? "是" : "否";
            default:
                return null;
        }
    }

    private Double getCellDouble(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        String s = getCellString(row, colIdx);
        if (s != null && !s.isEmpty()) {
            try { return Double.parseDouble(s.trim()); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Long getCellLong(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        }
        String s = getCellString(row, colIdx);
        if (s != null && !s.isEmpty()) {
            try { return Long.parseLong(s.trim()); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
