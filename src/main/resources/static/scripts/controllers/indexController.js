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
 * Controller for the index page that allows the user to find objects by uploading a similar image.
 *
 * @author Alibaba Cloud
 */
var indexController = {
    /**
     * @private
     * @type {Notifier}
     */
    _notifier: null,

    /**
     * @private
     * @type {UploadZone}
     */
    _uploadZone: null,

    /**
     * @private
     * @type {ObjectMosaic}
     */
    _foundObjectMosaic: null,

    /**
     * @private
     * @type {boolean}
     */
    _resultsDisplayed: false,

    /**
     * @private
     * @type {string}
     */
    _rawImageSearchResponseJson: '',

    /**
     * @private
     * @type {ObjectRegion}
     */
    _objectRegion: new ObjectRegion({x: 0, y: 0, width: 0, height: 0}),

    /**
     * Method called when the document is ready.
     */
    onDocumentReady: function () {
        var self = this;

        // Initialize the upload zone
        this._uploadZone = new UploadZone($('#similar_image_upload_zone'), {
            message: 'Upload or drop an image here to find similar objects.',
            acceptedMimeTypes: ['image/jpeg', 'image/png'],
            multiple: false
        });
        this._uploadZone.setOnFilesAddedListener(function (files) {
            self._searchObjects(files[0]);
        });

        // Initialize the found object mosaic
        this._foundObjectMosaic = new ObjectMosaic($('#found_object_mosaic'), {
            editable: false,
            alwaysShowAsEditable: false,
            showScore: true
        });

        // Initialize the notifier
        this._notifier = new Notifier($('#notifications'));

        // Display the raw response from the image search instance in a new window when the user clicks on the button
        $('#view_raw_btn').on('click', function () {
            $('#view-raw-modal-content').text(self._rawImageSearchResponseJson);
            $('#view-raw-modal').modal('show');
        });

        // Reload the page when the user clicks on the 'New search' button
        $('#new-search-button').on('click', function () {
            window.location.reload();
        });

        // Handle 'Search selection'
        $('#search-selection-button').on('click', function () {
            var $uploadedImage = $('#uploaded-image');
            self._findAndDisplayImage(self._dataURItoBlob($uploadedImage.attr('src')), self._objectRegion);
        });
    },

    /**
     * Convert the base64 url image to blob.
     *
     * @param dataURI
     * @returns {Blob}
     * @private
     */
    _dataURItoBlob: function (dataURI) {
        var binary = window.atob(dataURI.split(',')[1]);
        var array = [];
        for (var i = 0; i < binary.length; i++) {
            array.push(binary.charCodeAt(i));
        }
        return new Blob([new Uint8Array(array)], {type: 'image/jpeg'});
    },

    /**
     * Find and display objects with the given image.
     *
     * @param {File} imageFile
     * @private
     */
    _searchObjects: function (imageFile) {
        var self = this;

        this._setLoadingOverlayVisible(true);

        this._convertAndResizeImageFile(imageFile, function (transformedImageFile) {
            // Hide the upload zone and show the uploaded image instead
            $('#similar_image_upload_zone').hide();
            self._showImage(transformedImageFile);
            self._findAndDisplayImage(transformedImageFile);
        });
    },

    /**
     * Find the similar images and display them.
     *
     * @param transformedImageFile
     * @param objectRegion
     * @private
     */
    _findAndDisplayImage: function (transformedImageFile, objectRegion) {
        var self = this;

        // Start the search
        objectService.findAllBySimilarImage(transformedImageFile, objectRegion, function (objectWithScores, objectRegion, rawImageSearchResponseJson, error) {
            self._setLoadingOverlayVisible(false);

            if (error) {
                self._notifier.showNotification(error, Notifier.Level.DANGER);
                return;
            }

            // Change the layout of the page the first time results are displayed
            if (!self._resultsDisplayed) {
                self._resultsDisplayed = true;

                var $uploadZoneContainer = $('#upload_zone_container');
                $uploadZoneContainer.addClass('upload_zone_container_compact');
                $uploadZoneContainer.addClass('container');
                $('#found_objects_container').show();
            }
            // Show the editable object region
            self._objectRegion = objectRegion;
            self._showEditableObjectRegion();

            // Show the search buttons
            $('#new-search-button').show();
            $('#search-selection-button').show();


            // Update the object mosaic
            var previousObjectUuids = _.map(self._foundObjectMosaic.getAllObjects(), 'uuid');
            self._foundObjectMosaic.removeObjects(previousObjectUuids);
            self._foundObjectMosaic.addObjectWithScores(objectWithScores);

            $('#nb_objects_found').text(objectWithScores.length);

            // Allow the user to view raw JSON response
            self._rawImageSearchResponseJson = rawImageSearchResponseJson;
        });
    },

    /**
     * Show an image on top of the page content.
     *
     * @param {File} imageFile
     * @private
     */
    _showImage: function (imageFile) {
        var $uploadedImage = $('#uploaded-image');
        $uploadedImage.css('display', 'block');
        var reader = new FileReader();
        reader.onload = function (event) {
            $uploadedImage.prop('src', event.target.result);
        };
        reader.readAsDataURL(imageFile);
    },

    /**
     * Allow the user to view the object region where the search has been done.
     * In addition, allow the user to edit this region.
     *
     * @private
     */
    _showEditableObjectRegion: function () {
        var self = this;

        // Get the size of the uploaded image and the scaled version
        var $uploadedImage = $('#uploaded-image');
        var scaledImageWidth = $uploadedImage.width() + 2;
        var scaledImageHeight = $uploadedImage.height() + 2;
        $uploadedImage.css('max-width', 'inherit');
        $uploadedImage.css('max-height', 'inherit');
        var originalImageWidth = $uploadedImage.width() + 2;
        var originalImageHeight = $uploadedImage.height() + 2;
        $uploadedImage.css('max-width', '');
        $uploadedImage.css('max-height', '');

        // Scale the object region
        var hScalingRatio = scaledImageWidth / originalImageWidth;
        var vScalingRatio = scaledImageHeight / originalImageHeight;
        var regionX = this._objectRegion.x * hScalingRatio;
        var regionY = this._objectRegion.y * vScalingRatio;
        var regionWidth = this._objectRegion.width * hScalingRatio;
        var regionHeight = this._objectRegion.height * vScalingRatio;

        // Display the editable object region
        var containerWidth = $('#uploaded-image-container').width();
        var originX = (containerWidth - scaledImageWidth + 30) / 2;

        this._setOverlayBlockShapes(originX, scaledImageWidth, scaledImageHeight, regionX, regionY, regionWidth, regionHeight);

        // Display the region handles
        this._setHandleTopLeftPosition(originX, regionX, regionY);
        this._setHandleBottomRightPosition(originX, regionX, regionY, regionWidth, regionHeight);
        $('.uploaded-image-overlay-handle-cube').show();

        var $handleTopLeft = $('#uploaded-image-overlay-handle-top-left');
        var $handleBottomRight = $('#uploaded-image-overlay-handle-bottom-right');
        var handleTopLeftX = originX + regionX - $handleTopLeft.width() / 2;
        var handleTopLeftY = regionY - $handleTopLeft.height() / 2;
        var handleBottomRightX = originX + regionX + regionWidth - $handleBottomRight.width() / 2;
        var handleBottomRightY = regionY + regionHeight - $handleBottomRight.height() / 2;

        // Listen to region changes and update the object region attribute and enable the 'search selection' button
        var newRegion = {
            x: regionX,
            y: regionY,
            width: regionWidth,
            height: regionHeight
        };
        this._dragHandle($handleTopLeft, originX, handleTopLeftX, handleTopLeftY, function (x, y) {
            newRegion.x = x;
            newRegion.y = y;

            if (newRegion.x < 0) {
                newRegion.x = 0;
            }
            if (newRegion.y < 0) {
                newRegion.y = 0;
            }

            self._objectRegion.x = newRegion.x / hScalingRatio;
            self._objectRegion.y = newRegion.y / vScalingRatio;

            self._setOverlayBlockShapes(originX, scaledImageWidth, scaledImageHeight, newRegion.x, newRegion.y, newRegion.width, newRegion.height);
            self._setHandleBottomRightPosition(originX, newRegion.x, newRegion.y, newRegion.width, newRegion.height);

            $('#search-selection-button').prop('disabled', false);
        });
        this._dragHandle($handleBottomRight, originX, handleBottomRightX, handleBottomRightY, function (x, y) {
            newRegion.width = x - regionX;
            newRegion.height = y - regionY;

            if (newRegion.x + newRegion.width > scaledImageWidth) {
                newRegion.width = scaledImageWidth - newRegion.x;
            }
            if (newRegion.y + newRegion.height > scaledImageHeight) {
                newRegion.height = scaledImageHeight - newRegion.y;
            }

            self._objectRegion.width = newRegion.width / hScalingRatio;
            self._objectRegion.height = newRegion.height / vScalingRatio;

            self._setOverlayBlockShapes(originX, scaledImageWidth, scaledImageHeight, newRegion.x, newRegion.y, newRegion.width, newRegion.height);
            self._setHandleTopLeftPosition(originX, newRegion.x, newRegion.y);

            $('#search-selection-button').prop('disabled', false);
        });
    },

    /**
     * Make the given handle draggable and forward its position.
     *
     * @param $handle
     * @param {number} originX
     * @param {number} originalHandleX
     * @param {number} originalHandleY
     * @param {function(x: number, y: number)} positionListener
     * @private
     */
    _dragHandle: function ($handle, originX, originalHandleX, originalHandleY, positionListener) {
        var $draggable = $handle.draggabilly({});
        var accumulatedMoveVector = {x: 0, y: 0};
        var lastMoveVector = {x: 0, y: 0};

        $draggable.on('dragMove', function (event, pointer, moveVector) {
            lastMoveVector = moveVector;
            var newRegionX = originalHandleX + accumulatedMoveVector.x + moveVector.x - originX + $handle.height() / 2;
            var newRegionY = originalHandleY + accumulatedMoveVector.y + moveVector.y + $handle.height() / 2;
            positionListener(newRegionX, newRegionY);
        });

        $draggable.on('dragEnd', function () {
            accumulatedMoveVector.x += lastMoveVector.x;
            accumulatedMoveVector.y += lastMoveVector.y;
        });
    },

    /**
     * Set the position and the size of the overlays on top of the uploaded image.
     *
     * @param {number} originX
     * @param {number} scaledImageWidth
     * @param {number} scaledImageHeight
     * @param {number} regionX
     * @param {number} regionY
     * @param {number} regionWidth
     * @param {number} regionHeight
     * @private
     */
    _setOverlayBlockShapes: function (originX, scaledImageWidth, scaledImageHeight, regionX, regionY, regionWidth, regionHeight) {
        var $overlayTop = $('#uploaded-image-overlay-top');
        var $overlayBottom = $('#uploaded-image-overlay-bottom');
        var $overlayLeft = $('#uploaded-image-overlay-left');
        var $overlayRight = $('#uploaded-image-overlay-right');

        $overlayTop.css('left', originX + 'px');
        $overlayTop.css('width', scaledImageWidth + 'px');
        $overlayTop.css('top', 0 + 'px');
        $overlayTop.css('height', regionY + 'px');

        $overlayBottom.css('left', originX + 'px');
        $overlayBottom.css('width', scaledImageWidth + 'px');
        $overlayBottom.css('top', (regionY + regionHeight) + 'px');
        $overlayBottom.css('height', (scaledImageHeight - regionY - regionHeight) + 'px');

        $overlayLeft.css('left', originX + 'px');
        $overlayLeft.css('width', regionX + 'px');
        $overlayLeft.css('top', regionY + 'px');
        $overlayLeft.css('height', regionHeight + 'px');

        $overlayRight.css('left', (originX + regionX + regionWidth) + 'px');
        $overlayRight.css('width', (scaledImageWidth - regionX - regionWidth) + 'px');
        $overlayRight.css('top', regionY + 'px');
        $overlayRight.css('height', regionHeight + 'px');
    },

    /**
     * Set the position of the top left handle.
     *
     * @param {number}originX
     * @param {number}regionX
     * @param {number}regionY
     * @private
     */
    _setHandleTopLeftPosition: function (originX, regionX, regionY) {
        var $handleTopLeft = $('#uploaded-image-overlay-handle-top-left');
        var handleTopLeftX = originX + regionX - $handleTopLeft.width() / 2;
        var handleTopLeftY = regionY - $handleTopLeft.height() / 2;
        $handleTopLeft.css('left', handleTopLeftX + 'px');
        $handleTopLeft.css('top', handleTopLeftY + 'px');
    },

    /**
     * Set the position of the bottom right handle.
     *
     * @param {number}originX
     * @param {number}regionX
     * @param {number}regionY
     * @param {number} regionWidth
     * @param {number} regionHeight
     * @private
     */
    _setHandleBottomRightPosition: function (originX, regionX, regionY, regionWidth, regionHeight) {
        var $handleBottomRight = $('#uploaded-image-overlay-handle-bottom-right');
        var handleBottomRightX = originX + regionX + regionWidth - $handleBottomRight.width() / 2;
        var handleBottomRightY = regionY + regionHeight - $handleBottomRight.height() / 2;
        $handleBottomRight.css('left', handleBottomRightX + 'px');
        $handleBottomRight.css('top', handleBottomRightY + 'px');
    },

    /**
     * Process the given image file like this:
     * <ol>
     *     <li>Convert the PNG file to JPEG if necessary (do nothing if the file is already a JPEG).</li>
     *     <li>Resize the image if it is too small or too big.</li>
     * </ol>
     *
     * @param {File} imageFile
     * @param {function(transformedImageFile: File)} callback
     * @private
     */
    _convertAndResizeImageFile: function (imageFile, callback) {
        imageService.convertToJpeg(imageFile, function (jpegImageBlob) {
            imageService.scaleUpImageWithTooSmallResolution(jpegImageBlob, function (largeEnoughImageBlob) {
                imageService.scaleDownImageWithTooBigResolution(largeEnoughImageBlob, function (smallEnoughResolutionImageBlob) {
                    imageService.scaleDownImageWithTooBigSize(smallEnoughResolutionImageBlob, function (smallEnoughImageBlob) {
                        var processedFile = new File([smallEnoughImageBlob], imageFile.name);
                        callback(processedFile);
                    });
                });
            });
        });
    },

    /**
     * Show or hide the loading overlay.
     *
     * @param {boolean} visible
     * @private
     */
    _setLoadingOverlayVisible: function (visible) {
        $('#loading_overlay').css('display', visible ? 'flex' : 'none');
    }
};

$(function () {
    indexController.onDocumentReady();
});