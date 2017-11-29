import {pingServer,
        getCurrentState,
        newRandomEnvironment} from './SCOUtAPI.js'
import {loadEnvironmentBuilderPage} from './builder/EnvironmentBuilder.js'


window.$ = window.jQuery = require('jquery')
const fetch = require('node-fetch')

// Establish connection with SCOUt server
document.addEventListener("DOMContentLoaded", () => {
  establishConnection()
})


/*******************************************************************************
_____establishConnection_____
Description
    Attempts to establish connection with SCOUt Server
*******************************************************************************/
async function establishConnection() {
  document.getElementById("home-title").innerHTML = "Attempting to contact SCOUt server..."
  document.getElementById("home-content").innerHTML = ""
  let contactMade = await attemptContact(20, 500)
  if (contactMade) successfulContact()
  else unsuccessfulContact()
}

/*******************************************************************************
_____attemptContact_____
Description
    Returns response once server is contacted or attempts reaches 0
Parameters
    attempts:   number of attempts to reach server
    buffer:     time between contact attempts (ms)
*******************************************************************************/
async function attemptContact(attempts, buffer) {
  for (let i = 0; i < attempts; i++) {
    console.log("Server contact attempt: " + i)
    let response = await new Promise(resolve => {
      setTimeout(() => { resolve(pingServer()) }, buffer)
    })
    if (response.ok) return true
  }
  return false
}

/*******************************************************************************
_____successfulContact_____
Description
    Handles successful server contact
*******************************************************************************/
function successfulContact() {
  console.clear()
  document.getElementById("home-title").innerHTML = "Loading..."
  document.getElementById("home-content").innerHTML = ""
  loadEnvironmentBuilderPage()
}

/*******************************************************************************
_____unsuccessfulContact_____
Description
    Handles unsuccessful server contact
*******************************************************************************/
function unsuccessfulContact() {
  document.getElementById("home-title").innerHTML = "Could Not Contact SCOUt Server"
  document.getElementById("home-content").innerHTML = ""
  let retryButton = document.createElement("button")
  retryButton.textContent = "Retry Connecting"
  retryButton.addEventListener("click", () => { establishConnection() })
  document.getElementById("home-content").appendChild(retryButton)
}
