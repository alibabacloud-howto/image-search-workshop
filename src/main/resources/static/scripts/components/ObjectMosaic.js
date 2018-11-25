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
 * Display a mosaic of objects.
 *
 * @param $root JQuery-wrapped element that will contain the object mosaic.
 * @param {{editable: boolean, alwaysShowAsEditable: boolean, showScore: boolean}} config
 *
 * @constructor
 * @author Alibaba Cloud
 */
function ObjectMosaic($root, config) {
    this._$root = $root;

    /** @type {{editable: boolean, alwaysShowAsEditable: boolean, showScore: boolean}} */
    this._config = config;

    /** @type {Array.<RecognizableObject>} */
    this._objects = [];

    /** @type {Object.<string, number>} */
    this._objectScoreByUuid = {};

    /** @type {function(object: RecognizableObject, doneCallback: function())}  */
    this._objectSavedHandler = ObjectMosaic._noopHandler;

    /** @type {function(object: RecognizableObject, doneCallback: function())}  */
    this._objectDeletedHandler = ObjectMosaic._noopHandler;

    /** @type {Object.<string, object>} */
    this._$objectElementByUuid = {};

    this._updateView();
}

/**
 * @private
 * @static
 */
ObjectMosaic._noopHandler = function (object, doneCallback) {
    doneCallback();
};

/**
 * Get all the objects in the mosaic.
 *
 * @returns {Array.<RecognizableObject>}
 */
ObjectMosaic.prototype.getAllObjects = function () {
    return _.clone(this._objects);
};

/**
 * Get all the valid objects in the mosaic.
 *
 * @returns {Array.<RecognizableObject>}
 */
ObjectMosaic.prototype.getAllValidObjects = function () {
    var self = this;

    return _.filter(this._objects, function (object) {
        var $objectElement = self._$objectElementByUuid[object.uuid];
        if (!$objectElement) {
            return false;
        }
        var $saveButton = $objectElement.find('.object_mosaic_id_save_button');
        return !$saveButton.prop('disabled');
    });
};

/**
 * Add new objects to the mosaic.
 *
 * @param {Array.<RecognizableObject>} objects
 */
ObjectMosaic.prototype.addObjects = function (objects) {
    this._objects = _.concat(this._objects, objects);
    this._updateView();
};

/**
 * Add new objects to the mosaic with similarity scores.
 *
 * @param {Array.<ObjectWithScore>} objectWithScores
 */
ObjectMosaic.prototype.addObjectWithScores = function (objectWithScores) {
    var self = this;

    _.each(objectWithScores, function (objectWithScore) {
        self._objectScoreByUuid[objectWithScore.object.uuid] = objectWithScore.similarityScore;
    });
    this.addObjects(_.map(objectWithScores, 'object'));
};

/**
 * Remove objects from the mosaic by their UUIDs.
 *
 * @param {Array.<string>} objectUuids
 */
ObjectMosaic.prototype.removeObjects = function (objectUuids) {
    this._objects = _.filter(this._objects, function (object) {
        return objectUuids.indexOf(object.uuid) === -1;
    });
    this._objectScoreByUuid = _.pickBy(this._objectScoreByUuid, function (score, uuid) {
        return objectUuids.indexOf(uuid) !== -1;
    });
    this._updateView();
};

/**
 * Save all the given objects one by one.
 *
 * @param {Array.<RecognizableObject>} objects
 * @param {function(currentObject: number, totalObjects: number, isComplete: boolean)} progressCallback
 *     Function called each time an object is handled. The currentObject argument starts from 1 and goes
 *     until the totalObjects number.
 */
ObjectMosaic.prototype.saveObjects = function (objects, progressCallback) {
    var self = this;
    progressCallback(0, objects.length, false);

    function saveNextObject(objectIndex) {
        var object = objects[objectIndex];
        var $objectElement = self._$objectElementByUuid[object.uuid];

        self._saveObject($objectElement, object, function () {
            var nextObjectIndex = objectIndex + 1;
            var isComplete = nextObjectIndex >= objects.length;

            progressCallback(nextObjectIndex, objects.length, isComplete);

            if (!isComplete) {
                saveNextObject(nextObjectIndex);
            }
        });
    }

    saveNextObject(0);
};

