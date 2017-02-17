/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    // deviceready Event Handler
    //
    // Bind any cordova events here. Common events are:
    // 'pause', 'resume', etc.
    onDeviceReady: function() {
        this.receivedEvent('deviceready');

        console.log('sl: start');
        sl.start();

        var slOn = document.getElementById("sl_on");
        var slOff = document.getElementById("sl_off");
        var settingOn = document.getElementById("setting_on");
        var settingOff = document.getElementById("setting_off");

        var self = this;

        slOn.addEventListener('click', function(e) {
            console.log('sl: screen on');
            sl.show_screen();
            self.showCurrentScreenStatus();
        }, false);

        slOff.addEventListener('click', function(e) {
            console.log('sl: screen off');
            sl.hide_screen();
            self.showCurrentScreenStatus();
        }, false);

        settingOn.addEventListener('click', function(e) {
            console.log('sl: setting on');
            sl.show_setting();
            self.showCurrentScreenStatus();
        }, false);

        settingOff.addEventListener('click', function(e) {
            console.log('sl: setting off');
            sl.hide_setting();
            self.showCurrentScreenStatus();
        }, false);

        this.showCurrentScreenStatus();
    },

    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    },

    showCurrentScreenStatus: function() {
        var ret = document.getElementById("ret");
        var screen = "";
        var setting = "";
        sl.get_screen(
             function(msg) {
                 console.log(msg);
                 screen = msg;
                 if (setting) {
                    ret.innerText = "current screen:" + screen + " setting:" + setting;
                 }
             },
             function(msg) {
                 console.log(msg)
             }
        );

        sl.get_setting(
             function(msg) {
                 console.log(msg);
                 setting = msg;
                 if (screen) {
                     ret.innerText = "current screen:" + screen + " setting:" + setting;
                 }
             },
             function(msg) {
                 console.log(msg)
             }
        );
    }
};

app.initialize();