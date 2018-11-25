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
 * Handle communication with the server in order to send or receive objects.
 *
 * @author Alibaba Cloud
 */
var configurationService = {

    /**
     * Download the configuration.
     *
     * @param {string} password
     * @param {function(configuration: Configuration?, errorCode: Number?, errorMessage: String?)} callback
     */
    getConfiguration: function (password, callback) {
        $.ajax({
            url: '/configuration',
            cache: false,
            method: 'GET',
            headers: {
                'Authorization': 'Basic ' + btoa('admin:' + password)
            },
            success: function (data) {
                callback(new Configuration(data));
            },
            error: function (jqXHR, textStatus, errorThrown) {
                callback(null, jqXHR.status, 'Unable to load the configuration: textStatus = ' + textStatus + ', errorThrow = ' + errorThrown);
            }
        });
    },
    /**
     * Upload the configuration to the server.
     *
     * @param {string} password
     * @param {Configuration} configuration
     * @param {function(error: String?)} callback
     */
    saveConfiguration: function (password, configuration, callback) {
        $.ajax({
            url: '/configuration',
            data: JSON.stringify({
                password: configuration.password,
                accessKeyId: configuration.accessKeyId,
                accessKeySecret: configuration.accessKeySecret,
                regionId: configuration.regionId,
                imageSearchInstanceName: configuration.imageSearchInstanceName,
                imageSearchDomain: configuration.imageSearchDomain,
                imageSearchNamespace: configuration.imageSearchNamespace,
                ossBaseUrl: configuration.ossBaseUrl
            }),
            cache: false,
            contentType: 'application/json; charset=utf-8',
            method: 'PUT',
            headers: {
                'Authorization': 'Basic ' + btoa('admin:' + password)
            },
            success: function () {
                callback();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.error('Unable to save the configuration: ' + JSON.stringify(configuration) + ', textStatus = ' + textStatus + ', errorThrown = ' + errorThrown);
                callback('Unable to save the configuration: ' + jqXHR.responseText);
            }
        });
    },

    /**
     * Ask the server to check whether the configuration is valid or not.
     *
     * @param {string} password
     * @param {Configuration} configuration
     * @param {function(error: String?)} callback
     */
    checkConfiguration: function (password, configuration, callback) {
        $.ajax({
            url: '/configuration/check',
            data: JSON.stringify({
                accessKeyId: configuration.accessKeyId,
                accessKeySecret: configuration.accessKeySecret,
                regionId: configuration.regionId,
                imageSearchInstanceName: configuration.imageSearchInstanceName,
                imageSearchDomain: configuration.imageSearchDomain,
                imageSearchNamespace: configuration.imageSearchNamespace,
                ossBaseUrl: configuration.ossBaseUrl
            }),
            cache: false,
            contentType: 'application/json; charset=utf-8',
            method: 'POST',
            headers: {
                'Authorization': 'Basic ' + btoa('admin:' + password)
            },
            success: function () {
                callback();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log('Invalid configuration: ' + JSON.stringify(configuration) + ', textStatus = ' + textStatus + ', errorThrown = ' + errorThrown);
                callback('Invalid configuration: ' + jqXHR.responseText);
            }
        });
    }
};