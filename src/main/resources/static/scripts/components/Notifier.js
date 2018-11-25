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
 * Display notifications in stacked blocks.
 *
 * @param $root JQuery-wrapped element that will contain the notifications.
 *
 * @constructor
 * @author Alibaba Cloud
 */
function Notifier($root) {
    /**
     * @private
     */
    this._$root = $root;
}

/**
 * Notification level.
 *
 * @readonly
 * @enum {{color: string}}
 */
Notifier.Level = {
    SUCCESS: {color: '#28a745'},
    INFO: {color: '#17a2b8'},
    WARNING: {color: '#ffc107'},
    DANGER: {color: '#dc3545'}
};

/**
 * Show a new notification with the given message.
 *
 * @param {string} message
 * @param {Notifier.Level} level
 */
Notifier.prototype.showNotification = function (message, level) {
    // Create the notification view
    var $notification = $('' +
        '<div class="notifier_notification" style="background-color: ' + level.color + '">' +
        '    ' + message +
        '</div>');
    this._$root.append($notification);

    // Animate the notification
    $notification.slideDown();
    setTimeout(function () {
        $notification.slideUp(400, function () {
            $notification.remove();
        });
    }, 3000);
};