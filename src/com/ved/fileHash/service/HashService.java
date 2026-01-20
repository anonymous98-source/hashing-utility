package com.ved.filehash.service;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class HashService {

    public static String generateHash(
            File file,
            String algorithm,
            ProgressCallback callback
    ) throws Exception {

        MessageDigest digest = MessageDigest.getInstance(algorithm);
        long totalBytes = file.length();
        long processed = 0;

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
                processed += bytesRead;

                if (callback != null) {
                    int progress = (int) ((processed * 100) / totalBytes);
                    callback.onProgress(progress);
                }
            }
        }

        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {      
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public interface ProgressCallback {
        void onProgress(int percent);
    }
}
