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

import com.alibaba.intl.imagesearch.model.ObjectCategory;
import com.alibaba.intl.imagesearch.model.ObjectImageType;
import com.alibaba.intl.imagesearch.model.RecognizableObject;
import com.alibaba.intl.imagesearch.model.dto.ImageRegion;
import com.alibaba.intl.imagesearch.model.dto.ImageSearchAuction;
import com.alibaba.intl.imagesearch.model.dto.ImageSearchResponse;
import com.alibaba.intl.imagesearch.model.dto.ImageStoreType;
import com.alibaba.intl.imagesearch.services.RecognizableObjectService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Dummy implementation of {@link RecognizableObjectService}.
 *
 * @author Alibaba Cloud
 */
@Service
@Profile("dummy")
public class DummyRecognizableObjectServiceImpl implements RecognizableObjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyRecognizableObjectServiceImpl.class);

    private List<RecognizableObject> objects = new ArrayList<>(Arrays.asList(
            new RecognizableObject("2f2431d9-f4ce-48c6-990c-44221ef102d6", "Object A", ObjectCategory.OTHERS, ObjectImageType.JPEG,
                    loadResource("samples/2f2431d9-f4ce-48c6-990c-44221ef102d6.jpg"),
                    loadResource("samples/2f2431d9-f4ce-48c6-990c-44221ef102d6.jpg")),
            new RecognizableObject("a19c24f2-d2b3-4920-997f-5958210e6518", "Object B", ObjectCategory.OTHERS, ObjectImageType.JPEG,
                    loadResource("samples/a19c24f2-d2b3-4920-997f-5958210e6518.jpg"),
                    loadResource("samples/a19c24f2-d2b3-4920-997f-5958210e6518.jpg")),
            new RecognizableObject("0761a462-f46c-4b6d-9017-075fbe2a3797", "Object C", ObjectCategory.OTHERS, ObjectImageType.JPEG,
                    loadResource("samples/0761a462-f46c-4b6d-9017-075fbe2a3797.jpg"),
                    loadResource("samples/0761a462-f46c-4b6d-9017-075fbe2a3797.jpg")),
            new RecognizableObject("229ac3d7-2301-4e86-9eac-9fa28d7c8bcb", "Object D", ObjectCategory.OTHERS, ObjectImageType.JPEG,
                    loadResource("samples/229ac3d7-2301-4e86-9eac-9fa28d7c8bcb.jpg"),
                    loadResource("samples/229ac3d7-2301-4e86-9eac-9fa28d7c8bcb.jpg")),
            new RecognizableObject("2a5ddd8f-69fb-434c-b285-27ab57ea555d", "Object E", ObjectCategory.OTHERS, ObjectImageType.JPEG,
                    loadResource("samples/2a5ddd8f-69fb-434c-b285-27ab57ea555d.jpg"),
                    loadResource("samples/2a5ddd8f-69fb-434c-b285-27ab57ea555d.jpg")),
            new RecognizableObject("f328d358-6cf0-4aff-855a-f2c0d7fa55d9", "Object F", ObjectCategory.OTHERS, ObjectImageType.JPEG,
                    loadResource("samples/f328d358-6cf0-4aff-855a-f2c0d7fa55d9.jpg"),
                    loadResource("samples/f328d358-6cf0-4aff-855a-f2c0d7fa55d9.jpg"))
    ));

    @Override
    public RecognizableObject create(RecognizableObject object) {
        Optional<RecognizableObject> existingObject = objects.stream()
                .filter(o -> o.getUuid().equals(object.getUuid()))
                .findAny();
        if (existingObject.isPresent()) {
            LOGGER.warn("An object with the same UUID already exists (uuid = {}).", object.getUuid());
            return existingObject.get();
        }

        objects.add(object);

        return object;
    }

    @Override
    public RecognizableObject update(RecognizableObject object) {
        objects.remove(object);
        objects.add(object);
        return object;
    }

    @Override
    public boolean delete(String uuid) {
        return objects.remove(new RecognizableObject(uuid, null, null, null));
    }

    @Override
    public RecognizableObject findByUuid(String uuid) {
        return objects.stream()
                .filter(o -> o.getUuid().equals(uuid))
                .findAny()
                .orElse(null);
    }

    @Override
    public List<RecognizableObject> findAll() {
        return objects;
    }

    @Override
    public ImageSearchResponse findAllBySimilarImage(byte[] imageData, ImageRegion objectRegion) {
        return new ImageSearchResponse(objects.stream()
                .map(o -> new ImageSearchAuction(o.getUuid(), o.getCategory().getId(), o.getName(), ImageStoreType.DATABASE, 4.2F, null))
                .collect(Collectors.toList()), "", new ImageRegion(0, 0, 100, 100));
    }

    private static byte[] loadResource(String path) {
        Resource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            LOGGER.warn("Unable to load the resource: " + path, e);
            return null;
        }
    }
}
