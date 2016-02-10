/**
 *  Aurora
 *
 *  Copyright 2016 Jimming Cheng
 *
 *  Version 1.0.0
 *
 *	Version History
 *
 *	1.0.0	2016-02-09		Initial version
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
            input "bedroomMotion", "capability.motionSensor", title: "Bedroom"
            input "downstairsMotion", "capability.motionSensor", title: "Downstairs"
            input "entryWayMotion", "capability.motionSensor", title: "Entry Way"
            input "kitchenMotion", "capability.motionSensor", title: "Kitchen"
            input "livingRoomMotion", "capability.motionSensor", title: "Living Room"
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
    state.lastTurnedOn = [:]
    state.minutesToDisable = 15

    state.minutesOfInactivity = [
        (bedroomMotion.id): 5,
        (bedroomSwitch.id): 5,
        (downstairsMotion.id): 2,
        (downstairsSwitch.id): 2,
        (entryWayMotion.id): 5,
        (entryWaySwitch.id): 5,
        (frontDoorSwitch.id): 5,
        (frontWalkSwitch.id): 5,
        (kitchenMotion.id): 15,
        (kitchenSwitch.id): 15,
        (officeMotion.id): 15,
        (officeSwitch.id): 15,
        (upstairsMotion.id): 2,
        (upstairsSwitch.id): 2
    ]

    state.motionSensorToTurnOffFunc = [
        (bedroomMotion.id): turnOffBedroom,
        (downstairsMotion.id): turnOffDownstairs,
        (entryWayMotion.id): turnOffEntryWay,
        (kitchenMotion.id): turnOffKitchen,
        (officeMotion.id): turnOffOffice,
        (upstairsMotion.id): turnOffUpstairs
    ]

    state.lightSwitchToTurnOffFunc = [
        (bedroomSwitch.id): turnOffBedroom,
        (downstairsSwitch.id): turnOffDownstairs,
        (entryWaySwitch.id): turnOffEntryWay,
        (frontDoorSwitch.id): turnOffFrontDoor,
        (frontWalkSwitch.id): turnOffFrontWalk,
        (kitchenSwitch.id): turnOffKitchen,
        (officeSwitch.id): turnOffOffice,
        (upstairsSwitch.id): turnOffUpstairs
    ]

    subscribe(frontDoorContact, "contact", frontDoorHandler)

    subscribe(bedroomHallMotion, "motion.active", bedroomHallHandler)
    subscribe(bedroomMotion, "motion.active", bedroomHandler)
    subscribe(downstairsMotion, "motion.active", downstairsHandler)
    subscribe(entryWayMotion, "motion.active", entryWayHandler)
    subscribe(kitchenMotion, "motion.active", kitchenHandler)
    subscribe(livingRoomMotion, "motion.active", livingRoomHandler)
    subscribe(officeMotion, "motion.active", officeHandler)
    subscribe(upstairsMotion, "motion.active", upstairsHandler)

    subscribe(bedroomSwitch, "switch", switchHandler)
    subscribe(downstairsSwitch, "switch", switchHandler)
    subscribe(entryWaySwitch, "switch", switchHandler)
    subscribe(frontDoorSwitch, "switch", switchHandler)
    subscribe(frontWalkSwitch, "switch", switchHandler)
    subscribe(kitchenSwitch, "switch", switchHandler)
    subscribe(officeSwitch, "switch", switchHandler)
    subscribe(upstairsSwitch, "switch", switchHandler)

    subscribe(location, "sunriseTime", sunriseSunsetTimeHandler)
    subscribe(location, "sunsetTime", sunriseSunsetTimeHandler)

    astroCheck()
}

def frontDoorHandler(evt) {
    if (!isDark()) { return }
    if (evt.value == "open") {
        if (wasActive(entryWayMotion)) {
            sendOnCommand(frontDoorSwitch)
            sendOnCommand(frontWalkSwitch)
        }
        else {
            sendOnCommand(entryWaySwitch)
        }
    }
}

def bedroomHallHandler(evt) {
    motionActiveHandler(evt)
}

def bedroomHandler(evt) {
    if (location.mode == "Sleeping") {
        motionActiveHandler(evt)
    }
    else {
        motionActiveHandler(evt, bedroomSwitch)
    }
}

def downstairsHandler(evt) {
    motionActiveHandler(evt, downstairsSwitch)
}

def entryWayHandler(evt) {
    motionActiveHandler(evt, entryWaySwitch)
}

def kitchenHandler(evt) {
    motionActiveHandler(evt, kitchenSwitch)
}

def livingRoomHandler(evt) {
    motionActiveHandler(evt, livingRoomSwitch)
    if (isDark() && wasActive(upstairsMotion)) {
        sendOffCommand(upstairsSwitch)
    }
}

def officeHandler(evt) {
    motionActiveHandler(evt, officeSwitch)
    if (isDark() && wasActive(upstairsMotion)) {
        sendOffCommand(upstairsSwitch)
    }
}

def upstairsHandler(evt) {
    motionActiveHandler(evt, upstairsSwitch)
    if (isDark() && wasActive(officeMotion)) {
        sendOffCommand(officeSwitch)
    }
}

def motionActiveHandler(evt, lightSwitch=null) {
    state.lastActive[evt.deviceId] = now()
    if (isDark() && lightSwitch) {
        sendOnCommand(lightSwitch)
        def func = state.motionSensorToTurnOffFunc[evt.deviceId]
        if (func) {
            runIn(secondsOfInactivity(evt.deviceId), func)
        }
    }
}

def secondsOfInactivity(deviceId) {
    return 60*(minutesOfInactivity[deviceId] ?: 5)
}

def turnOffBedroom() { sendOffCommand(bedroomSwitch) }
def turnOffDownstairs() { sendOffCommand(downstairsSwitch) }
def turnOffEntryWay() { sendOffCommand(entryWaySwitch) }
def turnOffFrontDoor() { sendOffCommand(frontDoorSwitch) }
def turnOffFrontWalk() { sendOffCommand(frontWalkSwitch) }
def turnOffKitchen() { sendOffCommand(kitchenSwitch) }
def turnOffOffice() { sendOffCommand(officeSwitch) }
def turnOffUpstairs() { sendOffCommand(upstairsSwitch) }

def wasActive(motionSensor) {
    return (state.lastActive[motionSensor.id] >= now() - 30*1000)
}

def sendOnCommand(lightSwitch) {
    state.lastSentOnCommand[lightSwitch.id] = now()
    lightSwitch.on()
    def func = state.lightSwitchToTurnOffFunc[lightSwitch.id]
    if (func) {
        runIn(secondsOfInactivity(lightSwitch.id), func)
    }
}

def sendOffCommand(lightSwitch) {
    def lastTurnedOn = state.lastTurnedOn[lightSwitch.id] ?: 0
    def lastSentOnCommand = state.lastSentOnCommand[lightSwitch.id] ?: 0

    def turnedOnExternally = (lastTurnedOn - lastSentOnCommand > state.maxCommandLatency)
    turnedOnExternally = false

    if (turnedOnExternally && now() < lastTurnedOn + state.minutesToDisable*60*1000) {
        return
    }

    lightSwitch.off()
}

def switchHandler(evt) {
    if (evt.value == "on") {
        state.lastTurnedOn[evt.deviceId] = now()
    }
}

def isDark() {
    def t = now()
    return (t < state.riseTime || t > state.setTime)
}

def sunriseSunsetTimeHandler(evt) {
	state.lastSunriseSunsetEvent = now()
	astroCheck()
    if (isDark()) {
        sendNotificationEvent("Aurora is online")
    }
    else {
        sendNotificationEvent("Aurora is offline")
    }
}

def astroCheck() {
	def ss = getSunriseAndSunset()
	state.riseTime = ss.sunrise.time
	state.setTime = ss.sunset.time
}