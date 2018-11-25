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

import com.alibaba.intl.imagesearch.model.Configuration;
import com.alibaba.intl.imagesearch.services.ConfigurationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Serves HTML pages.
 *
 * @author Alibaba Cloud
 */
@Controller
public class PageController {

    private final ConfigurationService configurationService;

    public PageController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @RequestMapping("/")
    public String index() {
        Configuration configuration = configurationService.load();
        return configuration == null ? "redirect:/admin/configuration" : "index";
    }

    @RequestMapping("/admin/objects")
    public String adminObjects() {
        Configuration configuration = configurationService.load();
        return configuration == null ? "redirect:/admin/configuration" : "admin_objects";
    }

    @RequestMapping("/admin/configuration")
    public String adminConfiguration() {
        return "admin_configuration";
    }
}
