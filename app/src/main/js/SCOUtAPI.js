// SCOUt server contact API

// Host server
const host = 'http://localhost:8080'

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
    fetch(host + '/ping', getSpecs).then(function(resp) {
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
    fetch(host + '/element_types', getSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => resolve(Response.error(error)))
  })
}

/*******************************************************************************
_____getElementSeedForm_____
Description
    Get the required form data for an element seed form
Parameters
    elementType: the element type of the requested seed form data
*******************************************************************************/
function getElementSeedForm(elementType) {
  let reqBody = `{
    "element-type": "${elementType}"
  }`
  let reqSpecs = {  method: 'POST',
                    headers: reqHeaders,
                    mode: 'cors',
                    cache: 'default',
                    body: reqBody}
  return new Promise((resolve, reject) => {
    fetch(host + '/element_seed_form', reqSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => reject(Response.error(error)))
  })
}

/*******************************************************************************
_____getCurrentState_____
Description
    Gets the current state of the environment
*******************************************************************************/
function getCurrentState() {
  return new Promise(resolve => {
    fetch(host + '/current_state', getSpecs).then(function(resp) {
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
    fetch(host + '/new_random_environment', reqSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => reject(Response.error(error)))
  })
}

export {pingServer, getElementTypes, getElementSeedForm, getCurrentState, newRandomEnvironment}
