/**
 *  Off Only Switch
 *
 *  A switch that can only be turned off.
 *
 *  Copyright 2016 Jimming Cheng
 *
 *  Version 1.0.0
 *
 *	Version History
 *
 *	1.0.0	2016-02-06		Initial version
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
metadata {
	definition (name: "Off Only Switch", namespace: "jimmingcheng", author: "Jimming Cheng") {
		capability "Switch"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.off", backgroundColor: "#79b821"
		}
        main "switch"
		details(["switch","on","off"])
	}
}

def parse(String description) {
    return
}

def on() {
	sendEvent(name: "switch", value: "off", isStateChange: true)
}

def off() {
	sendEvent(name: "switch", value: "off", isStateChange: true)
}

