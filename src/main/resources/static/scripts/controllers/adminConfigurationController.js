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
 * Configuration page main controller.
 *
 * @author Alibaba Cloud
 */
var adminConfigurationController = {

    /**
     * @type {string}
     */
    _password: '',

    /**
     * Method called when the document is ready.
     */
    onDocumentReady: function () {
        var self = this;

        // Disable save and check buttons when the configuration is empty
        this._updateButtonsStatus();
        $('#accessKeyId, #accessKeySecret, #regionID, #imageSearchInstanceName, #imageSearchDomain, #imageSearchNamespace').on('change keyup', function () {
            self._updateButtonsStatus();
        });

        // Load the configuration and update the view
        this._loadConfiguration();

        // Handle authentication modal events
        $('#authentication-modal-submit').on('click', function () {
            self._password = $('#authentication-modal-password').val();
            $('#authentication-modal').modal('hide');
            self._loadConfiguration();
        });

        $('#authentication-modal-password').on('keypress', function (event) {
            if (event.key === 'Enter') {
                $('#authentication-modal-submit').click();
            }
        });

        // Check the configuration when the user click on the corresponding button
        $('#save_configuration').on('click', function () {
            var configuration = self._getConfigurationFromForm();

            // Check the password and the confirmation are the same
            if (configuration.password !== $('#passwordConfirmation').val()) {
                $('#server-response-modal-message').text('The passwords are different!');
                $('#server-response-modal').modal('show');
                return false;
            }

            // Save the configuration
            self._setLoadingPanelVisible(true);
            configurationService.saveConfiguration(self._password, configuration, function (error) {
                self._setLoadingPanelVisible(false);

                if (error) {
                    $('#server-response-modal-message').text(error);
                    $('#server-response-modal').modal('show');
                } else {
                    $('#server-response-modal-message').text('The configuration has been saved with success!');
                    $('#server-response-modal').modal('show');
                }
            });
        });

        // Save the configuration when the user clicks on the corresponding button
        $('#check_configuration').on('click', function () {
            self._setLoadingPanelVisible(true);
            var configuration = self._getConfigurationFromForm();
            configurationService.checkConfiguration(self._password, configuration, function (error) {
                self._setLoadingPanelVisible(false);

                if (error) {
                    $('#server-response-modal-message').text(error);
                    $('#server-response-modal').modal('show');
                } else {
                    $('#server-response-modal-message').text('The configuration is valid! You can now save it.');
                    $('#server-response-modal').modal('show');
                }
            });
        });
    },

    /**
     * Load the configuration from the server and display it in the form.
     * Mote: also display an authentication modal if applicable.
     */
    _loadConfiguration: function () {
        var self = this;

        this._setLoadingPanelVisible(true);
        configurationService.getConfiguration(this._password, function (configuration, errorCode, errorMessage) {
            self._setLoadingPanelVisible(false);

            if (errorCode) {
                if (errorCode === 401) { // Not authenticated
                    setTimeout(function () {
                        $('#authentication-modal').modal({backdrop: 'static', keyboard: false});
                    }, 500);
                } else if (errorCode !== 404) {
                    alert(errorMessage);
                }
            } else {
                self._setConfigurationIntoForm(configuration);
                self._updateButtonsStatus();
            }
        });
    },

    /**
     * Disable the buttons when the fields are not all set.
     */
    _updateButtonsStatus: function () {
        var configuration = this._getConfigurationFromForm();
        var hasBlankField = /^\s*$/.test(configuration.accessKeyId) ||
            /^\s*$/.test(configuration.accessKeySecret) ||
            /^\s*$/.test(configuration.regionId) ||
            /^\s*$/.test(configuration.imageSearchInstanceName) ||
            /^\s*$/.test(configuration.imageSearchDomain);

        $('#save_configuration').prop('disabled', hasBlankField);
        $('#check_configuration').prop('disabled', hasBlankField);
    },

    /**
     * Get the configuration from the form input fields.
     *
     * @return {Configuration}
     */
    _getConfigurationFromForm: function () {
        return new Configuration({
            password: $('#password').val(),
            accessKeyId: $('#accessKeyId').val(),
            accessKeySecret: $('#accessKeySecret').val(),
            regionId: $('#regionID').val(),
            imageSearchInstanceName: $('#imageSearchInstanceName').val(),
            imageSearchDomain: $('#imageSearchDomain').val(),
            imageSearchNamespace: $('#imageSearchNamespace').val(),
            ossBaseUrl: $('#ossBaseUrl').val()

        });
    },

    /**
     * Set the configuration into the form input fields.
     *
     * @param {Configuration} configuration
     */
    _setConfigurationIntoForm: function (configuration) {
        $('#accessKeyId').val(configuration.accessKeyId);
        $('#accessKeySecret').val(configuration.accessKeySecret);
        $('#regionID').val(configuration.regionId);
        $('#imageSearchInstanceName').val(configuration.imageSearchInstanceName);
        $('#imageSearchDomain').val(configuration.imageSearchDomain);
        $('#imageSearchNamespace').val(configuration.imageSearchNamespace);
        $('#ossBaseUrl').val(configuration.ossBaseUrl);
    },

    /**
     * Show or hide the loading panel.
     *
     * @param {boolean} visible
     */
    _setLoadingPanelVisible: function (visible) {
        $('#loading_panel').css('display', visible ? 'flex' : 'none');
    }
};

$(function () {
    adminConfigurationController.onDocumentReady();
});