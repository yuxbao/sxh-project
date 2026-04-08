package com.github.paicoding.forum.service.image.oss.impl;

import com.github.paicoding.forum.core.autoconf.DynamicConfigContainer;
import com.github.paicoding.forum.core.config.ImageProperties;
import com.github.paicoding.forum.core.util.Md5Util;
import com.github.paicoding.forum.core.util.StopWatchUtil;
import com.github.paicoding.forum.service.image.oss.ImageUploader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

/**
 * 通用 S3 兼容对象存储上传实现
 *
 * @author XuYifei
 * @date 2026-03-29
 */
@Component
@ConditionalOnExpression(value = "#{'s3'.equals(environment.getProperty('image.oss.type'))}")
public class S3OssWrapper implements ImageUploader, InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(S3OssWrapper.class);

    @Autowired
    private ImageProperties properties;
    private S3Client s3Client;

    @Autowired
    private DynamicConfigContainer dynamicConfigContainer;

    @Override
    public String upload(InputStream input, String fileType) {
        StopWatchUtil stopWatchUtil = StopWatchUtil.init("图片上传");
        try {
            byte[] bytes = stopWatchUtil.record("流转字节", () -> StreamUtils.copyToByteArray(input));
            return upload(bytes, fileType);
        } catch (Exception e) {
            log.error("upload to s3 compatible storage error!", e);
            return "";
        }
    }

    public String upload(byte[] bytes, String fileType) {
        StopWatchUtil stopWatchUtil = StopWatchUtil.init("图片上传");
        try {
            String fileName = stopWatchUtil.record("md5计算", () -> Md5Util.encode(bytes));
            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
            String objectKey = buildObjectKey(fileName, getFileType(input, fileType));
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getOss().getBucket())
                    .key(objectKey)
                    .build();
            stopWatchUtil.record("文件上传", () -> s3Client.putObject(request, RequestBody.fromBytes(bytes)));
            return buildFileUrl(objectKey);
        } catch (Exception e) {
            log.error("upload to s3 compatible storage error!", e);
            return "";
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("upload image size:{} cost: {}", bytes.length, stopWatchUtil.prettyPrint());
            }
        }
    }

    @Override
    public boolean uploadIgnore(String fileUrl) {
        if (StringUtils.isNotBlank(properties.getOss().getHost()) && fileUrl.startsWith(properties.getOss().getHost())) {
            return true;
        }
        return !fileUrl.startsWith("http");
    }

    @Override
    public void afterPropertiesSet() {
        init();
        dynamicConfigContainer.registerRefreshCallback(properties, () -> {
            init();
            log.info("s3Client refreshed!");
        });
    }

    @Override
    public void destroy() {
        if (s3Client != null) {
            s3Client.close();
        }
    }

    private void init() {
        if (s3Client != null) {
            s3Client.close();
        }

        String endpoint = Objects.requireNonNull(properties.getOss().getEndpoint(), "image.oss.endpoint can not be null");
        String ak = Objects.requireNonNull(properties.getOss().getAk(), "image.oss.ak can not be null");
        String sk = Objects.requireNonNull(properties.getOss().getSk(), "image.oss.sk can not be null");
        String region = StringUtils.defaultIfBlank(properties.getOss().getRegion(), "auto");
        boolean pathStyleAccess = Boolean.TRUE.equals(properties.getOss().getPathStyleAccess());

        log.info("init s3Client, endpoint:{}, bucket:{}, region:{}, pathStyleAccess:{}",
                endpoint, properties.getOss().getBucket(), region, pathStyleAccess);

        s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ak, sk)))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(pathStyleAccess).build())
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    private String buildObjectKey(String fileName, String fileType) {
        String prefix = StringUtils.defaultString(properties.getOss().getPrefix());
        return prefix + fileName + "." + fileType;
    }

    private String buildFileUrl(String objectKey) {
        String host = StringUtils.defaultString(properties.getOss().getHost());
        if (StringUtils.isBlank(host)) {
            return objectKey;
        }
        return StringUtils.appendIfMissing(host, "/") + objectKey;
    }

    public ImageProperties getProperties() {
        return properties;
    }

    public void setProperties(ImageProperties properties) {
        this.properties = properties;
    }
}
