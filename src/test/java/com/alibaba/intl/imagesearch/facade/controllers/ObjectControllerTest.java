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

package com.alibaba.intl.imagesearch.facade.controllers;

import com.alibaba.intl.imagesearch.AbstractTest;
import com.alibaba.intl.imagesearch.facade.dto.ObjectDTO;
import com.alibaba.intl.imagesearch.facade.dto.ObjectSearchResponseDTO;
import com.alibaba.intl.imagesearch.model.ObjectCategory;
import com.alibaba.intl.imagesearch.model.ObjectImageType;
import com.alibaba.intl.imagesearch.model.dto.ImageRegion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test the REST API behind "/objects".
 *
 * @author Alibaba Cloud
 */
@ActiveProfiles("dummy")
public class ObjectControllerTest extends AbstractTest {

    @Value("classpath:samples/kettle.jpg")
    private Resource sampleImageResource;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testCreate() throws Exception {
        // Create the object
        byte[] sampleImageData = loadResource(sampleImageResource);
        String objectUuid = UUID.randomUUID().toString();
        ObjectDTO objectDto = createObject(new ObjectDTO(
                objectUuid,
                "test-create",
                ObjectCategory.BOTTLE_DRINKS,
                ObjectImageType.JPEG,
                "",
                ""), sampleImageData, sampleImageData);

        // Check the response
        assertEquals(objectUuid, objectDto.getUuid());
        assertEquals("test-create", objectDto.getName());
        assertEquals(ObjectCategory.BOTTLE_DRINKS, objectDto.getCategory());
        assertEquals(ObjectImageType.JPEG, objectDto.getImageType());
        assertEquals("/objects/" + objectUuid + "/image", objectDto.getImageUrl());
        assertEquals("/objects/" + objectUuid + "/thumbnail", objectDto.getThumbnailUrl());

        // Check the object as been created by searching it by its UUID
        MockHttpServletResponse response = mockMvc.perform(get("/objects/" + objectUuid))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        objectDto = objectMapper.readValue(response.getContentAsString(), ObjectDTO.class);
        assertNotNull(objectDto);
    }

