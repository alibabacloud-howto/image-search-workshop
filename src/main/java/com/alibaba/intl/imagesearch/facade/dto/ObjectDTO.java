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

package com.alibaba.intl.imagesearch.facade.dto;

import com.alibaba.intl.imagesearch.model.ObjectCategory;
import com.alibaba.intl.imagesearch.model.ObjectImageType;

/**
 * @author Alibaba Cloud
 */
public class ObjectDTO {

    private String uuid;
    private String name;
    private ObjectCategory category;
    private ObjectImageType imageType;
    private String imageUrl;
    private String thumbnailUrl;

    public ObjectDTO() {
    }

    public ObjectDTO(String uuid, String name, ObjectCategory category, ObjectImageType imageType, String imageUrl, String thumbnailUrl) {
        this.uuid = uuid;
        this.name = name;
        this.category = category;
        this.imageType = imageType;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObjectCategory getCategory() {
        return category;
    }

    public void setCategory(ObjectCategory category) {
        this.category = category;
    }

    public ObjectImageType getImageType() {
        return imageType;
    }

    public void setImageType(ObjectImageType imageType) {
        this.imageType = imageType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public String toString() {
        return "ObjectDTO{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", imageType=" + imageType +
                ", imageUrl='" + imageUrl + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                '}';
    }
}
