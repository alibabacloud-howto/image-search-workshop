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
 * Display a zone where the user can drop files.
 *
 * @param $root JQuery-wrapped element that will contain the upload zone.
 * @param {{message: string, acceptedMimeTypes: Array.<string>, multiple: boolean}} config
 *
 * @constructor
 * @author Alibaba Cloud
 */
function UploadZone($root, config) {
    /**
     * @private
     */
    this._$root = $root;

    /**
     * @type {{message: string, acceptedMimeTypes: Array.<string>, multiple: boolean}}
     * @private
     */
    this._config = config;

    /**
     * @type {function(files: Array.<File>)}
     * @private
     */
    this._onFilesAddedListener = _.noop;

    this._init();
}

/**
 * @type {number}
 * @private
 * @static
 */
UploadZone._nextInputId = 0;

/**
 * Register a listener that will be called when the user uploads files.
 *
 * @param {function(files: Array.<File>)} onFilesAddedListener
 */
UploadZone.prototype.setOnFilesAddedListener = function (onFilesAddedListener) {
    this._onFilesAddedListener = onFilesAddedListener;
};

/**
 * Initialize the component.
 *
 * @private
 */
UploadZone.prototype._init = function () {
    var self = this;

    // Build a form element with a file input and add it to the document
    var $form = this._buildView();
    this._$root.empty();
    this._$root.append($form);

    // Listen to selected files event
    var $inputFile = $form.find('input[type="file"]');
    $inputFile.on('click', function () {
        $(this).val(null);
    });
    $inputFile.on('change', function (event) {
        var files = _.map(event.originalEvent.target.files, _.identity);
        self._onFilesAddedListener(files);

        $(this).val(null);
    });

    // Listen to dropped files
    $form.on('dragover', function (event) {
        event.preventDefault();
    });
    $form.on('drop', function (event) {
        event.preventDefault();

        // Extract the dropped files
        var files = [];
        if (event.originalEvent.dataTransfer.items) {
            files = _
                .chain(event.originalEvent.dataTransfer.items)
                .filter(function (item) {
                    return item.kind === 'file';
                })
                .map(function (item) {
                    return item.getAsFile();
                })
                .value();
        } else {
            files = _.map(event.originalEvent.dataTransfer.files, _.identity);
        }

        // Filter the files by their types and process them
        var filteredFiles = _.filter(files, function (file) {
            return self._config.acceptedMimeTypes.indexOf(file.type) !== -1;
        });
        if (filteredFiles) {
            if (!self._config.multiple && filteredFiles.length > 1) {
                filteredFiles = [filteredFiles[0]];
            }

            self._onFilesAddedListener(filteredFiles);
        }

        // Clear the drag data
        if (event.originalEvent.dataTransfer.items) {
            event.originalEvent.dataTransfer.items.clear();
        } else {
            event.originalEvent.dataTransfer.clearData();
        }
    });
};

/**
 * Build a form element with a file input.
 *
 * @return JQuery-wrapped element that represents objects.
 * @private
 */
UploadZone.prototype._buildView = function () {
    var inputId = UploadZone._nextInputId++;
    var accept = _.join(this._config.acceptedMimeTypes);
    var multipleParam = this._config.multiple ? 'multiple="multiple"' : '';

    return $('' +
        '<form action="#" method="post" enctype="multipart/form-data" class="upload-zone">\n' +
        '    <label for="' + inputId + '">\n' +
        '        <div class="upload_zone_message">' + this._config.message + '</div>\n' +
        '    </label>\n' +
        '    <input type="file" id="' + inputId + '" name="' + inputId + '" accept="' + accept + '" ' + multipleParam + '/>\n' +
        '</form>');
};
