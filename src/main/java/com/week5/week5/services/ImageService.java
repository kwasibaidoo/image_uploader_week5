package com.week5.week5.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ImageService {
    

    public Page<String> getImages(int page, int size) {

        List<String> imageList = new ArrayList<>();
        int start = Math.min(page * size, imageList.size());
        int end = Math.min((page + 1) * size, imageList.size());
        List<String> paginatedImages = imageList.subList(start, end);
        return new PageImpl<>(paginatedImages, PageRequest.of(page, size), imageList.size());
        
    }
}
