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
 * Controller for the administration page that allow the user to manage objects.
 *
 * @author Alibaba Cloud
 */
var adminObjectsController = {

    /**
     * @private
     * @type {Notifier}
     */
    _notifier: null,

    /**
     * @private
     * @type {ObjectMosaic}
     */
    _unfinalizedObjectMosaic: null,

    /**
     * @private
     * @type {ObjectMosaic}
     */
    _existingObjectMosaic: null,

    /**
     * Method called when the document is ready.
     */
    onDocumentReady: function () {
        var self = this;

        // Initialize the upload zone
        var uploadZone = new UploadZone($('#new_object_upload_zone'), {
            message: 'Upload or drop images to create new objects.',
            acceptedMimeTypes: ['image/jpeg', 'image/png'],
            multiple: true
        });
        uploadZone.setOnFilesAddedListener(function (files) {
            self._onImageFilesAdded(files);
        });

        // Initialize the object mosaic for un-finalized objects
        this._unfinalizedObjectMosaic = new ObjectMosaic($('#unfinalized_object_mosaic'), {
            editable: true,
            alwaysShowAsEditable: true,
            showScore: false
        });
        this._unfinalizedObjectMosaic.setObjectSavedHandler(function (object, doneCallback) {
            objectService.create(object, function (createdObject, error) {
                if (error) {
                    self._notifier.showNotification(error, Notifier.Level.DANGER);
                    doneCallback();
                    return;
                }
                doneCallback();

                // Remove the object from this mosaic and reload the one for existing objects
                self._unfinalizedObjectMosaic.removeObjects([createdObject.uuid]);
                self._loadExistingObjects();

                // Hide the finalization message when there are no remaining objects
                if (self._unfinalizedObjectMosaic.getAllObjects().length === 0) {
                    $('#registration_finalization_header').hide();
                }
            });
        });
        this._unfinalizedObjectMosaic.setObjectDeletedHandler(function (object, doneCallback) {
            doneCallback();

            // Hide the finalization message when there are no remaining objects
            if (self._unfinalizedObjectMosaic.getAllObjects().length === 0) {
                $('#registration_finalization_header').hide();
            }
        });

        // Initialize the object mosaic for existing objects
        this._existingObjectMosaic = new ObjectMosaic($('#existing_object_mosaic'), {
            editable: true,
            alwaysShowAsEditable: false,
            showScore: false
        });
        this._existingObjectMosaic.setObjectSavedHandler(function (object, doneCallback) {
            objectService.update(object, function (updatedObject, error) {
                if (error) {
                    self._notifier.showNotification(error, Notifier.Level.DANGER);
                } else {
                    self._loadExistingObjects(); // Reload the mosaic
                }
                doneCallback();
            });
        });
        this._existingObjectMosaic.setObjectDeletedHandler(function (object, doneCallback) {
            objectService.delete(object.uuid, function (error) {
                if (error) {
                    self._notifier.showNotification(error, Notifier.Level.DANGER);
                }
                doneCallback();
            });
        });
        this._loadExistingObjects();

        // Initialize the notifier
        this._notifier = new Notifier($('#notifications'));

        // Show a confirmation modal when the user click on the 'Add all' button
        $('#registration_finalization_header_add_all').on('click', function () {
            var validObjects = self._unfinalizedObjectMosaic.getAllValidObjects();
            $('#add_all_modal .modal-body > div').hide();
            if (validObjects.length === 0) {
                $('#add_all_modal_no_valid_object').show();
                $('#add_all_modal_confirm').prop('disabled', true);
            } else {
                $('#add_all_modal_nb_addable_objects').text(validObjects.length);
                $('#add_all_modal_confirmation_message').show();
                $('#add_all_modal_confirm').prop('disabled', false);
            }
            $('#add_all_modal button[data-dismiss="modal"]').prop('disabled', false);
            $('#add_all_modal_ok').hide();
            $('#add_all_modal_cancel').show();
            $('#add_all_modal_confirm').show();
            $('#add_all_modal').modal();
        });

        // Start to add objects one by one when the user click on the confirmation button
        $('#add_all_modal_confirm').on('click', function () {
            $('#add_all_modal button').prop('disabled', true);
            $('#add_all_modal_confirmation_message').hide();
            var $processing = $('#add_all_modal_processing');
            $processing.show();

            var validObjects = self._unfinalizedObjectMosaic.getAllValidObjects();
            var $progressBar = $('#add_all_modal_progress_bar');

            self._unfinalizedObjectMosaic.saveObjects(validObjects, function (currentObject, totalObjects, isComplete) {
                if (isComplete) {
                    $processing.hide();
                    $('#add_all_modal_done').show();
                    $('#add_all_modal_cancel').hide();
                    $('#add_all_modal_confirm').hide();
                    $('#add_all_modal_ok').show();
                    $('#add_all_modal button[data-dismiss="modal"]').prop('disabled', false);
                } else {
                    var percentage = Math.round(currentObject * 100 / totalObjects);
                    $progressBar.css('width', percentage + '%');
                    $progressBar.prop('aria-valuenow', percentage);
                    $progressBar.text(currentObject + '/' + totalObjects + ' (' + percentage + '%)');
                }
            });
        });
    },

    /**
     * Load all existing objects and display them.
     *
     * @private
     */
    _loadExistingObjects: function () {
        var self = this;

        this._existingObjectMosaic.setLoadingOverlayVisible(true);
        objectService.findAll(function (objects, error) {
            self._existingObjectMosaic.setLoadingOverlayVisible(false);

            if (error) {
                self._notifier.showNotification(error, Notifier.Level.DANGER);
            } else {
                // Remove all existing objects
                var uuids = _.map(self._existingObjectMosaic.getAllObjects(), 'uuid');
                self._existingObjectMosaic.removeObjects(uuids);

                // Add the loaded objects
                self._existingObjectMosaic.addObjects(objects);
            }
        });
    },

    /**
     * Method called when image files have been added with drag and drop or with file selection.
     *
     * @private
     * @param {Array.<File>} imageFiles
     */
    _onImageFilesAdded: function (imageFiles) {
        var self = this;

        this._createThumbnailFiles(imageFiles, function (thumbnailFiles) {
            self._convertAndResizeImageFiles(imageFiles, function (transformedImageFiles) {
                // Convert each image into objects
                var objects = _.map(transformedImageFiles, function (imageFile, imageFileIndex) {
                    var nameWithoutExtension = imageFile.name;
                    var indexOfExtension = nameWithoutExtension.lastIndexOf('.');
                    if (indexOfExtension > 0) {
                        nameWithoutExtension = nameWithoutExtension.substr(0, indexOfExtension);
                    }
                    return new RecognizableObject({
                        uuid: self._generateUuid(),
                        name: nameWithoutExtension,
                        imageType: 'JPEG',
                        imageFile: imageFile,
                        thumbnailFile: thumbnailFiles[imageFileIndex]
                    });
                });

                // Display the objects in a mosaic
                $('#registration_finalization_header').css('display', 'flex');
                self._unfinalizedObjectMosaic.addObjects(objects);
            });
        });
    },

    /**
     * Create thumbnails for each images.
     *
     * @param {Array.<File>} imageFiles
     * @param {function(thumbnailFiles: Array.<File>)} callback
     */
    _createThumbnailFiles: function (imageFiles, callback) {
        if (imageFiles.length === 0) {
            return callback(imageFiles);
        }

        var processedFiles = [];

        /**
         * @param {File} imageFile
         * @param {number} imageFileIndex
         */
        function processImageFile(imageFile, imageFileIndex) {
            imageService.createThumbnail(imageFile, function (thumbnailBlob) {
                var processedFile = new File([thumbnailBlob], imageFile.name);
                processedFiles.push(processedFile);

                // Continue with the next file or exit if applicable
                var newIndex = imageFileIndex + 1;
                if (newIndex < imageFiles.length) {
                    processImageFile(imageFiles[newIndex], newIndex);
                } else {
                    callback(processedFiles);
                }
            });
        }

        processImageFile(imageFiles[0], 0);
    },

    /**
     * Process the given image files like this:
     * <ol>
     *     <li>Convert PNG files to JPEG if necessary (do nothing if the file is already a JPEG).</li>
     *     <li>Resize the images if they are too small or too big.</li>
     * </ol>
     *
     * @param {Array.<File>} imageFiles
     * @param {function(transformedImageFiles: Array.<File>)} callback
     * @private
     */
    _convertAndResizeImageFiles: function (imageFiles, callback) {
        if (imageFiles.length === 0) {
            return callback(imageFiles);
        }

        var processedFiles = [];

        /**
         * @param {File} imageFile
         * @param {number} imageFileIndex
         */
        function processImageFile(imageFile, imageFileIndex) {
            imageService.convertToJpeg(imageFile, function (jpegImageBlob) {
                imageService.scaleUpImageWithTooSmallResolution(jpegImageBlob, function (largeEnoughImageBlob) {
                    imageService.scaleDownImageWithTooBigResolution(largeEnoughImageBlob, function (smallEnoughResolutionImageBlob) {
                        imageService.scaleDownImageWithTooBigSize(smallEnoughResolutionImageBlob, function (smallEnoughImageBlob) {
                            var processedFile = new File([smallEnoughImageBlob], imageFile.name);
                            processedFiles.push(processedFile);

                            // Continue with the next file or exit if applicable
                            var newIndex = imageFileIndex + 1;
                            if (newIndex < imageFiles.length) {
                                processImageFile(imageFiles[newIndex], newIndex);
                            } else {
                                callback(processedFiles);
                            }
                        });
                    });
                });
            });
        }

        processImageFile(imageFiles[0], 0);
    },

    /**
     * Generate a random-based UUID v4.
     * Thanks to https://stackoverflow.com/a/2117523
     *
     * @private
     * @returns {string}
     */
    _generateUuid: function () {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }
};

$(function () {
    adminObjectsController.onDocumentReady();
});