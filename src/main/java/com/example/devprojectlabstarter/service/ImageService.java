package com.example.devprojectlabstarter.service;


import com.example.devprojectlabstarter.entity.Image;
import com.example.devprojectlabstarter.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    public void saveImage(String url) {
        Image image = new Image();
        image.setUrl(url);
        imageRepository.save(image);
    }



}