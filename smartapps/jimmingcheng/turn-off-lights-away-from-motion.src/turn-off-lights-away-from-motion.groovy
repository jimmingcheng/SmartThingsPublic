/**
 *  Turn Off Lights Away From Motion
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
definition(
    name: "Turn Off Lights Away From Motion",
    namespace: "jimmingcheng",
    author: "Jimming Cheng",
    description: "Allow a virtual controlling switch to turn off a set of lights based on the last active motion sensor.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "mainPage", install: true, uninstall: true)
}

def mainPage() {
    dynamicPage(name: "mainPage") {
        section("Controlling Switch") {
            input "controllingSwitch", "capability.switch", required: true, title: "Controlling Switch"
        }
        for (zone in [1, 2, 3, 4]) {
            section("Zone ${zone}") {
                input "motionSensors_${zone}", "capability.motionSensor", multiple: true, required: false, title: "Motion Sensors"
                input "awaySwitches_${zone}", "capability.switch", multiple: true, required: false, title: "Switches Away From Motion"
            }
        }
    }
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
    state.zones = [1, 2, 3, 4]
    state.lastActiveMotionSensorId = null
    for (zone in state.zones) {
        subscribe(settings["motionSensors_${zone}"], "motion.active", motionActiveHandler)
    }
    subscribe(controllingSwitch, "switch.off", offHandler)
}

def motionActiveHandler(evt) {
    state.lastActiveMotionSensorId = evt.deviceId
}

def offHandler(evt) {
    for (zone in state.zones) {
        for (motionSensor in settings["motionSensors_${zone}"]) {
            if (motionSensor.id == state.lastActiveMotionSensorId) {
                sendNotificationEvent("Turning off switches away from ${motionSensor}")
                for (lightSwitch in settings["awaySwitches_${zone}"]) {
                    if (lightSwitch.currentSwitch == "on") {
                        lightSwitch.off()
                    }
                }
                break
            }
        }
    }
}

