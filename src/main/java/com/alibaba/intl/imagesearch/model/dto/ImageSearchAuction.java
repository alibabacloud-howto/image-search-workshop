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

import java.util.Map;

/**
 * Result of a search by similar image.
 *
 * @author Alibaba Cloud
 */
public class ImageSearchAuction {

    private String itemId;
    private String catId;
    private String picName;
    private ImageStoreType imageStoreType;

    /**
     * Value returned by the Image Search API.
     * Its absolute value is not meaningful in itself, but makes senses when compared to other results.
     */
    private double similarityScore;

    private Map<String, String> customContent;


    public ImageSearchAuction() {
    }

    public ImageSearchAuction(String itemId, String catId, String picName, ImageStoreType imageStoreType, double similarityScore, Map<String, String> customContent) {
        this.itemId = itemId;
        this.catId = catId;
        this.picName = picName;
        this.imageStoreType = imageStoreType;
        this.similarityScore = similarityScore;
        this.customContent = customContent;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }

    public ImageStoreType getImageStoreType() {
        return imageStoreType;
    }

    public void setImageStoreType(ImageStoreType imageStoreType) {
        this.imageStoreType = imageStoreType;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public Map<String, String> getCustomContent() {
        return customContent;
    }

    public void setCustomContent(Map<String, String> customContent) {
        this.customContent = customContent;
    }

    @Override
    public String toString() {
        return "ImageSearchAuction{" +
                "itemId='" + itemId + '\'' +
                ", catId='" + catId + '\'' +
                ", picName='" + picName + '\'' +
                ", imageStoreType=" + imageStoreType +
                ", similarityScore=" + similarityScore +
                ", customContent=" + customContent +
                '}';
    }
}
