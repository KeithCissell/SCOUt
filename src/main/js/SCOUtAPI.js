// SCOUt server contact API

// Request details
let reqHeaders = new Headers();
let getSpecs = {  method: 'GET',
                  headers: reqHeaders,
                  mode: 'cors',
                  cache: 'default' }

// Trys to ping server
function pingServer() {
  return new Promise(resolve => {
    fetch('http://localhost:8080/ping', getSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => resolve(Response.error(error)))
  })
}

// Gets the current state of the environment
function getCurrentState() {
  return new Promise(resolve => {
    fetch('http://localhost:8080/current_state', getSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => resolve(Response.error(error)))
  })
}

// Get a new random environment
function newRandomEnvironment(name, length, width) {
  let reqBody = `{
    "name": "${name}",
    "length": "${length}",
    "width": "${width}"
  }`
  let reqSpecs = {  method: 'POST',
                    headers: reqHeaders,
                    mode: 'cors',
                    cache: 'default',
                    body: reqBody}
  return new Promise((resolve, reject) => {
    fetch('http://localhost:8080/new_random_environment', reqSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => reject(Response.error(error)))
  })
}

export {pingServer, getCurrentState, newRandomEnvironment}
