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

package com.alibaba.intl.imagesearch.services;

import com.alibaba.intl.imagesearch.exceptions.InvalidConfigurationException;
import com.alibaba.intl.imagesearch.model.Configuration;
import com.alibaba.intl.imagesearch.model.ObjectCategory;
import com.alibaba.intl.imagesearch.model.ObjectImageType;
import com.alibaba.intl.imagesearch.model.dto.ImageSearchResponse;
import com.alibaba.intl.imagesearch.model.dto.ImageRegion;

/**
 * Provide services from the image search API.
 *
 * @author Alibaba Cloud
 */
public interface ImageSearchService {

    /**
     * Register an image into the search instance.
     *
     * @param imageData Image data in JPEG or PNG format.
     * @param imageType Image format.
     * @param category  Product category.
     * @param uuid      Unique identifier of the image.
     */
    void register(byte[] imageData, ObjectImageType imageType, ObjectCategory category, String uuid);

    /**
     * Un-register an image from the search instance.
     *
     * @param uuid Unique identifier of the image.
     */
    void unregister(String uuid);

    /**
     * Find all images similar to the given one.
     *
     * @param imageData    Image to match with registered ones in the search instance.
     * @param objectRegion object region to search.
     * @return Found images UUIDs and raw response.
     */
    ImageSearchResponse findAllBySimilarImage(byte[] imageData, ImageRegion objectRegion);

    /**
     * Check the image search configuration is correct by making a fake search request.
     *
     * @param configuration Configuration to check.
     */
    void checkImageSearchConfiguration(Configuration configuration) throws InvalidConfigurationException;
}
