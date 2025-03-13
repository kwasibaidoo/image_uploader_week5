package com.week5.week5.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.week5.week5.services.ImageService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/")
public class HomeController {


    @Autowired
    private ImageService imageService;


    @GetMapping("/")
    public String index(@RequestParam(value = "page", defaultValue = "0") int page, 
                        @RequestParam(value = "size", defaultValue = "5") int size, 
                        @RequestParam(value = "continuationToken", required = false) String continuationToken,
                        Model model) {

        Pageable pageable = PageRequest.of(page, size);

        Page<String> imagesPage = imageService.getImages(pageable, continuationToken);
        
        // Pass the content directly to avoid issues with Thymeleaf iteration
        model.addAttribute("images", imagesPage.getContent());
        
        // Make sure totalPages is at least 1 to handle empty buckets
        int totalPages = Math.max(1, imagesPage.getTotalPages());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("continuationToken", continuationToken);

        return "index";
    }

    @GetMapping("/upload")
    public String addimage() {
        return "addimage";
    }


    @PostMapping("/upload")
    public String uploadFiles(@RequestParam("images") MultipartFile[] files, RedirectAttributes redirectAttributes) {
        try {
            String response = imageService.uploadMultipleFiles(files);
            if(response.equals("success")) {
                redirectAttributes.addFlashAttribute("message", "Successfully uploaded");
            }
            else if(response.equals("empty")) {
                redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
                return "redirect:/upload";
            }
            else if(response.equals("max")) {
                redirectAttributes.addFlashAttribute("message", "Upload file. Max number of images is 5");
                return "redirect:/upload";
            }
            else if(response.equals("size")) {
                redirectAttributes.addFlashAttribute("message", "Max image size is 1mb");
                return "redirect:/upload";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Failed to upload file: " + e.getMessage());
            return "redirect:/upload";
        }
        
        return "redirect:/";
    }
    
}