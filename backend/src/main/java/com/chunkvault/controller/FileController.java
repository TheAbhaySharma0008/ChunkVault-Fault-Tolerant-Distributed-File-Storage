package com.chunkvault.controller;

import com.chunkvault.model.FileMetadata;
import com.chunkvault.model.UploadResponse;
import com.chunkvault.service.FileStorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    private final FileStorageService service;

    public FileController(FileStorageService service) {
        this.service = service;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        UploadResponse response = service.upload(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}/metadata")
    public ResponseEntity<FileMetadata> metadata(@PathVariable String fileId) {
        return ResponseEntity.ok(service.getMetadata(fileId));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<byte[]> download(@PathVariable String fileId) {
        FileMetadata meta = service.getMetadata(fileId);
        byte[] bytes = service.reconstruct(fileId);

        String encodedName = URLEncoder.encode(meta.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", encodedName);
        headers.setContentLength(bytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }

    @GetMapping
    public ResponseEntity<?> listFiles() {
        return ResponseEntity.ok(service.getAllFiles());
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> delete(@PathVariable String fileId) {
        service.delete(fileId);
        return ResponseEntity.ok().build();
    }
}
