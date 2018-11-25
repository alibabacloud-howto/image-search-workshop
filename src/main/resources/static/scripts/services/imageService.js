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
 * Convert and resize images when too small or too large.
 *
 * @author Alibaba Cloud
 */
var imageService = {

    jpegCompressionQuality: 0.9,
    minImageWidth: 200,
    minImageHeight: 200,
    maxImageFileSize: 1000000,
    thumbnailMaxSize: 142,
    maxImageWidth: 1024,
    maxImageHeight: 1024,

    /**
     * Resize the given image to the given size.
     * Note: if the targetWidth or targetHeight are equal to -1, then we keep the original value.
     *
     * @param {Blob} imageBlob
     * @param {number} targetWidth
     * @param {number} targetHeight
     * @param {function(transformedImageBlob: Blob)} callback
     */
    resize: function (imageBlob, targetWidth, targetHeight, callback) {
        var canvas = document.createElement('canvas');
        var canvasContext = canvas.getContext('2d');

        this._convertBlobToImage(imageBlob, function (image) {
            var dWidth = targetWidth === -1 ? image.width : targetWidth;
            var dHeight = targetHeight === -1 ? image.height : targetHeight;
            canvas.width = dWidth;
            canvas.height = dHeight;
            canvasContext.drawImage(image, 0, 0, image.width, image.height, 0, 0, dWidth, dHeight);
            canvas.toBlob(function (transformedImageBlob) {
                callback(transformedImageBlob);
            }, 'image/jpeg', 0.9);
        });
    },

    /**
     * Convert the given image to JPEG format.
     *
     * @param {Blob} imageBlob
     * @param {function(transformedImageBlob: Blob)} callback
     */
    convertToJpeg: function (imageBlob, callback) {
        if (imageBlob.type !== 'image/jpeg') {
            this.resize(imageBlob, -1, -1, callback);
        } else {
            callback(imageBlob);
        }
    },

    /**
     * Scale up the given image if its resolution is less than 200x200.
     *
     * @param {Blob} imageBlob Image to resize if it is too small.
     * @param {function(transformedImageBlob: Blob)} callback
     *     Scaled image with a resolution higher than 200x200. Or original image if
     *     its resolution is already large enough.
     */
    scaleUpImageWithTooSmallResolution: function (imageBlob, callback) {
        var self = this;

        this._convertBlobToImage(imageBlob, function (image) {
            // Return the image if the size is lower than the limit
            if (image.width >= self.minImageWidth && image.height >= self.minImageHeight) {
                return callback(imageBlob);
            }

            // Choose the target image dimension (keep the aspect ratio)
            var imageWidth = image.width;
            var imageHeight = image.height;
            var targetWidth;
            var targetHeight;

            if (imageWidth < self.minImageWidth && imageHeight >= self.minImageHeight) {
                // Case 1: only the width is too small
                targetWidth = self.minImageWidth;
                targetHeight = Math.ceil(imageHeight * self.minImageWidth / imageWidth);
            } else if (imageWidth >= self.minImageWidth && imageHeight < self.minImageHeight) {
                // Case 2: only the height is too small
                targetWidth = Math.ceil(imageWidth * self.minImageHeight / imageHeight);
                targetHeight = self.minImageHeight;
            } else {
                // Case 3: both width and height are too small, find which dimension will determine the scaling ratio
                if (self.minImageWidth - imageWidth > self.minImageHeight - imageHeight) {
                    // Case 3.1: Scale according to the width
                    targetWidth = self.minImageWidth;
                    targetHeight = Math.ceil(imageHeight * self.minImageWidth / imageWidth);
                } else {
                    // Case 3.2: Scale according to the height
                    targetWidth = Math.ceil(imageWidth * self.minImageHeight / imageHeight);
                    targetHeight = self.minImageHeight;
                }
            }

            self.resize(imageBlob, targetWidth, targetHeight, function (transformedImageBlob) {
                callback(transformedImageBlob);
            });
        });
    },

    /**
     * Scale down the given image if its resolution is greater than 1024x1024.
     *
     * @param {Blob} imageBlob     Image to resize if it is too big.
     * @param {function(transformedImageBlob: Blob)} callback
     *     Scaled image with a resolution lower than 1024x1024. Or original image if
     *     its resolution is already small enough.
     */
    scaleDownImageWithTooBigResolution: function (imageBlob, callback) {
        var self = this;

        this._convertBlobToImage(imageBlob, function (image) {
            // Return the image if the resolution is lower than the limit
            if (image.width <= self.maxImageWidth && image.height <= self.maxImageHeight) {
                return callback(imageBlob);
            }

            // Choose the target image dimension (keep the aspect ratio)
            var targetWidth;
            var targetHeight;
            if (image.width > image.height) {
                targetWidth = self.maxImageWidth;
                targetHeight = Math.floor(image.height * self.maxImageWidth / image.width);
            } else {
                targetWidth = Math.floor(image.width * self.maxImageHeight / image.height);
                targetHeight = self.maxImageHeight;
            }

            self.resize(imageBlob, targetWidth, targetHeight, function (transformedImageBlob) {
                callback(transformedImageBlob);
            });
        });
    },

    /**
     * Scale down the given image if its size is bigger than the given limit.
     *
     * @param {Blob} imageBlob     Image to resize if it is too big.
     * @param {function(transformedImageBlob: Blob)} callback
     *     Scaled image if the file size lower than the limit. Or original image if its size is already lower than
     *     the limit.
     */
    scaleDownImageWithTooBigSize: function (imageBlob, callback) {
        var self = this;

        // Return the image if the size is lower than the limit
        if (imageBlob.size <= this.maxImageFileSize) {
            return callback(imageBlob);
        }

        this._convertBlobToImage(imageBlob, function (image) {
            // Successively scale down the image until the file size respects the target
            var startingScalingRatio = Math.sqrt(self.maxImageFileSize / imageBlob.size);

            function resizeBy(scalingRatio) {
                var targetWidth = Math.round(image.width * scalingRatio);
                var targetHeight = Math.round(image.height * scalingRatio);

                self.resize(imageBlob, targetWidth, targetHeight, function (transformedImageBlob) {
                    if (transformedImageBlob.size <= self.maxImageFileSize) {
                        callback(transformedImageBlob);
                    } else {
                        if (scalingRatio > 0.1) {
                            scalingRatio -= 0.1;
                        } else {
                            scalingRatio /= 2;
                        }
                        resizeBy(scalingRatio);
                    }
                });
            }

            resizeBy(startingScalingRatio);
        });
    },

    /**
     * Create a thumbnail for the given image.
     *
     * @param {Blob} imageBlob
     * @param {function(thumbnailBlob: Blob)} callback
     */
    createThumbnail: function (imageBlob, callback) {
        var self = this;

        this._convertBlobToImage(imageBlob, function (image) {
            // Return the image if the size is lower than the limit
            if (image.width <= self.thumbnailMaxSize && image.height <= self.thumbnailMaxSize) {
                return callback(imageBlob);
            }

            // Choose the target image dimension (keep the aspect ratio)
            var targetWidth;
            var targetHeight;
            if (image.width > image.height) {
                targetWidth = self.thumbnailMaxSize;
                targetHeight = Math.floor(image.height * self.thumbnailMaxSize / image.width);
            } else {
                targetWidth = Math.floor(image.width * self.thumbnailMaxSize / image.height);
                targetHeight = self.thumbnailMaxSize;
            }

            self.resize(imageBlob, targetWidth, targetHeight, function (transformedImageBlob) {
                callback(transformedImageBlob);
            });
        });
    },

    /**
     *  Convert a blob into an image.
     *
     * @param {Blob} imageBlob
     * @param {function(image: HTMLImageElement)} callback
     * @private
     */
    _convertBlobToImage: function (imageBlob, callback) {
        var image = new Image();
        image.onload = function () {
            callback(image);
        };
        image.src = URL.createObjectURL(imageBlob);
    }
};