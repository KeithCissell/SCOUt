window.$ = window.jQuery = require('jquery');
const fetch = require('node-fetch');


// Document Elements
const header = document.getElementById("Header");

// Server Contact variables
let attempts = 60;  // number of attempts to reach server
let buffer = 500;   // time between contact attempts (ms)
let reqTarget = 'http://localhost:8080/ping';
let reqHeaders = new Headers();
let reqSpecs = {  method: 'GET',
                  headers: reqHeaders,
                  mode: 'cors',
                  cache: 'default' };
// let myRequest = new Request('http://localhost:8080/ping', myInit);


// Attempts to establish connection with SCOUt Server
async function establishConnection(callAttempts) {
  let response = await testCall(callAttempts);
  header.innerHTML = "We have contact!!";
  console.log(response);
}

// Returns response once server is contacted or attempts reaches 0
async function testCall(callAttempts) {
  for (i = 0; i < callAttempts; i++) {
    console.log("attempt: " + i)
    let response = await new Promise(resolve => {
      setTimeout(() => {
        fetch(reqTarget, reqSpecs).then(function(resp) {
          resolve(resp)
        })
      }, 1000)
    })
    if (response.ok || i == callAttempts - 1) return response
  }
}

// Trys to ping SCOUt Server
function pingServer() {
  return new Promise(resolve => {
    setTimeout(() => {
      fetch(reqTarget, reqSpecs).then(function(response) {
        resolve(response)
      })
    }, 2000)
  })
}

establishConnection(attempts)
