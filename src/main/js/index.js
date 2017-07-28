import {pingServer,
        newRandomEnvironment} from './SCOUtAPI.js'

window.$ = window.jQuery = require('jquery')
const fetch = require('node-fetch')


// Document Elements
const header = document.getElementById("Header")
const body = document.getElementById("Body")


// Attempts to establish connection with SCOUt Server
async function establishConnection() {
  let contactMade = await attemptContact()
  if (contactMade) successfulContact()
  else unsuccessfulContact()
}

// Returns response once server is contacted or attempts reaches 0
async function attemptContact() {
  let attempts = 60   // number of attempts to reach server
  let buffer = 500    // time between contact attempts (ms)
  for (let i = 0; i < attempts; i++) {
    console.log("attempt: " + i)
    let response = await new Promise(resolve => {
      setTimeout(() => {
        resolve(pingServer());
      }, buffer);
    });
    if (response.ok) return true
  }
  return false
}

// Handles successful server contact
function successfulContact() {
  header.innerHTML = "We have contact!!"
  console.log(newRandomEnvironment("RandomTest", 5, 5))
  let environment = getCurrentState().body()
  body.innerHTML = '${environment}'
}

// Handles unsuccessful server contact
function unsuccessfulContact() {

}

// Establish connection with SCOUt server
establishConnection()
