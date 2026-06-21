package com.skill.skill.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.skill.skill.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OSS ossClient;

    @Value("${oss.bucketName}")
    private String bucketName;

    @Override
    public String upload(InputStream inputStream, long contentLength, String ossKey, String contentType) {
        ossClient.putObject(bucketName, ossKey, inputStream);
        log.info("OSS upload success: {}", ossKey);
        return ossKey;
    }

    @Override
    public InputStream download(String ossKey) {
        OSSObject ossObject = ossClient.getObject(bucketName, ossKey);
        return ossObject.getObjectContent();
    }

    @Override
    public void delete(String ossKey) {
        ossClient.deleteObject(bucketName, ossKey);
        log.info("OSS delete success: {}", ossKey);
    }

    @Override
    public URL generatePresignedUrl(String ossKey, long expirationSeconds) {
        Date expiration = new Date(System.currentTimeMillis() + expirationSeconds * 1000);
        return ossClient.generatePresignedUrl(bucketName, ossKey, expiration);
    }
}
