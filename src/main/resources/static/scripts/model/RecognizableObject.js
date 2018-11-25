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

/**
 * Contains all information of an object.
 *
 * @param {{    uuid: string?,
 *              name: string?,
 *              category: string?,
 *              imageType: string?,
 *              imageUrl: string?,
 *              thumbnailUrl: string?,
 *              imageFile: File?,
 *              thumbnailFile: File?}?} params
 * @constructor
 *
 * @author Alibaba Cloud
 */
function RecognizableObject(params) {
    var nonNullParams = params || {};

    /**
     * @type {string}
     */
    this.uuid = nonNullParams.uuid || '';

    /**
     * @type {string}
     */
    this.name = nonNullParams.name || '';

    /**
     * @type {string}
     */
    this.category = nonNullParams.category || 'OTHERS';

    /**
     * @type {string}
     */
    this.imageType = nonNullParams.imageType || 'JPEG';

    /**
     * @type {string}
     */
    this.imageUrl = nonNullParams.imageUrl || '';

    /**
     * @type {string}
     */
    this.thumbnailUrl = nonNullParams.thumbnailUrl || '';

    /**
     * @type {?File}
     */
    this.imageFile = nonNullParams.imageFile;

    /**
     * @type {?File}
     */
    this.thumbnailFile = nonNullParams.thumbnailFile;
}