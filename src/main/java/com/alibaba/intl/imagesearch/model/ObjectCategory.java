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

import java.util.Arrays;

/**
 * Categories supported by the Image Search API.
 *
 * @author Alibaba Cloud
 */
public enum ObjectCategory {

    TOPS("0"),
    DRESSES("1"),
    BOTTOMS("2"),
    BAGS("3"),
    SHOES("4"),
    ACCESSORIES("5"),
    SNACKS("6"),
    MAKEUP("7"),
    BOTTLE_DRINKS("8"),
    FURNITURE("9"),
    TOYS("20"),
    UNDERWEARS("21"),
    DIGITAL_DEVICES("22"),
    OTHERS("88888888");

    private final String id;

    ObjectCategory(String id) {
        this.id = id;
    }

    /**
     * @return Category ID supported by the Image Search API.
     */
    public String getId() {
        return id;
    }

    /**
     * Find the category that matches the given ID.
     *
     * @param id Category ID to search.
     * @return Corresponding category or null.
     */
    public static ObjectCategory findById(String id) {
        return Arrays.stream(ObjectCategory.values())
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
