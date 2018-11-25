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

import com.alibaba.intl.imagesearch.model.RecognizableObject;
import com.alibaba.intl.imagesearch.model.dto.ImageSearchResponse;
import com.alibaba.intl.imagesearch.model.dto.ImageRegion;

import java.util.List;

/**
 * Provide methods to find and manage {@link RecognizableObject}s.
 *
 * @author Alibaba Cloud
 */
public interface RecognizableObjectService {

    /**
     * Create an object in the database.
     * Note: the image is automatically resized
     *
     * @return Created object.
     */
    RecognizableObject create(RecognizableObject object);

    /**
     * Update an object in the database.
     * Note: the id and image file cannot be modified.
     *
     * @return Updated object.
     */
    RecognizableObject update(RecognizableObject object);

    /**
     * Delete the object with the given uuid.
     *
     * @return true if the object has been deleted successfully, false if the object doesn't exist.
     */
    boolean delete(String uuid);

    /**
     * @return Object with the given uuid.
     */
    RecognizableObject findByUuid(String uuid);

    /**
     * Find all objects sorted in alpha-numeric order.
     *
     * @return All registered objects.
     */
    List<RecognizableObject> findAll();

    /**
     * Find all objects that match the given image.
     *
     * @param imageData
     * @param objectRegion
     * @return Found objects with their score.
     */
    ImageSearchResponse findAllBySimilarImage(byte[] imageData, ImageRegion objectRegion);
}
