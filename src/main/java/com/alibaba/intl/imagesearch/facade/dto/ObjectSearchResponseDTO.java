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

import com.alibaba.intl.imagesearch.model.dto.ImageRegion;

import java.util.List;

/**
 * Result of a search by similar image.
 *
 * @author Alibaba Cloud
 */
public class ObjectSearchResponseDTO {

    private List<ObjectWithScoreDTO> objectWithScores;
    private String rawImageSearchResponseJson;
    private ImageRegion objectRegion;

    public ObjectSearchResponseDTO() {
    }

    public ObjectSearchResponseDTO(List<ObjectWithScoreDTO> objectWithScores, String rawImageSearchResponseJson, ImageRegion objectRegion) {
        this.objectWithScores = objectWithScores;
        this.rawImageSearchResponseJson = rawImageSearchResponseJson;
        this.objectRegion = objectRegion;
    }

    public List<ObjectWithScoreDTO> getObjectWithScores() {
        return objectWithScores;
    }

    public void setObjectWithScores(List<ObjectWithScoreDTO> objectWithScores) {
        this.objectWithScores = objectWithScores;
    }

    public String getRawImageSearchResponseJson() {
        return rawImageSearchResponseJson;
    }

    public void setRawImageSearchResponseJson(String rawImageSearchResponseJson) {
        this.rawImageSearchResponseJson = rawImageSearchResponseJson;
    }

    public ImageRegion getObjectRegion() {
        return objectRegion;
    }

    public void setObjectRegion(ImageRegion objectRegion) {
        this.objectRegion = objectRegion;
    }

    @Override
    public String toString() {
        return "ObjectSearchResponseDTO{" +
                "objectWithScores=" + objectWithScores +
                ", rawImageSearchResponseJson='" + rawImageSearchResponseJson + '\'' +
                ", objectRegion=" + objectRegion +
                '}';
    }
}
