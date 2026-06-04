package com.jellystudy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 头像文件存储：保存到上传目录的 avatars 子目录，返回可静态访问的 URL 路径。
 */
@Service
public class AvatarStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 保存头像，返回形如 /uploads/avatars/{userId}.{ext} 的访问路径。
     */
    public String store(String userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        String ext = resolveExtension(file.getOriginalFilename(), file.getContentType());

        Path dir = Paths.get(uploadDir, "avatars");
        Files.createDirectories(dir);

        // 固定文件名（userId.ext），覆盖旧头像，避免残留
        String filename = userId + "." + ext;
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/avatars/" + filename;
    }

    private String resolveExtension(String originalName, String contentType) {
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
            if (ext.matches("png|jpg|jpeg|gif|webp")) {
                return ext;
            }
        }
        if (contentType != null) {
            switch (contentType) {
                case "image/png":
                    return "png";
                case "image/jpeg":
                    return "jpg";
                case "image/gif":
                    return "gif";
                case "image/webp":
                    return "webp";
                default:
                    break;
            }
        }
        return "png";
    }
}
