import {pingServer,
        getCurrentState,
        newRandomEnvironment} from './SCOUtAPI.js'
import {formatEnvironment} from './environment/EnvironmentFormatter.js'
import {loadEnvironmentBuilderPage, loadVisualizerPage} from './PageLoader.js'


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
async function successfulContact() {
  await console.clear()
  document.getElementById("home-title").innerHTML = "Loading..."
  document.getElementById("home-content").innerHTML = ""
  let nre = await newRandomEnvironment("My Environment", 10, 10)
  nre.json().then((json) => {
    let environment = formatEnvironment(json)
    console.log(environment)
    loadVisualizerPage(environment)
  }).catch((err) => { console.log(err) })
}

/*******************************************************************************
_____unsuccessfulContact_____
Description
    Handles unsuccessful server contact
*******************************************************************************/
function unsuccessfulContact() {
  document.getElementById("home-title").innerHTML = "Could Not Contact SCOUt Server"
  document.getElementById("content").innerHTML = ""
  let retryButton = document.createElement("button")
  retryButton.textContent = "Retry Connecting"
  retryButton.addEventListener("click", () => { establishConnection() })
  document.getElementById("content").appendChild(retryButton)
}
