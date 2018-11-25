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

package com.alibaba.intl.imagesearch.facade.security;

import com.alibaba.intl.imagesearch.model.Configuration;
import com.alibaba.intl.imagesearch.services.ConfigurationService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Authenticate users by checking the configuration.
 *
 * @author Alibaba Cloud
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final ConfigurationService configurationService;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CustomAuthenticationProvider(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        // the username must be "admin"
        if (!"admin".equals(authentication.getName())) {
            return null;
        }

        // If there is no configuration, everybody is an administrator because the system is not installed yet
        Configuration configuration = configurationService.load();
        if (configuration == null) {
            return buildAdminToken();
        }

        // Compare the password
        if (passwordEncoder.matches(authentication.getCredentials().toString(), configuration.getPassword())) {
            return buildAdminToken();
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private Authentication buildAdminToken() {
        return new UsernamePasswordAuthenticationToken("admin", "",
                Collections.singletonList(new SimpleGrantedAuthority("ADMIN")));
    }
}