/**
 * Register a handler that will be called when the user saves an object.
 *
 * @param {function(object: RecognizableObject, doneCallback: function())} objectSavedHandler
 */
ObjectMosaic.prototype.setObjectSavedHandler = function (objectSavedHandler) {
    this._objectSavedHandler = objectSavedHandler;
};

/**
 * Register a handler that will be called when the user deletes an object.
 *
 * @param {function(object: RecognizableObject, doneCallback: function())} objectDeletedHandler
 */
ObjectMosaic.prototype.setObjectDeletedHandler = function (objectDeletedHandler) {
    this._objectDeletedHandler = objectDeletedHandler;
};

/**
 * Show or hide the loading overlay on top of the complete mosaic.
 */
ObjectMosaic.prototype.setLoadingOverlayVisible = function (visible) {
    this._$root.find('.object_mosaic_loading_overlay').css('display', visible ? 'flex' : 'none');
};

/**
 * Add or remove objects from the HTML document.
 *
 * @private
 */
ObjectMosaic.prototype._updateView = function () {
    var self = this;

    // Create HTML elements for the new objects
    _.each(this._objects, function (object) {
        if (!self._$objectElementByUuid[object.uuid]) {
            self._$objectElementByUuid[object.uuid] = self._buildObjectElement(object);
        }
    });

    // Remove elements for the deleted objects
    var objectUuids = _.map(this._objects, 'uuid');
    this._$objectElementByUuid = _.pickBy(this._$objectElementByUuid, function ($objectElement, uuid) {
        return objectUuids.indexOf(uuid) !== -1;
    });

    // Refresh the view
    this._$root.empty();
    var $cardDeck = null;
    _.each(this._objects, function (object, objectIndex) {
        // Layout the element
        var $objectElement = self._$objectElementByUuid[object.uuid];
        if (!$cardDeck || objectIndex % 4 === 0) {
            $cardDeck = $('<div class="card-deck object_mosaic_row"></div>');
            self._$root.append($cardDeck);
        }
        $cardDeck.append($objectElement);

        // Register event handlers
        self._handleObjectElementEvents($objectElement, object);
    });
    this._$root.append('' +
        '<div class="object_mosaic_loading_overlay">\n' +
        '    <img src="/images/loading.gif"/>\n' +
        '</div>');
};

/**
 * Build the HTML element that represents an object.
 *
 * @private
 * @param {RecognizableObject} object
 * @return JQuery-wrapped element that represents objects.
 */
