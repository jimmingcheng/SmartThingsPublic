/**
 *  Linked Switches
 *
 *  Copyright 2016 Jimming Cheng
 *
 *  Version 1.0.0
 *
 *	Version History
 *
 *	1.0.0	2016-02-04		Initial version
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Linked Switches",
    namespace: "jimmingcheng",
    author: "Jimming Cheng",
    description: "Linked Switches",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "mainPage", title: "Linked Switches", install: true, uninstall: true) {
        section {
            app(name: "linkedSwitchesInstance", appName: "Linked Switches Instance", namespace: "jimmingcheng", title: "New Linked Switches", multiple: true)
        }
    }
}
