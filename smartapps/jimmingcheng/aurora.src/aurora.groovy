/**
 *  Aurora
 *
 *  Copyright 2016 Jimming Cheng
 *
 *  Version 1.3.0
 *
 *	Version History
 *
 *  1.3.0   2016-02-15      Added illuminance sensor for dark vs day mode 
 *  1.2.1   2016-02-12      Fixed bugs
 *  1.2.0   2016-02-12      Remove auto-off functionality
 *  1.1.0   2016-02-12      Traversal function and smarter auto-off
 *	1.0.0   2016-02-09      Initial version
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
    name: "Aurora",
    namespace: "jimmingcheng",
    author: "Jimming Cheng",
    description: "Turn lights on/off when entering/exiting rooms.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "mainPage", install: true, uninstall: true) {
        section("Contact Sensors") {
            input "frontDoorContact", "capability.contactSensor", title: "Front Door"
        }
        section("Lux Sensors") {
            input "luxSensor", "capability.illuminanceMeasurement", title: "Lux Sensor"
            input "luxThreshold", "number", title: "Lux Threshold"
        }
        section("Motion Sensors") {
            input "bedroomHallMotion", "capability.motionSensor", title: "Bedroom Hall"
            input "downstairsMotion", "capability.motionSensor", title: "Downstairs"
            input "entryWayEastMotion", "capability.motionSensor", title: "Entry Way East"
            input "entryWayWestMotion", "capability.motionSensor", title: "Entry Way West"
            input "kitchenMotion", "capability.motionSensor", title: "Kitchen"
            input "livingRoomMotion", "capability.motionSensor", title: "Living Room"
            input "masterBathMotion", "capability.motionSensor", title: "Master Bath"
            input "officeMotion", "capability.motionSensor", title: "Office"
            input "upstairsMotion", "capability.motionSensor", title: "Upstairs"
        }
        section("Switches") {
            input "bedroomSwitch", "capability.switch", title: "Bedroom"
            input "downstairsSwitch", "capability.switch", title: "Downstairs"
            input "entryWaySwitch", "capability.switch", title: "Entry Way"
            input "frontDoorSwitch", "capability.switch", title: "Front Door"
            input "frontWalkSwitch", "capability.switch", title: "Front Walk"
            input "kitchenSwitch", "capability.switch", title: "Kitchen"
            input "livingRoomSwitch", "capability.switch", title: "Living Room"
            input "masterBathMirrorSwitch", "capability.switch", title: "Master Bath Mirror"
            input "masterBathSpotLightsSwitch", "capability.switch", title: "Master Bath Spot Lights"
            input "masterClosetSwitch", "capability.switch", title: "Master Closet"
            input "officeSwitch", "capability.switch", title: "Office"
            input "upstairsSwitch", "capability.switch", title: "Upstairs"
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
    state.lastActive = [:]
    state.sleepingDimLevel = 15

    subscribe(frontDoorContact, "contact.open", frontDoorHandler)

    subscribe(bedroomHallMotion, "motion.active", bedroomHallHandler)
    subscribe(downstairsMotion, "motion.active", downstairsHandler)
    subscribe(entryWayEastMotion, "motion.active", entryWayEastHandler)
    subscribe(entryWayWestMotion, "motion.active", entryWayWestHandler)
    subscribe(kitchenMotion, "motion.active", kitchenHandler)
    subscribe(livingRoomMotion, "motion.active", livingRoomHandler)
    subscribe(masterBathMotion, "motion.active", masterBathHandler)
    subscribe(officeMotion, "motion.active", officeHandler)
    subscribe(upstairsMotion, "motion.active", upstairsHandler)

    subscribe(location, "sunriseTime", darknessHandler)
    subscribe(location, "sunsetTime", sunsetTimeHandler)
    subscribe(luxSensor, "illuminance", darknessHandler)

    state.isDark = null
    updateDarkness()
}

def frontDoorHandler(evt) {
    updateDarkness()
    motionActiveHandler(evt.device)
    if (isDark()) {
        if (wasActive(entryWayEastMotion) || wasActive(entryWayWestMotion)) {
            sendOnCommand(frontDoorSwitch)
            sendOnCommand(frontWalkSwitch)
        }
        else {
            sendOnCommand(entryWaySwitch)
        }
    }
}

def bedroomHallHandler(evt) {
    motionActiveHandler(evt.device)
    if (isDark()) {
        if (location.mode == "Sleeping") {
            //sendOnCommand(bedroomHallSwitch, 20)
            //sendOnCommand(masterClosetSwitch, 20)
            if (wasActive(entryWayEastMotion) || wasActive(masterBathMotion)) {
                sendOffCommand(entryWaySwitch)
                sendOffCommand(masterBathMirrorSwitch)
            }
            if (!wasActive(entryWayEastMotion) && !wasActive(masterBathMotion)) {
                sendOnCommand(masterBathMirrorSwitch, state.sleepingDimLevel)
            }
        }
        else {
            //sendOnCommand(bedroomHallSwitch, 100)
            //sendOnCommand(masterClosetSwitch, 100)
            if (wasActive(entryWayEastMotion) || wasActive(masterBathMotion)) {
                sendOnCommand(bedroomSwitch)
            }
        }
    }
}

def downstairsHandler(evt) {
    motionActiveHandler(evt.device, downstairsSwitch)
}

def entryWayEastHandler(evt) {
    motionActiveHandler(evt.device, entryWaySwitch)
}

def entryWayWestHandler(evt) {
    motionActiveHandler(evt.device, entryWaySwitch)
    if (location.mode == "Sleeping") {
        if (wasActive(downstairsMotion)) {
            sendOffCommand(downstairsSwitch)
        }
        if (wasActive(kitchenMotion)) {
            sendOffCommand(kitchenSwitch)
        }
        if (wasActive(livingRoomMotion)) {
            sendOffCommand(livingRoomSwitch)
        }
        if (wasActive(upstairsMotion)) {
            sendOffCommand(upstairsSwitch)
        }
    }
}

def kitchenHandler(evt) {
    motionActiveHandler(evt.device, kitchenSwitch)
}

def livingRoomHandler(evt) {
    motionActiveHandler(evt.device)
    if (isDark()) {
        if (location.mode == "Sleeping") {
            sendOnCommand(livingRoomSwitch)
        }
        if (wasActive(upstairsMotion)) {
            sendOffCommand(upstairsSwitch)
        }
    }
}

def masterBathHandler(evt) {
    motionActiveHandler(evt.device)
    if (location.mode == "Sleeping") {
        sendOnCommand(masterBathMirrorSwitch, state.sleepingDimLevel)
    }
    else {
        sendOnCommand(masterBathMirrorSwitch, 100)
    }
}

def officeHandler(evt) {
    motionActiveHandler(evt.device, officeSwitch)
    if (isDark() && wasActive(upstairsMotion)) {
        sendOffCommand(upstairsSwitch)
    }
}

def upstairsHandler(evt) {
    motionActiveHandler(evt.device, upstairsSwitch)
    if (isDark() && wasActive(officeMotion)) {
        sendOffCommand(officeSwitch)
    }
}

def motionActiveHandler(motionSensor, lightSwitch=null) {
    state.lastActive[motionSensor.id] = now()
    if (isDark() && lightSwitch) {
        sendOnCommand(lightSwitch)
    }
}

def wasActive(motionSensor) {
    return traversedMotion([motionSensor])
}

def traversedMotion(motionSensors) {
    def prevLastActive = 0
    for (m in motionSensors) {
        def lastActive = state.lastActive[m.id]
        if (!lastActive) {
            return false
        }
        if (lastActive < prevLastActive) {
            return false
        }
        if (lastActive < now() - 60*1000) {
            return false
        }
    }

    return true
}

def sendOnCommand(lightSwitch, level=null) {
    if (level == null) {
        if (lightSwitch.currentSwitch != "on") {
            lightSwitch.on()
        }
    }
    else {
        lightSwitch.setLevel(level)
    }
}

def sendOffCommand(lightSwitch) {
    if (lightSwitch.currentSwitch != "off") {
        lightSwitch.off()
    }
}

def sunsetTimeHandler(evt) {
    becameDark()
}

def darknessHandler(evt) {
    updateDarkness()
}

def isDark() {
    return state.isDark
}

def updateDarkness() {
    def ss = getSunriseAndSunset()
    def t = now()
    if (t < ss.sunrise.time || t > ss.sunset.time) {
        becameDark()
    }
    else {
        if (officeSwitch.currentSwitch == "on" || luxSensor.currentIlluminance < luxThreshold) {
            becameDark()
        }
        else {
            becameBright()
        }
    }
}

def becameDark() {
    if (state.isDark != true) {
        state.isDark = true
        sendNotificationEvent("Aurora is in dark mode")
    }
}

def becameBright() {
    if (state.isDark != false) {
        state.isDark = false
        sendNotificationEvent("Aurora is in day mode")
    }
}
