package com.aichat.roleplay.service.impl;

import com.aichat.roleplay.service.IFileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 远程文件存储服务实现 - 通过HTTP上传到远程服务器
 */
@Service
public class LocalFileStorageServiceImpl implements IFileStorageService {

    private static final Logger logger = Logger.getLogger(LocalFileStorageServiceImpl.class.getName());

    @Value("${file.storage.upload-url}")
    private String uploadUrl;

    @Value("${file.storage.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public LocalFileStorageServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        logger.info("远程文件存储服务初始化完成，上传地址: " + uploadUrl + ", 访问地址: " + baseUrl);
    }

    @Override
    public String uploadAudioFile(MultipartFile audioFile, String fileName) {
        try {
            byte[] audioData = audioFile.getBytes();
            return uploadAudioFile(audioData, fileName, audioFile.getContentType());
        } catch (Exception e) {
            logger.severe("读取音频文件失败: " + e.getMessage());
            throw new RuntimeException("上传音频文件失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadAudioFile(byte[] audioData, String fileName, String contentType) {
        try {
            String uniqueFileName = generateUniqueFileName(fileName);
            
            logger.info("开始上传音频文件到远程服务器: " + uniqueFileName + ", 大小: " + audioData.length + " bytes");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            ByteArrayResource fileResource = new ByteArrayResource(audioData) {
                @Override
                public String getFilename() {
                    return uniqueFileName;
                }
            };
            
            body.add("file", fileResource);
            body.add("filename", uniqueFileName);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // 发送HTTP请求上传文件
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, requestEntity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                // 构建文件访问URL
                String fileUrl = buildFileUrl(uniqueFileName);
                logger.info("音频文件上传成功: " + uniqueFileName + " -> " + fileUrl);
                return fileUrl;
            } else {
                throw new RuntimeException("远程上传失败，状态码: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.severe("上传音频文件到远程服务器失败: " + e.getMessage());
            throw new RuntimeException("上传音频文件失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String fileName) {
        // todo 远程删除功能可以后续实现
        logger.info("远程删除文件功能暂未实现: " + fileName);
        return true;
    }

    /**
     * 生成唯一文件名
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        return String.format("audio_%s_%s%s", timestamp, uuid.substring(0, 8), extension);
    }


    private String getFileExtension(String fileName) {
        return ".mp3";
    }

    private String buildFileUrl(String fileName) {
        String normalizedBaseUrl = baseUrl;
        if (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        
        return normalizedBaseUrl + "/audio/" + fileName;
    }
}