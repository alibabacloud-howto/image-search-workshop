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
var objectService = {

    /**
     * Create a new object.
     *
     * @param {RecognizableObject} object Object to create.
     * @param {function(createdObject: RecognizableObject?, error: string?)} callback Function called when the object is created.
     */
    create: function (object, callback) {
        var formData = new FormData();
        formData.append('imageFile', object.imageFile);
        formData.append('thumbnailFile', object.thumbnailFile);
        var objectJson = JSON.stringify({
            uuid: object.uuid,
            name: object.name,
            category: object.category,
            imageType: object.imageType
        });
        formData.append('json', new Blob([objectJson], {type: 'application/json'}));
        $.ajax({
            url: '/objects',
            data: formData,
            cache: false,
            contentType: false,
            processData: false,
            method: 'POST',
            success: function (data) {
                callback(new RecognizableObject(data));
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.error('Unable to create the object: ' + JSON.stringify(object) + ', textStatus = ' + textStatus + ', errorThrown = ' + errorThrown);
                callback(null, 'Unable to create the object: ' + errorThrown);
            }
        });
    },

    /**
     * Update the given object.
     *
     * @param {RecognizableObject} object Object to update.
     * @param {function(updatedObject: RecognizableObject?, error: string?)} callback Function called when the object is updated.
     */
    update: function (object, callback) {
        $.ajax({
            url: '/objects/' + object.uuid,
            data: JSON.stringify({
                uuid: object.uuid,
                name: object.name,
                category: object.category,
                imageType: object.imageType
            }),
            cache: false,
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            method: 'PUT',
            success: function (data) {
                callback(new RecognizableObject(data));
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.error('Unable to update the object: ' + JSON.stringify(object) + ', textStatus = ' + textStatus + ', errorThrown = ' + errorThrown);
                callback(null, 'Unable to update the object: ' + errorThrown);
            }
        });
    },

    /**
     * Delete the object with the given UUID.
     *
     * @param {string} objectUuid
     * @param {function(error: string?)} callback
     */
    delete: function (objectUuid, callback) {
        $.ajax({
            url: '/objects/' + objectUuid,
            cache: false,
            method: 'DELETE',
            success: function () {
                callback();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.error('Unable to delete the object: ' + objectUuid + ', textStatus = ' + textStatus + ', errorThrown = ' + errorThrown);
                callback('Unable to delete the object: ' + errorThrown);
            }
        });
    },

    /**
     * Load all the objects from the server.
     *
     * @param {function(objects: Array.<RecognizableObject>?, error: String?)} callback
     */
    findAll: function (callback) {
        $.getJSON('/objects', function (objectParams) {
            var objects = _.map(objectParams, function (objectParam) {
                return new RecognizableObject(objectParam);
            });
            callback(objects);
        }).fail(function (jqXHR, textStatus, errorThrown) {
            console.error('Unable to find all the objects: textStatus = ' + textStatus + ', errorThrown = ' + errorThrown);
            callback(null, 'Unable to find all the objects: ' + errorThrown);
        });
    },

    /**
     * Find all objects that match the given image.
     *
     * @param {File} imageFile
     * @param {ObjectRegion} objectRegion
     * @param {function(objectWithScores: Array.<ObjectWithScore>?, objectRegion: ObjectRegion, rawImageSearchResponseJson: String?, error: String?)} callback
     */
    findAllBySimilarImage: function (imageFile, objectRegion, callback) {
        var formData = new FormData();
        formData.append('imageFile', imageFile);

        //If the object region is present then sent it server.
        if (objectRegion) {
            formData.append('objectRegion', new Blob([JSON.stringify(objectRegion)], {type: 'application/json'}));
        }
        $.ajax({
            url: '/objects/findAllBySimilarImage',
            data: formData,
            cache: false,
            contentType: false,
            processData: false,
            method: 'POST',
            success: function (objectSearchResponse) {
                var objectWithScores = _.map(objectSearchResponse.objectWithScores, function (objectWithScore) {
                    return new ObjectWithScore(objectWithScore);
                });
                var objectRegion = new ObjectRegion(objectSearchResponse.objectRegion);
                callback(objectWithScores, objectRegion, objectSearchResponse.rawImageSearchResponseJson);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.error('Unable to find objects by uploading a similar image: textStatus = ' + textStatus + ', errorThrown = ' + errorThrown);
                callback(null, null, 'Unable to find objects by uploading a similar image: ' + errorThrown);
            }
        });
    }
};