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
 * Contains all information of a configuration.
 *
 * @param {{
 *     password: String?,
 *     accessKeyId: String?,
 *     accessKeySecret: String?,
 *     regionId: String?,
 *     imageSearchInstanceName: String?,
 *     imageSearchDomain: String?,
 *     imageSearchNamespace: String?}?} params
 *     ossBaseUrl: String?}?} params
 * @constructor
 *
 * @author Alibaba Cloud
 */
function Configuration(params) {
    var nonNullParams = params || {};
    /**
     * @type {string}
     */
    this.password = nonNullParams.password || '';
    /**
     * @type {string}
     */
    this.accessKeyId = nonNullParams.accessKeyId || '';
    /**
     * @type {string}
     */
    this.accessKeySecret = nonNullParams.accessKeySecret || '';
    /**
     * @type {string}
     */
    this.regionId = nonNullParams.regionId || '';
    /**
     * @type {string}
     */
    this.imageSearchInstanceName = nonNullParams.imageSearchInstanceName || '';
    /**
     * @type {string}
     */
    this.imageSearchDomain = nonNullParams.imageSearchDomain || '';
    /**
     * @type {string}
     */
    this.imageSearchNamespace = nonNullParams.imageSearchNamespace || '';
    /**
     * @type {string}
     */
    this.ossBaseUrl = nonNullParams.ossBaseUrl || '';
}