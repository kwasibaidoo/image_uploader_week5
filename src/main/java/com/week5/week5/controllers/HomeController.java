package com.week5.week5.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.week5.week5.services.ImageService;

import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private ImageService imageService;

    @GetMapping("/")
    public String index(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size, Model model) {

        Page<String> images = imageService.getImages(page, size);
        model.addAttribute("images", images.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", images.getTotalPages());
        model.addAttribute("totalItems", images.getTotalElements());
        model.addAttribute("pageSize", size);
        return "index";

    }

    @GetMapping("/upload")
    public String addimage() {
        return "addimage";
    }
    
}
