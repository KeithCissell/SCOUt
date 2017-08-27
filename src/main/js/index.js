import {pingServer,
        getCurrentState,
        newRandomEnvironment} from './SCOUtAPI.js'
import {buildEnvironment} from './environment/EnvironmentBuilder.js'
import {loadEnvironmentDisplay} from './environment/EnvironmentDisplay.js'

window.$ = window.jQuery = require('jquery')
const fetch = require('node-fetch')


// Document Elements
const header = document.getElementById("header")
const toolbar = document.getElementById("toolbar")
const main = document.getElementById("main")
const message = document.getElementById("message")
const mainContent = document.getElementById("content")

// Establish connection with SCOUt server
document.addEventListener("DOMContentLoaded", () => {
  establishConnection()
})


// Attempts to establish connection with SCOUt Server
async function establishConnection() {
  mainContent.innerHTML = ""
  message.innerHTML = "Attempting to contact SCOUt server..."
  let contactMade = await attemptContact()
  //await console.clear()
  if (contactMade) successfulContact()
  else unsuccessfulContact()
}

// Returns response once server is contacted or attempts reaches 0
async function attemptContact() {
  let attempts = 20   // number of attempts to reach server
  let buffer = 500    // time between contact attempts (ms)
  for (let i = 0; i < attempts; i++) {
    console.log("Server contact attempt: " + i)
    let response = await new Promise(resolve => {
      setTimeout(() => { resolve(pingServer()) }, buffer)
    })
    if (response.ok) return true
  }
  return false
}

// Handles successful server contact
async function successfulContact() {
  mainContent.innerHTML = ""
  message.innerHTML = "We have contact!!"
  let nre = await newRandomEnvironment("RandomTest", 100, 100)
  nre.json().then((json) => {
    let environment = buildEnvironment(json)
    console.log(environment)
    loadEnvironmentDisplay(environment)
  }).catch((err) => { console.log(err) })
}

// Handles unsuccessful server contact
function unsuccessfulContact() {
  mainContent.innerHTML = ""
  message.innerHTML = "Could Not Contact SCOUt Server"
  let retryButton = document.createElement("button")
  retryButton.textContent = "Retry Connecting"
  retryButton.addEventListener("click", () => { establishConnection() })
  mainContent.appendChild(retryButton)
}
