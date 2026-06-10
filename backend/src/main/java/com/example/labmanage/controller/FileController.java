package com.example.labmanage.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.upload-dir:./uploads/equipment}")
    private String uploadDir;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String savedName = UUID.randomUUID().toString() + ext;

        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        Path targetPath = dir.resolve(savedName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Map<String, Object> result = new HashMap<>();
        result.put("fileName", originalFilename);
        result.put("savedName", savedName);
        result.put("filePath", savedName);
        result.put("fileSize", file.getSize());
        result.put("contentType", file.getContentType());
        return result;
    }

    @GetMapping("/download/{savedName}")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<byte[]> download(@PathVariable String savedName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(savedName);
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = Files.readAllBytes(filePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + savedName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @DeleteMapping("/{savedName}")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public Map<String, Object> delete(@PathVariable String savedName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(savedName);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("message", "删除成功");
        return result;
    }
}
