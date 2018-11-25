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
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.Objects;

/**
 * Object linked to an image.
 *
 * @author Alibaba Cloud
 */
@Entity
@Table(name = "RECOGNIZABLE_OBJECT")
public class RecognizableObject {

    @Id
    private String uuid;
    private String name;
    private ObjectCategory category;
    private ObjectImageType imageType;
    @Lob
    private byte[] imageData;
    @Lob
    private byte[] thumbnailData;

    public RecognizableObject(String uuid, String name, ObjectCategory category, ObjectImageType imageType, byte[] imageData, byte[] thumbnailData) {
        this.uuid = uuid;
        this.name = name;
        this.category = category;
        this.imageType = imageType;
        this.imageData = imageData;
        this.thumbnailData = thumbnailData;
    }

    public RecognizableObject() {
    }

    public RecognizableObject(String uuid, String name, ObjectCategory category, ObjectImageType imageType) {
        this(uuid, name, category, imageType, null, null);
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

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public byte[] getThumbnailData() {
        return thumbnailData;
    }

    public void setThumbnailData(byte[] thumbnailData) {
        this.thumbnailData = thumbnailData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecognizableObject that = (RecognizableObject) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "RecognizableObject{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", imageType=" + imageType +
                ", imageData=" + Arrays.toString(imageData) +
                ", thumbnailData=" + Arrays.toString(thumbnailData) +
                '}';
    }
}
