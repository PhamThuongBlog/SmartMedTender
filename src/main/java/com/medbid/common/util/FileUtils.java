package com.medbid.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public final class FileUtils {

    private FileUtils() {}

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "docx", "doc", "xlsx", "xls", "zip", "png", "jpg", "jpeg"
    );

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel",
            "application/zip",
            "application/x-zip-compressed",
            "image/png",
            "image/jpeg"
    );

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    public static boolean isAllowedExtension(String filename) {
        if (filename == null) return false;
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return false;
        return ALLOWED_EXTENSIONS.contains(filename.substring(dot + 1).toLowerCase());
    }

    public static boolean isAllowedMimeType(String mimeType) {
        return mimeType != null && ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase());
    }

    public static boolean isFileSizeValid(long size) {
        return size > 0 && size <= MAX_FILE_SIZE;
    }

    public static String sanitizeFilename(String filename) {
        if (filename == null) return "unknown";
        return filename.replaceAll("[^a-zA-Z0-9._\\-\\p{L}]", "_");
    }
}
