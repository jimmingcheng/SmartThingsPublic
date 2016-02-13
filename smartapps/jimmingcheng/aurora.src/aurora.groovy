/**
 *  Aurora
 *
 *  Copyright 2016 Jimming Cheng
 *
 *  Version 1.1.0
 *
 *	Version History
 *
 *	1.0.0	2016-02-09		Initial version
 *  1.1.0   2016-02-12      Traversal function and smarter auto-off
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
    state.lastSentOnCommand = [:]
    state.lastSentOffCommand = [:]
    state.lastTurnedOn = [:]

    state.minutesUntilAutoOff = [
        (downstairsSwitch.id): 2,
        (entryWaySwitch.id): 5,
        (frontDoorSwitch.id): 5,
        (frontWalkSwitch.id): 5,
        (kitchenSwitch.id): 5,
        (masterBathSpotLightsSwitch.id): 5,
        (masterClosetSwitch.id): 5,
        (officeSwitch.id): 5,
        (upstairsSwitch.id): 2
    ]
    state.lightSwitchToTurnOffFunc = [
        (bedroomSwitch.id): turnOffBedroom,
        (downstairsSwitch.id): turnOffDownstairs,
        (entryWaySwitch.id): turnOffEntryWay,
        (frontDoorSwitch.id): turnOffFrontDoor,
        (frontWalkSwitch.id): turnOffFrontWalk,
        (kitchenSwitch.id): turnOffKitchen,
        (masterBathMirrorSwitch.id): turnOffMasterBathMirror,
        (masterBathSpotLightsSwitch.id): turnOffMasterBathSpotLights,
        (masterClosetSwitch.id): turnOffMasterCloset,
        (officeSwitch.id): turnOffOffice,
        (upstairsSwitch.id): turnOffUpstairs
    ]

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

    subscribe(bedroomSwitch, "switch", switchHandler)
    subscribe(downstairsSwitch, "switch", switchHandler)
    subscribe(entryWaySwitch, "switch", switchHandler)
    subscribe(frontDoorSwitch, "switch", switchHandler)
    subscribe(frontWalkSwitch, "switch", switchHandler)
    subscribe(kitchenSwitch, "switch", switchHandler)
    subscribe(masterBathMirrorSwitch, "switch", switchHandler)
    subscribe(masterBathSpotLightsSwitch, "switch", switchHandler)
    subscribe(masterClosetSwitch, "switch", switchHandler)
    subscribe(officeSwitch, "switch", switchHandler)
    subscribe(upstairsSwitch, "switch", switchHandler)

    subscribe(location, "sunriseTime", sunriseTimeHandler)
    subscribe(location, "sunsetTime", sunsetTimeHandler)

    initIsDark()
}

def frontDoorHandler(evt) {
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
            //sendOnCommand(bedroomHallSwitch, 50)
            sendOnCommand(masterClosetSwitch, 20)
            if (
                traversedMotion([entryWayEastMotion, bedroomHallMotion]) &&
                !traversedMotion([entryWayEastMotion, masterBathMotion])
            ) {
                sendOffCommand(entryWaySwitch)
            }
            else if (
                traversedMotion([masterBathMotion, bedroomHallMotion]) &&
                !traversedMotion([masterBathMotion, entryWayEastMotion])
            ) {
                sendOffCommand(masterBathMirrorSwitch)
            }
            else if (!wasActive(entryWayEastMotion) && !wasActive(masterBathMotion)) {
                sendOnCommand(masterBathMirrorSwitch, 20)
            }
        }
        else {
            //sendOnCommand(bedroomHallSwitch, 100)
            sendOnCommand(masterClosetSwitch, 100)
            if (
                traversedMotion([entryWayEastMotion, bedroomHallMotion]) ||
                traversedMotion([masterBathMotion, bedroomHallMotion])
            ) {
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
}

def kitchenHandler(evt) {
    motionActiveHandler(evt.device, kitchenSwitch)
}

def livingRoomHandler(evt) {
    motionActiveHandler(evt.device, livingRoomSwitch)
    if (isDark() && traversedMotion([upstairsMotion, livingRoomMotion])) {
        sendOffCommand(upstairsSwitch)
    }
}

def masterBathHandler(evt) {
    motionActiveHandler(evt.device)
    if (location.mode == "Sleeping") {
        sendOnCommand(masterBathMirrorSwitch, 50)
    }
    else {
        sendOnCommand(masterBathMirrorSwitch, 100)
    }
}

def officeHandler(evt) {
    motionActiveHandler(evt.device, officeSwitch)
    if (isDark() && traversedMotion([officeMotion, upstairsMotion])) {
        sendOffCommand(upstairsSwitch)
    }
}

def upstairsHandler(evt) {
    motionActiveHandler(evt.device, upstairsSwitch)
    if (isDark() && traversedMotion([upstairsMotion, officeMotion])) {
        sendOffCommand(officeSwitch)
    }
}

def motionActiveHandler(motionSensor, lightSwitch=null) {
    state.lastActive[motionSensor.id] = now()
    if (isDark() && lightSwitch) {
        sendOnCommand(lightSwitch)
    }
}

def secondsUntilAutoOff(deviceId) {
    def mins = state.minutesUntilAutoOff[deviceId]
    if (mins) {
        return 60*mins
    }
    else {
        return mins
    }
}

def turnOffBedroom() { finishSendOffCommand(bedroomSwitch) }
def turnOffDownstairs() { finishSendOffCommand(downstairsSwitch) }
def turnOffEntryWay() { finishSendOffCommand(entryWaySwitch) }
def turnOffFrontDoor() { finishSendOffCommand(frontDoorSwitch) }
def turnOffFrontWalk() { finishSendOffCommand(frontWalkSwitch) }
def turnOffKitchen() { finishSendOffCommand(kitchenSwitch) }
def turnOffMasterBathMirror() { finishSendOffCommand(masterBathMirrorSwitch) }
def turnOffMasterBathSpotLights() { finishSendOffCommand(masterBathSpotLightsSwitch) }
def turnOffMasterCloset() { finishSendOffCommand(masterClosetSwitch) }
def turnOffOffice() { finishSendOffCommand(officeSwitch) }
def turnOffUpstairs() { finishSendOffCommand(upstairsSwitch) }

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
    state.lastSentOnCommand[lightSwitch.id] = now()
    if (level == null) {
        lightSwitch.on()
    }
    else {
        lightSwitch.setLevel(level)
    }
    def secs = secondsUntilAutoOff(lightSwitch.id)
    if (secs) {
        sendOffCommand(lightSwitch, secs)
    }
}

def sendOffCommand(lightSwitch, delay=0) {
    state.lastSentOffCommand[lightSwitch.id] = now()
    def func = state.lightSwitchToTurnOffFunc[lightSwitch.id]
    if (delay && func) {
        runIn(delay, func)
    }
    else {
        finishSendOffCommand(lightSwitch)
    }
}

def finishSendOffCommand(lightSwitch) {
    def lastTurnedOn = state.lastTurnedOn[lightSwitch.id] ?: 0
    def lastSentOnCommand = state.lastSentOnCommand[lightSwitch.id] ?: 0
    def lastSentOffCommand = state.lastSentOffCommand[lightSwitch.id] ?: 0

    if (lastTurnedOn > lastSentOffCommand || lastSentOnCommand > lastSentOnCommand) {
        return
    }

    lightSwitch.off()
}

def switchHandler(evt) {
    if (evt.value == "on") {
        state.lastTurnedOn[evt.deviceId] = now()
    }
}

def sunriseTimeHandler(evt) {
    becameBright()
}

def sunsetTimeHandler(evt) {
    becameDark()
}

def isDark() {
    return state.isDark
}

def initIsDark() {
    state.isDark = null

    def ss = getSunriseAndSunset()
    def t = now()
    if (t < ss.sunriseTime || t > state.sunsetTime) {
        becameDark()
    }
    else {
        becameBright()
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