    @Test
    public void testUpdate() throws Exception {
        // Create the object
        byte[] sampleImageData = loadResource(sampleImageResource);
        String objectUuid = UUID.randomUUID().toString();
        ObjectDTO objectDto = createObject(new ObjectDTO(
                objectUuid,
                "test-update-1",
                ObjectCategory.BOTTLE_DRINKS,
                ObjectImageType.JPEG,
                "",
                ""), sampleImageData, sampleImageData);
        assertNotNull(objectDto);

        // Update the object (change name and category)
        objectDto.setName("test-update-2");
        objectDto.setCategory(ObjectCategory.OTHERS);
        MockHttpServletResponse response = mockMvc.perform(put("/objects/" + objectUuid)
                .content(objectMapper.writeValueAsBytes(objectDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        objectDto = objectMapper.readValue(response.getContentAsString(), ObjectDTO.class);
        assertEquals(objectUuid, objectDto.getUuid());
        assertEquals("test-update-2", objectDto.getName());
        assertEquals(ObjectCategory.OTHERS, objectDto.getCategory());

        // Check the object as been updated by searching it by its UUID
        response = mockMvc.perform(get("/objects/" + objectUuid))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        objectDto = objectMapper.readValue(response.getContentAsString(), ObjectDTO.class);
        assertNotNull(objectDto);
        assertEquals(objectUuid, objectDto.getUuid());
        assertEquals("test-update-2", objectDto.getName());
        assertEquals(ObjectCategory.OTHERS, objectDto.getCategory());
    }

    @Test
    public void testDelete() throws Exception {
        // Create the object
        byte[] sampleImageData = loadResource(sampleImageResource);
        String objectUuid = UUID.randomUUID().toString();
        ObjectDTO objectDto = createObject(new ObjectDTO(
                objectUuid,
                "test-delete-1",
                ObjectCategory.BOTTLE_DRINKS,
                ObjectImageType.JPEG,
                "",
                ""), sampleImageData, sampleImageData);
        assertNotNull(objectDto);

        // Delete the object
        MockHttpServletResponse response = mockMvc.perform(delete("/objects/" + objectUuid)
                .content(objectUuid)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        String deleteFlag = response.getContentAsString();
        assertEquals("\"OK\"", deleteFlag);

        // Check the object as been deleted
        response = mockMvc.perform(get("/objects/" + objectUuid))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
        assertEquals("", response.getContentAsString());
    }

    @Test
    public void testFindAll() throws Exception {
        // Create 2 objects
        byte[] sampleImageData = loadResource(sampleImageResource);
        String objectUuid = UUID.randomUUID().toString();
        ObjectDTO objectDto = createObject(new ObjectDTO(
                objectUuid,
                "test-create-1",
                ObjectCategory.BOTTLE_DRINKS,
                ObjectImageType.JPEG,
                "",
                ""), sampleImageData, sampleImageData);
        assertNotNull(objectDto);
        byte[] sampleImageData2 = loadResource(sampleImageResource);
        String objectUuid2 = UUID.randomUUID().toString();
        ObjectDTO objectDto2 = createObject(new ObjectDTO(
                objectUuid2,
                "test-create-2",
                ObjectCategory.BOTTLE_DRINKS,
                ObjectImageType.JPEG,
                "",
                ""), sampleImageData2, sampleImageData2);
        assertNotNull(objectDto2);

        // Try to find them
        MockHttpServletResponse response = mockMvc.perform(get("/objects")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        ObjectDTO[] foundObjects = objectMapper.readValue(response.getContentAsString(), ObjectDTO[].class);
        assertTrue(Arrays.stream(foundObjects).anyMatch(o -> o.getUuid().equals(objectUuid)));
        assertTrue(Arrays.stream(foundObjects).anyMatch(o -> o.getUuid().equals(objectUuid2)));
    }

    @Test
    public void testFindAllBySimilarImage() throws Exception {
        // Create 2 SimilarImage objects
        byte[] sampleImageData = loadResource(sampleImageResource);
        String objectUuid = UUID.randomUUID().toString();
        ObjectDTO objectDto = createObject(new ObjectDTO(
                objectUuid,
                "test-create-1",
                ObjectCategory.BOTTLE_DRINKS,
                ObjectImageType.JPEG,
                "",
                ""), sampleImageData, sampleImageData);
        assertNotNull(objectDto);

        byte[] sampleImageData2 = loadResource(sampleImageResource);
        String objectUuid2 = UUID.randomUUID().toString();
        ObjectDTO objectDto2 = createObject(new ObjectDTO(
                objectUuid2,
                "test-create-2",
                ObjectCategory.BOTTLE_DRINKS,
                ObjectImageType.PNG,
                "",
                ""), sampleImageData2, sampleImageData2);
        assertNotNull(objectDto2);

        ImageRegion region = new ImageRegion(100, 100, 100, 100);
        // Try to find all Similar Image
        MockHttpServletResponse response = mockMvc.perform(multipart("/objects/findAllBySimilarImage")
                .file(new MockMultipartFile("imageFile", "kettle.jpg", "image/jpeg", sampleImageData)).param("objectRegion", objectMapper.writeValueAsString(region)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        ObjectSearchResponseDTO objectSearchResponse = objectMapper.readValue(response.getContentAsString(), ObjectSearchResponseDTO.class);
        assertTrue(objectSearchResponse.getObjectWithScores().stream().anyMatch(ows -> ows.getObject().getUuid().equals(objectUuid)));
        assertTrue(objectSearchResponse.getObjectWithScores().stream().anyMatch(ows -> ows.getObject().getUuid().equals(objectUuid2)));
    }

    private byte[] loadResource(Resource resource) throws IOException {
        try (InputStream inputStream = sampleImageResource.getInputStream()) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private ObjectDTO createObject(ObjectDTO objectDto, byte[] imageData, byte[] thumbnailData) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(multipart("/objects")
                .file(new MockMultipartFile("imageFile", "kettle.jpg", "image/jpeg", imageData))
                .file(new MockMultipartFile("thumbnailFile", "kettle.jpg", "image/jpeg", thumbnailData))
                .file(new MockMultipartFile("json", "", "application/json",
                        objectMapper.writeValueAsBytes(objectDto))))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        return objectMapper.readValue(response.getContentAsString(), ObjectDTO.class);
    }
}
