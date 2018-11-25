/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.intl.imagesearch.services.impl;

import com.alibaba.intl.imagesearch.model.RecognizableObject;
import com.alibaba.intl.imagesearch.model.dto.ImageRegion;
import com.alibaba.intl.imagesearch.model.dto.ImageSearchResponse;
import com.alibaba.intl.imagesearch.repositories.RecognizableObjectRepository;
import com.alibaba.intl.imagesearch.services.ImageSearchService;
import com.alibaba.intl.imagesearch.services.RecognizableObjectService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of {@link RecognizableObjectService} based on Alibaba Cloud SDK.
 *
 * @author Alibaba Cloud
 */
@Service
@Profile("!dummy")
public class RecognizableObjectServiceImpl implements RecognizableObjectService {

    private final ImageSearchService imageSearchService;
    private final RecognizableObjectRepository recognizableObjectRepository;

    public RecognizableObjectServiceImpl(ImageSearchService imageSearchService, RecognizableObjectRepository recognizableObjectRepository) {
        this.imageSearchService = imageSearchService;
        this.recognizableObjectRepository = recognizableObjectRepository;
    }

    @Override
    public RecognizableObject create(RecognizableObject object) {
        // Add the new object to the Image Search API
        imageSearchService.register(object.getImageData(), object.getImageType(), object.getCategory(), object.getUuid());

        // Save the object
        return recognizableObjectRepository.save(object);
    }

    @Override
    public RecognizableObject update(RecognizableObject object) {
        // Check the object exists and download the current version
        RecognizableObject existingObject = recognizableObjectRepository.findById(object.getUuid()).orElse(null);
        if (existingObject == null) {
            throw new IllegalArgumentException("No object exist with the UUID: " + object.getUuid());
        }

        // Prevent forbidden attribute modifications
        if (object.getImageType() != existingObject.getImageType()) {
            throw new IllegalArgumentException("The image type cannot be changed (" +
                    "existing object image type = " + existingObject.getImageType() +
                    ", updated object image type = " + object.getImageType() + ").");
        }

        // Update the object
        RecognizableObject savedObject = recognizableObjectRepository.save(object);

        // Refresh the object into the Image Search API if the category has changed
        if (savedObject.getCategory() != existingObject.getCategory()) {
            imageSearchService.register(savedObject.getImageData(), savedObject.getImageType(), savedObject.getCategory(), savedObject.getUuid());
        }

        return object;
    }

    @Override
    public boolean delete(String uuid) {
        // Delete the item from the Image Search API
        imageSearchService.unregister(uuid);

        // Delete the object
        recognizableObjectRepository.deleteById(uuid);

        return true;
    }

    @Override
    public RecognizableObject findByUuid(String uuid) {
        return recognizableObjectRepository.findById(uuid).orElse(null);
    }

    @Override
    public List<RecognizableObject> findAll() {
        return recognizableObjectRepository.findAll();
    }

    @Override
    public ImageSearchResponse findAllBySimilarImage(byte[] imageData, ImageRegion objectRegion) {
        // Search for similar images
        return imageSearchService.findAllBySimilarImage(imageData, objectRegion);
    }

}
