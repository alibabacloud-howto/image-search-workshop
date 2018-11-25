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

package com.alibaba.intl.imagesearch.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Application configuration parameters.
 *
 * @author Alibaba Cloud
 */
@Entity
@Table(name = "CONFIGURATION")
public class Configuration {
    @Id
    private String id;
    private String password;
    private String accessKeyId;
    private String accessKeySecret;
    private String regionId;
    private String imageSearchInstanceName;
    private String imageSearchDomain;
    private String imageSearchNamespace;
    private String ossBaseUrl;

    public Configuration() {
    }

    public Configuration(String id, String password, String accessKeyId, String accessKeySecret, String regionId, String imageSearchInstanceName, String imageSearchDomain, String imageSearchNamespace, String ossBaseUrl) {
        this.id = id;
        this.password = password;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.regionId = regionId;
        this.imageSearchInstanceName = imageSearchInstanceName;
        this.imageSearchDomain = imageSearchDomain;
        this.imageSearchNamespace = imageSearchNamespace;
        this.ossBaseUrl = ossBaseUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getImageSearchInstanceName() {
        return imageSearchInstanceName;
    }

    public void setImageSearchInstanceName(String imageSearchInstanceName) {
        this.imageSearchInstanceName = imageSearchInstanceName;
    }

    public String getImageSearchDomain() {
        return imageSearchDomain;
    }

    public void setImageSearchDomain(String imageSearchDomain) {
        this.imageSearchDomain = imageSearchDomain;
    }

    public String getImageSearchNamespace() {
        return imageSearchNamespace;
    }

    public void setImageSearchNamespace(String imageSearchNamespace) {
        this.imageSearchNamespace = imageSearchNamespace;
    }

    public String getOssBaseUrl() {
        return ossBaseUrl;
    }

    public void setOssBaseUrl(String ossBaseUrl) {
        this.ossBaseUrl = ossBaseUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Configuration)) {
            return false;
        }
        Configuration that = (Configuration) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", accessKeyId='" + accessKeyId + '\'' +
                ", accessKeySecret='" + accessKeySecret + '\'' +
                ", regionId='" + regionId + '\'' +
                ", imageSearchInstanceName='" + imageSearchInstanceName + '\'' +
                ", imageSearchDomain='" + imageSearchDomain + '\'' +
                ", imageSearchNamespace='" + imageSearchNamespace + '\'' +
                ", ossBaseUrl='" + ossBaseUrl + '\'' +
                '}';
    }
}



