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
import com.alibaba.intl.imagesearch.exceptions.InvalidConfigurationException;
import com.alibaba.intl.imagesearch.model.Configuration;
import com.alibaba.intl.imagesearch.services.ImageSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test the REST API behind "/configuration".
 *
 * @author Alibaba Cloud
 */
public class ConfigurationControllerTest extends AbstractTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private ConfigurationController configurationController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ImageSearchService originalImageSearchService = null;
    private ImageSearchService mockImageSearchService = mock(ImageSearchService.class);

    @Before
    public void setMockImageSearchService() {
        originalImageSearchService = (ImageSearchService) ReflectionTestUtils.getField(configurationController, "imageSearchService");
        ReflectionTestUtils.setField(configurationController, "imageSearchService", mockImageSearchService);
    }

    @After
    public void restoreImageSearchService() {
        ReflectionTestUtils.setField(configurationController, "imageSearchService", originalImageSearchService);
    }

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testSaveAndFindConfiguration() throws Exception {
        // Save a new configuration
        Configuration configuration = buildConfiguration();
        mockMvc.perform(put("/configuration")
                .content(objectMapper.writeValueAsBytes(configuration))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Load the configuration and check it worked
        String configurationJson = mockMvc.perform(get("/configuration"))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        Configuration loadedConfiguration = objectMapper.readValue(configurationJson, Configuration.class);
        assertEquals("sample-accesskey", loadedConfiguration.getAccessKeyId());
        assertEquals("sample-access-key-secret", loadedConfiguration.getAccessKeySecret());
        assertEquals("sample-region-id", loadedConfiguration.getRegionId());
        assertEquals("sample-instance-name", loadedConfiguration.getImageSearchInstanceName());
        assertEquals("sample-instance-domain.com", loadedConfiguration.getImageSearchDomain());
        assertEquals("sample-instance-namespace", loadedConfiguration.getImageSearchNamespace());
    }

    @Test
    public void testCheckConfiguration() throws Exception {
        // Check when the configuration is correct
        Configuration configuration = buildConfiguration();
        mockMvc.perform(post("/configuration/check")
                .content(objectMapper.writeValueAsBytes(configuration))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
        verify(mockImageSearchService).checkImageSearchConfiguration(configuration);

        // Check when the configuration is invalid
        doThrow(new InvalidConfigurationException("Sample error")).when(mockImageSearchService).checkImageSearchConfiguration(any());
        String response = mockMvc.perform(post("/configuration/check")
                .content(objectMapper.writeValueAsBytes(configuration))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals("Sample error", response);
    }

    private Configuration buildConfiguration() {
        return new Configuration(
                "",
                "sample-password",
                "sample-accesskey",
                "sample-access-key-secret",
                "sample-region-id",
                "sample-instance-name",
                "sample-instance-domain.com",
                "sample-instance-namespace",
                null);
    }
}
