package com.week5.week5.services;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
public class ImageService {

    
    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    private String BUCKET_NAME = "week5labbucketimageuploader";

    public Page<String> getImages(Pageable pageable, String continuationToken) {
        // First, get the total count of images to properly calculate pagination
        long totalImageCount = countTotalImages();
        
        // Then fetch the specific page
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .maxKeys(pageable.getPageSize())
                .continuationToken(continuationToken)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsV2Request);

        List<String> imageList = response.contents().stream()
                .filter(s3Object -> isImage(s3Object.key()))
                .map(obj -> generatePresignedUrl(obj.key()))
                .collect(Collectors.toList());
       
        // Use the total count instead of keyCount for accurate pagination
        return new PageImpl<>(imageList, pageable, totalImageCount);
    }

    private long countTotalImages() {
        // This method counts all images in the bucket to calculate pagination
        ListObjectsV2Request countRequest = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build();
        
        ListObjectsV2Response response = s3Client.listObjectsV2(countRequest);
        
        // Count only image files
        return response.contents().stream()
                .filter(s3Object -> isImage(s3Object.key()))
                .count();
    }

    public String generatePresignedUrl(String objectKey) {
        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(objectKey)
                        .build())
                .build();
        
        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presignedGetObjectRequest.url().toString();
    }

    private boolean isImage(String key) {
        return key.toLowerCase().endsWith(".jpg") || 
               key.toLowerCase().endsWith(".jpeg") || 
               key.toLowerCase().endsWith(".png");
    }


    public String uploadMultipleFiles(MultipartFile[] files) throws IOException {
        // Filter out empty files
        List<MultipartFile> nonEmptyFiles = Arrays.stream(files)
                .filter(file -> !file.isEmpty())
                .collect(Collectors.toList());
        
        // If all files were empty, return an appropriate response
        if (nonEmptyFiles.isEmpty()) {
            return "empty";
        }
        
        if (nonEmptyFiles.size() > 5) {
            return "max";
        }
        
        for (MultipartFile file : nonEmptyFiles) {
            // check file size
            if (file.getSize() > 1000000) {
                return "size";
            }
            String key = "image_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
        }
    
        return "success";
    }
}