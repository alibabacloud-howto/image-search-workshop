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

package com.alibaba.intl.imagesearch.model.dto;

import java.util.List;

/**
 * Result of a search by similar image.
 *
 * @author Alibaba Cloud
 */
public class ImageSearchResponse {

    private List<ImageSearchAuction> imageSearchAuctions;
    private String rawImageSearchResponseJson;
    private ImageRegion objectRegion;

    public ImageSearchResponse() {
    }

    public ImageSearchResponse(List<ImageSearchAuction> imageSearchAuctions, String rawImageSearchResponseJson, ImageRegion objectRegion) {
        this.imageSearchAuctions = imageSearchAuctions;
        this.rawImageSearchResponseJson = rawImageSearchResponseJson;
        this.objectRegion = objectRegion;
    }

    public List<ImageSearchAuction> getImageSearchAuctions() {
        return imageSearchAuctions;
    }

    public void setImageSearchAuctions(List<ImageSearchAuction> imageSearchAuctions) {
        this.imageSearchAuctions = imageSearchAuctions;
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
        return "ImageSearchResponse{" +
                "imageSearchAuctions=" + imageSearchAuctions +
                ", rawImageSearchResponseJson='" + rawImageSearchResponseJson + '\'' +
                ", objectRegion=" + objectRegion +
                '}';
    }
}
