// SCOUt server contact API

// Request details
let reqHeaders = new Headers();
let getSpecs = {  method: 'GET',
                  headers: reqHeaders,
                  mode: 'cors',
                  cache: 'default' }

/*******************************************************************************
_____pingServer_____
Description
    Trys to ping SCOUt server
*******************************************************************************/
function pingServer() {
  return new Promise(resolve => {
    fetch('http://localhost:8080/ping', getSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => resolve(Response.error(error)))
  })
}

/*******************************************************************************
_____getElementTypes_____
Description
    Gets the different element types that can be used in an environment
*******************************************************************************/
function getElementTypes() {
  return new Promise(resolve => {
    fetch('http://localhost:8080/element_types', getSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => resolve(Response.error(error)))
  })
}

/*******************************************************************************
_____getCurrentState_____
Description
    Gets the current state of the environment
*******************************************************************************/
function getCurrentState() {
  return new Promise(resolve => {
    fetch('http://localhost:8080/current_state', getSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => resolve(Response.error(error)))
  })
}

/*******************************************************************************
_____newRandomEnvironment_____
Description
    Get a new random environment
Parameters
    name:     associated name for random Environment
    length:   number of cells long the Environment will be
    width:    number of cells wide the Environment will be
*******************************************************************************/
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

export {pingServer, getElementTypes, getCurrentState, newRandomEnvironment}
