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


import com.alibaba.intl.imagesearch.exceptions.InvalidConfigurationException;
import com.alibaba.intl.imagesearch.model.Configuration;
import com.alibaba.intl.imagesearch.services.ConfigurationService;
import com.alibaba.intl.imagesearch.services.ImageSearchService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Configuration operations behind the "/configuration" path.
 *
 * @author Alibaba Cloud
 */
@RestController
public class ConfigurationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationController.class);

    private final ConfigurationService configurationService;
    private final ImageSearchService imageSearchService;

    public ConfigurationController(ConfigurationService configurationService, ImageSearchService imageSearchService) {
        this.configurationService = configurationService;
        this.imageSearchService = imageSearchService;
    }

    /**
     * Get the configuration.
     */
    @RequestMapping(value = "/configuration", method = RequestMethod.GET)
    public ResponseEntity<Configuration> getConfiguration() {
        LOGGER.debug("Get the configuration.");

        Configuration configuration = configurationService.load();
        if (configuration == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            configuration.setPassword("");
            return new ResponseEntity<>(configuration, HttpStatus.OK);
        }
    }

    /**
     * Save the configuration.
     */
    @RequestMapping(value = "/configuration", method = RequestMethod.PUT)
    public ResponseEntity<String> saveConfiguration(@RequestBody Configuration configuration) {
        LOGGER.info("Save the configuration (configuration = {})", configuration);

        try {
            validateConfiguration(configuration);
        } catch (InvalidConfigurationException e) {
            LOGGER.warn("Invalid configuration: " + configuration, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        configurationService.save(configuration);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Check that the configuration is correct.
     *
     * @return {@link HttpStatus#OK} if the configuration is correct, {@link HttpStatus#BAD_REQUEST} if not.
     */
    @RequestMapping(value = "/configuration/check", method = RequestMethod.POST)
    public ResponseEntity<String> checkConfiguration(@RequestBody Configuration configuration) {
        try {
            validateConfiguration(configuration);
            imageSearchService.checkImageSearchConfiguration(configuration);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            LOGGER.warn("Invalid configuration: " + configuration, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void validateConfiguration(Configuration configuration) throws InvalidConfigurationException {
        if (configuration == null) {
            throw new InvalidConfigurationException("The configuration cannot be null.");
        }
        if (StringUtils.isBlank(configuration.getAccessKeyId())) {
            throw new InvalidConfigurationException("The accessKeyId is invalid.");
        }
        if (StringUtils.isBlank(configuration.getAccessKeySecret())) {
            throw new InvalidConfigurationException("The accessKeySecret is invalid.");
        }
        if (StringUtils.isBlank(configuration.getImageSearchDomain())) {
            throw new InvalidConfigurationException("The imageSearchDomain is invalid.");
        }
        if (StringUtils.isBlank(configuration.getImageSearchInstanceName())) {
            throw new InvalidConfigurationException("The imageSearchInstanceName is invalid.");
        }
//        if (StringUtils.isBlank(configuration.getImageSearchNamespace())) {
//            throw new InvalidConfigurationException("The imageSearchNamespace is invalid.");
//        }
        if (StringUtils.isBlank(configuration.getRegionId())) {
            throw new InvalidConfigurationException("The regionId is invalid.");
        }
    }
}