ObjectMosaic.prototype._buildObjectElement = function (object) {
    var additionalNameInputClass = this._config.editable && this._config.alwaysShowAsEditable ?
        'object_mosaic_editable_name_always_shown_as_editable' : '';
    var additionalSaveButtonClass = this._config.editable && this._config.alwaysShowAsEditable ?
        'object_mosaic_id_save_button_always_shown' : '';
    var nameReadonly = this._config.editable ? '' : 'readonly="readonly"';
    var deleteButtonStyle = this._config.editable ? '' : 'style="display:none;"';
    var score = this._objectScoreByUuid[object.uuid];
    var scoreAsText = '';
    if (_.isNumber(score)) {
        score = Math.round(score * 100) / 100;
        scoreAsText = score > 1000 ? '> 1000' : score;
    }
    var scoreStyle = this._config.showScore && _.isNumber(score) ? '' : 'style="display:none;"';

    var $objectElement = $('' +
        '<div class="card object_mosaic_item">\n' +
        '    <img class="card-img-top object_mosaic_img" src="' + object.thumbnailUrl + '"/>\n' +
        '    <div class="card-body object_mosaic_item_body">\n' +
        '        <div class="object_mosaic_editable_name_wrapper">\n' +
        '            <input type="text"\n' +
        '                   class="object_mosaic_editable_name form-control ' + additionalNameInputClass + '"\n' +
        '                   value="' + object.name + '"\n' +
        '                   ' + nameReadonly + '/>\n' +
        '        </div>\n' +
        '        <button type="button"\n' +
        '                class="btn btn-primary input-group-append object_mosaic_id_save_button ' + additionalSaveButtonClass + '">\n' +
        '            <span class="oi oi-check" title="Save" aria-hidden="true"></span>\n' +
        '        </button>\n' +
        '    </div>\n' +
        '    <div class="card-body object_mosaic_errors">\n' +
        '        <span class="object_mosaic_error_min_length">\n' +
        '            The name must have at least one character.\n' +
        '        </span>\n' +
        '        <span class="object_mosaic_error_invalid_characters">\n' +
        '            The name can only contains the following characters: alpha-numeric, space, \'_\', \'-\' and \'.\'.\n' +
        '        </span>\n' +
        '    </div>\n' +
        '    <div class="object_mosaic_score badge badge-danger" ' + scoreStyle + '>' + scoreAsText + '</div>\n' +
        '    <div class="object_mosaic_tool_overlay">\n' +
        '        <button type="button" class="btn btn-secondary object_mosaic_open_fullscreen" title="View"\n' +
        '                data-image-url="' + object.imageUrl + '">\n' +
        '            <span class="oi oi-fullscreen-enter" title="View" aria-hidden="true"></span>\n' +
        '        </button>\n' +
        '        <button type="button" class="btn btn-secondary object_mosaic_delete" title="Delete" ' + deleteButtonStyle + '>\n' +
        '            <span class="oi oi-x" title="Delete" aria-hidden="true"></span>\n' +
        '        </button>\n' +
        '    </div>\n' +
        '    <div class="object_mosaic_item_loading_overlay">\n' +
        '        <img src="/images/loading.gif"/>\n' +
        '    </div>\n' +
        '</div>');

    // Load the image file asynchronously
    if (object.imageFile) {
        var reader = new FileReader();
        reader.onload = function (event) {
            $objectElement.find('.card-img-top').prop('src', event.target.result);
            $objectElement.find('.object_mosaic_open_fullscreen').data('imageAsDataURL', event.target.result);
        };
        reader.readAsDataURL(object.imageFile);
    }

    return $objectElement;
};

/**
 * Handle the input events coming from the given object element.
 *
 * @private
 * @param $objectElement JQuery-wrapped element that represents objects.
 * @param {RecognizableObject} object
 */
