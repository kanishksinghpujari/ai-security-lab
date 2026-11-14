package dev.kanishk.aisecuritylab.api;

import dev.kanishk.aisecuritylab.security.ClamAvScanner;
import dev.kanishk.aisecuritylab.security.ClamAvScanner.ScanResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private final ClamAvScanner scanner;

    public UploadController(ClamAvScanner scanner) {
        this.scanner = scanner;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A non-empty file is required");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Maximum upload size is 5 MiB");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read upload");
        }

        String mediaType = detectAllowedType(content);
        if (mediaType == null) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Only PNG, JPEG, PDF and plain text files are accepted");
        }

        ScanResult result;
        try {
            result = scanner.scan(content);
        } catch (IOException exception) {
            // Fail closed: an unavailable scanner must never be treated as a clean file.
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Malware scanner unavailable; upload rejected");
        }
        if (!result.clean()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Upload rejected by malware scanner");
        }

        String safeName = safeFileName(file.getOriginalFilename());
        return new UploadResponse(safeName, content.length, mediaType, result.status());
    }

    private String detectAllowedType(byte[] bytes) {
        if (startsWith(bytes, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})) {
            return "image/png";
        }
        if (startsWith(bytes, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
            return "image/jpeg";
        }
        if (startsWith(bytes, "%PDF-".getBytes(StandardCharsets.US_ASCII))) {
            return "application/pdf";
        }
        if (isPlainText(bytes)) {
            return "text/plain";
        }
        return null;
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (value[i] != prefix[i]) return false;
        }
        return true;
    }

    private boolean isPlainText(byte[] bytes) {
        if (bytes.length == 0) return false;
        for (byte value : bytes) {
            int c = value & 0xFF;
            if (c == 9 || c == 10 || c == 13 || (c >= 32 && c <= 126)) continue;
            return false;
        }
        return true;
    }

    private String safeFileName(String original) {
        if (original == null || original.isBlank()) return "upload";
        String name = original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        return name.substring(0, Math.min(name.length(), 100));
    }

    public record UploadResponse(String fileName, int bytes, String mediaType, String scanStatus) {}
}
