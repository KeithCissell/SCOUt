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
_____getAnomalySeedForm_____
Description
    Get the required form data for an anomaly seed form
Parameters
    anomalyType: the anomaly type of the requested seed form data
*******************************************************************************/
function getAnomalySeedForm(anomalyType) {
  let reqBody = `{
    "anomaly-type": "${anomalyType}"
  }`
  let reqSpecs = {  method: 'POST',
                    headers: reqHeaders,
                    mode: 'cors',
                    cache: 'default',
                    body: reqBody}
  return new Promise((resolve, reject) => {
    fetch(host + '/anomaly_seed_form', reqSpecs).then(function(resp) {
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
    height:   number of cells long the Environment will be
    width:    number of cells wide the Environment will be
*******************************************************************************/
function newRandomEnvironment(name, height, width) {
  let reqBody = `{
    "name": "${name}",
    "height": ${height},
    "width": ${width}
  }`
  console.log(reqBody)
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


/*******************************************************************************
_____buildCustomEnvironment_____
Description
    Build a custom environment based on element seed data
Parameters
    name:     associated name for random Environment
    height:   number of cells long the Environment will be
    width:    number of cells wide the Environment will be
    elements: list of all the element types used
    seeds:    map of element types with their seed data
*******************************************************************************/
function buildCustomEnvironment(name, height, width, elements, seeds) {
  let reqBody = `{
    "name": "${name}",
    "height": ${height},
    "width": ${width},
    "elements": ${JSON.stringify(elements)},
    "seeds": ${JSON.stringify(seeds)}
  }`
  let reqSpecs = {  method: 'POST',
                    headers: reqHeaders,
                    mode: 'cors',
                    cache: 'default',
                    body: reqBody}
  console.log(reqBody)
  return new Promise((resolve, reject) => {
    fetch(host + '/build_custom_environment', reqSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => reject(Response.error(error)))
  })
}

export {pingServer, getElementTypes, getElementSeedForm, getAnomalySeedForm, getCurrentState, newRandomEnvironment, buildCustomEnvironment}
