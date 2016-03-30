/**
 *  Darken Empty Rooms
 *
 *  Copyright 2016 Jimming Cheng
 *
 *  Version 1.0.0
 *
 *	Version History
 *
 *  1.0.0   2016-03-24      Initial version
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
    name: "Darken Empty Rooms",
    namespace: "jimmingcheng",
    author: "Jimming Cheng",
    description: "Darken Empty Rooms",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "mainPage", install: true, uninstall: true)
}

def mainPage() {
    dynamicPage(name: "mainPage") {
        section("Motion Sensors") {
            input "motionSensors", "capability.motionSensor", multiple: true, required: false, title: "Motion Sensors", submitOnChange: true
        }

        for (motionSensor in motionSensors) {
            section("${motionSensor}") {
                input "switches_${motionSensor.id}", "capability.switch", multiple: true, required: true, title: "Switches"
                input "delay_${motionSensor.id}", "number", required: true, title: "Delay (minutes)", defaultValue: 5
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
    atomicState.timeToTurnOff = [:]

    def v = [:]
    for (motionSensor in motionSensors) {
        for (lightSwitch in settings["switches_${motionSensor.id}"]) {
            v[lightSwitch.id] = motionSensor.id
        }
    }
    atomicState.switchToMotion = v

    subscribe(motionSensors, "motion.active", motionActiveHandler)
    subscribe(motionSensors, "motion.inactive", motionInactiveHandler)
    for (motionSensor in motionSensors) {
        subscribe(settings["switches_${motionSensor.id}"], "switch.on", onHandler)
    }
}

def motionActiveHandler(evt) {
    def v = atomicState.timeToTurnOff
    v.remove(evt.deviceId)
    atomicState.timeToTurnOff = v
}

def motionInactiveHandler(evt) {
    def v = atomicState.timeToTurnOff
    v[evt.deviceId] = now() + settings["delay_${evt.deviceId}"]*60*1000
    atomicState.timeToTurnOff = v

    scheduleNextTurnOff()
}

def onHandler(evt) {
    def motionSensorId = atomicState.switchToMotion[evt.deviceId]

    def v = atomicState.timeToTurnOff
    v.remove(motionSensorId)
    atomicState.timeToTurnOff = v   
}

def scheduleNextTurnOff() {
    def minTimeOff = atomicState.timeToTurnOff.values().min()

    if (minTimeOff) {
        def delay = ((minTimeOff - now()) / 1000).intValue() + 5
        runIn(delay, turnOffSwitches)
    }
}

def turnOffSwitches() {
    def t = now()
    def v = atomicState.timeToTurnOff
    for (motionSensor in motionSensors) {
        def timeOff = atomicState.timeToTurnOff[motionSensor.id]
        if (timeOff && timeOff <= t) {
            settings["switches_${motionSensor.id}"].off()
            v.remove(motionSensor.id)
        }
    }
    atomicState.timeToTurnOff = v
    scheduleNextTurnOff()
}