ObjectMosaic.prototype._handleObjectElementEvents = function ($objectElement, object) {
    var self = this;
    var $name = $objectElement.find('.object_mosaic_editable_name');
    var $saveButton = $objectElement.find('.object_mosaic_id_save_button');
    var $deleteButton = $objectElement.find('.object_mosaic_delete');
    var $fullscreenButton = $objectElement.find('.object_mosaic_open_fullscreen');

    // When applicable, show the 'save' button when the name has been changed
    var initialName = $name.val();
    if (!this._config.alwaysShowAsEditable) {
        $name.on('change keyup', function () {
            if ($name.val() !== initialName) {
                $saveButton.show();
            } else {
                $saveButton.hide();
            }
        });
    }

    // Show an error when the name is not correct
    $name.on('change keyup', function () {
        self._validateObjectElement($objectElement);
    });

    // Validate the item when loaded
    this._validateObjectElement($objectElement);

    // Update the object and call the handler when the user clicks on the save button
    $saveButton.on('click', function () {
        self._saveObject($objectElement, object);
    });

    // Call the handler when the user clicks on the delete button
    $deleteButton.on('click', function () {
        self._setItemLoadingOverlayVisible($objectElement, true);
        self._objectDeletedHandler(object, function () {
            self._setItemLoadingOverlayVisible($objectElement, false);
            self.removeObjects([object.uuid]);
        });
    });

    // Show the fullscreen image in a modal when the user click on the button
    $fullscreenButton.on('click', function () {
        var url = $fullscreenButton.attr('data-image-url');
        var imageAsDataURL = $fullscreenButton.data('imageAsDataURL');

        var $modal = $('' +
            '<div class="modal fade" tabindex="-1" role="dialog" aria-labelledby="fullscreenModalLabel" aria-hidden="true">\n' +
            '  <div class="modal-dialog" role="document" style="max-width: inherit; margin: inherit;">\n' +
            '    <div class="modal-content" style="height: ' + $(window).height() + 'px; width: ' + $(window).width() + 'px;">\n' +
            '      <div class="modal-header">\n' +
            '        <h5 class="modal-title" id="fullscreenModalLabel">' + object.name + '</h5>\n' +
            '        <button type="button" class="close" data-dismiss="modal" aria-label="Close">\n' +
            '          <span aria-hidden="true">&times;</span>\n' +
            '        </button>\n' +
            '      </div>\n' +
            '      <div class="modal-body text-center">\n' +
            '        <img src="' + (imageAsDataURL ? imageAsDataURL : url) + '" ' +
            '             style="max-height: ' + ($(window).height() - 170) + 'px; max-width: ' + ($(window).width() - 32) + 'px;"/>\n' +
            '      </div>\n' +
            '      <div class="modal-footer">\n' +
            '        <button type="button" class="btn btn-primary" data-dismiss="modal">Close</button>\n' +
            '      </div>\n' +
            '    </div>\n' +
            '  </div>\n' +
            '</div>');
        $modal.modal();
    });
};

/**
 * Validate the given object element and update its visual state.
 *
 * @private
 * @param $objectElement JQuery-wrapped element that represents objects.
 */
ObjectMosaic.prototype._validateObjectElement = function ($objectElement) {
    var $errors = $objectElement.find('.object_mosaic_errors');
    var $errorMessages = $errors.find('> span');
    var $errorMinLength = $objectElement.find('.object_mosaic_error_min_length');
    var $errorInvalidCharacters = $objectElement.find('.object_mosaic_error_invalid_characters');
    var $saveButton = $objectElement.find('.object_mosaic_id_save_button');
    var $name = $objectElement.find('.object_mosaic_editable_name');

    $errorMessages.hide();
    var hasError = false;

    if (this._config.editable) {
        var name = $name.val();
        if (!name || /^(\\s*)$/.test(name)) {
            $errorMinLength.show();
            hasError = true;
        } else if (!/^([a-zA-Z0-9 \.\-_\(\)]+)$/.test(name)) {
            $errorInvalidCharacters.show();
            hasError = true;
        }
    }

    $saveButton.prop('disabled', hasError);
    if (hasError) {
        $errors.show();
        $name.addClass('object_mosaic_editable_name_in_error');
    } else {
        $errors.hide();
        $name.removeClass('object_mosaic_editable_name_in_error');
    }
};

/**
 * Set the user's input into the given object and save it.
 *
 * @private
 * @param $objectElement JQuery-wrapped element that represents objects.
 * @param {RecognizableObject} object
 * @param {function()?} completeCallback
 */
ObjectMosaic.prototype._saveObject = function ($objectElement, object, completeCallback) {
    var self = this;
    var $name = $objectElement.find('.object_mosaic_editable_name');

    object.name = $name.val();
    // TODO update the category

    this._setItemLoadingOverlayVisible($objectElement, true);
    this._objectSavedHandler(object, function () {
        self._setItemLoadingOverlayVisible($objectElement, false);

        if (completeCallback) {
            completeCallback();
        }
    });
};

/**
 * Show or hide the loading overlay on an object element.
 *
 * @param $objectElement JQuery-wrapped element that represents objects.
 * @param {boolean} visible true = show the overlay, false = hide it
 * @private
 */
ObjectMosaic.prototype._setItemLoadingOverlayVisible = function ($objectElement, visible) {
    $objectElement.find('.object_mosaic_item_loading_overlay').css('display', visible ? 'flex' : 'none');
};