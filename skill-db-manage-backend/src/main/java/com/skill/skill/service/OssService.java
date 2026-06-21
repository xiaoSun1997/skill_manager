package com.skill.skill.service;

import java.io.InputStream;
import java.net.URL;

public interface OssService {
    String upload(InputStream inputStream, long contentLength, String ossKey, String contentType);
    InputStream download(String ossKey);
    void delete(String ossKey);
    URL generatePresignedUrl(String ossKey, long expirationSeconds);
}
