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
import com.alibaba.intl.imagesearch.model.Configuration;
import com.alibaba.intl.imagesearch.repositories.ConfigurationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test the controller that provides HTML pages.
 *
 * @author Alibaba Cloud
 */
public class PageControllerTest extends AbstractTest {

    @Autowired
    private ConfigurationRepository configurationRepository;
    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void checkRedirectionWhenConfigurationIsEmpty() throws Exception {
        // Remove the current configuration if any and check it is not possible to go to the home page and the object management page
        configurationRepository.deleteAll();

        MockHttpServletResponse response = mockMvc.perform(get("/"))
                .andExpect(status().isFound())
                .andReturn().getResponse();
        assertEquals("/admin/configuration", response.getRedirectedUrl());

        response = mockMvc.perform(get("/admin/objects"))
                .andExpect(status().isFound())
                .andReturn().getResponse();
        assertEquals("/admin/configuration", response.getRedirectedUrl());

        // Add a configuration and check the home page and the object management page are now available
        Configuration configuration = new Configuration(
                "",
                "sample-password",
                "sample-accesskey",
                "sample-access-key-secret",
                "sample-region-id",
                "sample-instance-name",
                "sample-instance-domain.com",
                "sample-instance-namespace",
                null);
        mockMvc.perform(put("/configuration")
                .content(new ObjectMapper().writeValueAsBytes(configuration))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        response = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertNull(response.getRedirectedUrl());
        assertTrue(response.getContentAsString().contains("<html"));

        response = mockMvc.perform(get("/admin/objects"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertNull(response.getRedirectedUrl());
        assertTrue(response.getContentAsString().contains("<html"));
    }
}
