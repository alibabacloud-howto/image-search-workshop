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

import com.alibaba.intl.imagesearch.AbstractTest;
import com.alibaba.intl.imagesearch.model.ObjectCategory;
import com.alibaba.intl.imagesearch.model.ObjectImageType;
import com.alibaba.intl.imagesearch.model.RecognizableObject;
import com.alibaba.intl.imagesearch.model.dto.ImageRegion;
import com.alibaba.intl.imagesearch.model.dto.ImageSearchAuction;
import com.alibaba.intl.imagesearch.model.dto.ImageSearchResponse;
import com.alibaba.intl.imagesearch.model.dto.ImageStoreType;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test the {@link RecognizableObjectService}.
 *
 * @author Alibaba Cloud
 */
public class RecognizableObjectServiceTest extends AbstractTest {

    @Value("classpath:samples/kettle.jpg")
    private Resource imageResource;

    @Autowired
    private RecognizableObjectService recognizableObjectService;

    private byte[] imageData;
    private ImageSearchService originalImageSearchService = null;
    private ImageSearchService mockImageSearchService = mock(ImageSearchService.class);

    @Before
    public void loadImageData() throws IOException {
        try (InputStream inputStream = imageResource.getInputStream()) {
            imageData = IOUtils.toByteArray(inputStream);
        }
    }

    @Before
    public void setMockImageSearchService() {
        originalImageSearchService = (ImageSearchService) ReflectionTestUtils.getField(recognizableObjectService, "imageSearchService");
        ReflectionTestUtils.setField(recognizableObjectService, "imageSearchService", mockImageSearchService);
    }

    @After
    public void restoreImageSearchService() {
        ReflectionTestUtils.setField(recognizableObjectService, "imageSearchService", originalImageSearchService);
    }

    /**
     * Test the complete lifecycle of an object:
     * <ol>
     * <li>Create an object.</li>
     * <li>Check we can find it.</li>
     * <li>Update the object.</li>
     * <li>Check again we can find it.</li>
     * <li>Delete the object.</li>
     * </ol>
     */
    @Test
    public void testObjectLifecycle() {
        // Create the object
        String uuid = UUID.randomUUID().toString();
        RecognizableObject object = new RecognizableObject(uuid, "testObjectLifecycleImage", ObjectCategory.OTHERS, ObjectImageType.JPEG, imageData, imageData);
        RecognizableObject createdObject = recognizableObjectService.create(object);
        assertEquals(createdObject, object);
        verify(mockImageSearchService).register(imageData, ObjectImageType.JPEG, ObjectCategory.OTHERS, uuid);

        // Find the object
        ImageRegion objectRegion = new ImageRegion(0, 0, 100, 100);
        List<ImageSearchAuction> imageSearchAuctionList = new ArrayList<ImageSearchAuction>();
        imageSearchAuctionList.add(new ImageSearchAuction("2490233", ObjectCategory.OTHERS.getId(), "342323901.png", ImageStoreType.OSS, 4.2F, null));
        imageSearchAuctionList.add(new ImageSearchAuction(object.getUuid(), ObjectCategory.OTHERS.getId(), object.getName(), ImageStoreType.DATABASE, 4.2F, null));
        when(mockImageSearchService.findAllBySimilarImage(imageData, objectRegion)).thenReturn(
                new ImageSearchResponse(imageSearchAuctionList, "fake-json", null));

        ImageSearchResponse imageSearchResponse = recognizableObjectService.findAllBySimilarImage(imageData, objectRegion);
        assertEquals(imageSearchAuctionList.size(), imageSearchResponse.getImageSearchAuctions().size());

        ImageSearchAuction objectWithScore = imageSearchResponse.getImageSearchAuctions().stream()
                .filter(ows -> ows.getItemId().equals(object.getUuid()))
                .findFirst()
                .orElse(null);
        assertNotNull(objectWithScore);
        assertEquals(4, (int) objectWithScore.getSimilarityScore());
        assertEquals(object.getUuid(), objectWithScore.getItemId());
        assertEquals(object.getName(), objectWithScore.getPicName());
        assertEquals(object.getCategory().getId(), objectWithScore.getCatId());

        // Update the object but keep the same category, and check the image search instance is NOT updated
        reset(mockImageSearchService);
        object.setName("testObjectLifecycleImage_updated");
        RecognizableObject updatedObject = recognizableObjectService.update(object);
        assertEquals(updatedObject, object);
        verify(mockImageSearchService, times(0)).register(any(), any(), any(), anyString());

        // Update the object again by changing the category, and check the image search instance is updated
        object.setCategory(ObjectCategory.BOTTLE_DRINKS);
        updatedObject = recognizableObjectService.update(object);
        assertEquals(updatedObject, object);
        verify(mockImageSearchService, times(1)).register(imageData, ObjectImageType.JPEG, ObjectCategory.BOTTLE_DRINKS, uuid);

        // Delete the object
        recognizableObjectService.delete(object.getUuid());
        verify(mockImageSearchService).unregister(uuid);
    }

    @Test
    public void testFindByUuid() {
        // Try to find an object that doesn't exist
        RecognizableObject foundObject = recognizableObjectService.findByUuid(UUID.randomUUID().toString());
        assertNull(foundObject);

        // Create an object and check it can be found
        RecognizableObject object = new RecognizableObject(
                UUID.randomUUID().toString(), "testFindByUuid", ObjectCategory.OTHERS, ObjectImageType.JPEG, imageData, imageData);
        recognizableObjectService.create(object);

        foundObject = recognizableObjectService.findByUuid(object.getUuid());
        assertEquals(object.getUuid(), foundObject.getUuid());
        assertEquals(object.getName(), foundObject.getName());
        assertEquals(object.getCategory(), foundObject.getCategory());
        assertEquals(object.getImageType(), foundObject.getImageType());

        // Delete the object
        recognizableObjectService.delete(object.getUuid());

        // Check the object cannot be found anymore
        foundObject = recognizableObjectService.findByUuid(object.getUuid());
        assertNull(foundObject);
    }

    @Test
    public void testFindAll() {
        // Create 2 objects
        RecognizableObject object1 = new RecognizableObject(
                UUID.randomUUID().toString(), "testFindAll1", ObjectCategory.OTHERS, ObjectImageType.JPEG, imageData, imageData);
        RecognizableObject object2 = new RecognizableObject(
                UUID.randomUUID().toString(), "testFindAll2", ObjectCategory.BOTTLE_DRINKS, ObjectImageType.JPEG, imageData, imageData);
        recognizableObjectService.create(object1);
        recognizableObjectService.create(object2);

        // Check we can find these objects
        List<RecognizableObject> foundObjects = recognizableObjectService.findAll();
        RecognizableObject foundObject1 = foundObjects.stream()
                .filter(o -> o.getUuid().equals(object1.getUuid()))
                .findFirst()
                .orElse(null);
        assertEquals(object1, foundObject1);

        RecognizableObject foundObject2 = foundObjects.stream()
                .filter(o -> o.getUuid().equals(object2.getUuid()))
                .findFirst()
                .orElse(null);
        assertEquals(object2, foundObject2);

        // Delete the objects
        recognizableObjectService.delete(object1.getUuid());
        recognizableObjectService.delete(object2.getUuid());

        // Check we cannot find these objects
        foundObjects = recognizableObjectService.findAll();
        boolean matchingFound = foundObjects.stream()
                .anyMatch(o -> o.getUuid().equals(object1.getUuid()) || o.getUuid().equals(object2.getUuid()));
        assertFalse(matchingFound);
    }
}
