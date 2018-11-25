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

import com.alibaba.intl.imagesearch.model.Configuration;
import com.alibaba.intl.imagesearch.repositories.ConfigurationRepository;
import com.alibaba.intl.imagesearch.services.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link ConfigurationService}.
 *
 * @author Alibaba Cloud
 */
@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private static final String CONFIGURATION_ID = "MAIN_CONFIGURATION";

    private final ConfigurationRepository configurationRepository;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ConfigurationServiceImpl(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Override
    public Configuration save(Configuration configuration) {
        configuration.setId(CONFIGURATION_ID);

        if (StringUtils.isBlank(configuration.getPassword())) {
            Configuration existingConfiguration = load();
            configuration.setPassword(existingConfiguration.getPassword());
        } else {
            configuration.setPassword(passwordEncoder.encode(configuration.getPassword()));
        }

        return configurationRepository.save(configuration);
    }

    @Override
    public Configuration load() {
        return configurationRepository.findById(CONFIGURATION_ID).orElse(null);
    }
}
