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

/**
 * Result of a search by similar image.
 *
 * @author Alibaba Cloud
 */
public class ObjectWithScoreDTO {

    private ObjectDTO object;
    /**
     * Value returned by the Image Search API.
     * Its absolute value is not meaningful in itself, but makes senses when compared to other results.
     */
    private double similarityScore;

    public ObjectWithScoreDTO() {
    }

    public ObjectWithScoreDTO(ObjectDTO object, double similarityScore) {
        this.object = object;
        this.similarityScore = similarityScore;
    }

    public ObjectDTO getObject() {
        return object;
    }

    public void setObject(ObjectDTO object) {
        this.object = object;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    @Override
    public String toString() {
        return "ObjectWithScoreDTO{" +
                "object=" + object +
                ", similarityScore=" + similarityScore +
                '}';
    }
}
