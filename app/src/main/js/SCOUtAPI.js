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
_____getTerrainModificationTypes_____
Description
    Gets the different terrain modification types that can be applied to an environment
*******************************************************************************/
function getTerrainModificationTypes() {
  return new Promise(resolve => {
    fetch(host + '/terrain_modification_types', getSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => resolve(Response.error(error)))
  })
}

/*******************************************************************************
_____getAnomalyTypes_____
Description
    Gets the different anomaly types that can be used in an environment
*******************************************************************************/
function getAnomalyTypes() {
  return new Promise(resolve => {
    fetch(host + '/anomaly_types', getSpecs).then(function(resp) {
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
_____getTerrainModificationForm_____
Description
    Get the required form data for a terrain modification form
Parameters
    terrainModificationType: the terrain modification type of the requested seed form data
*******************************************************************************/
function getTerrainModificationForm(terrainModificationType) {
  let reqBody = `{
    "terrain-modification-type": "${terrainModificationType}"
  }`
  let reqSpecs = {  method: 'POST',
                    headers: reqHeaders,
                    mode: 'cors',
                    cache: 'default',
                    body: reqBody}
  return new Promise((resolve, reject) => {
    fetch(host + '/terrain_modification_form', reqSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => reject(Response.error(error)))
  })
}

/*******************************************************************************
_____getAnomalyForm_____
Description
    Get the required form data for an anomaly form
Parameters
    anomalyType: the anomaly type of the requested seed form data
*******************************************************************************/
function getAnomalyForm(anomalyType) {
  let reqBody = `{
    "anomaly-type": "${anomalyType}"
  }`
  let reqSpecs = {  method: 'POST',
                    headers: reqHeaders,
                    mode: 'cors',
                    cache: 'default',
                    body: reqBody}
  return new Promise((resolve, reject) => {
    fetch(host + '/anomaly_form', reqSpecs).then(function(resp) {
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
    formData:       fully jsonified form data
*******************************************************************************/
function buildCustomEnvironment(formData) {
  let reqBody = formData
  let reqSpecs = {  method: 'POST',
                    headers: reqHeaders,
                    mode: 'cors',
                    cache: 'default',
                    body: reqBody}
  return new Promise((resolve, reject) => {
    fetch(host + '/build_custom_environment', reqSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => reject(Response.error(error)))
  })
}

/*******************************************************************************
_____saveEnvironmentTemplate_____
Description
    Saves environment template on the back end
Parameters
    environmentTemplate:       fully jsonified environment template
*******************************************************************************/
function saveEnvironmentTemplate(environmentTemplate) {
  let reqBody = environmentTemplate
  let reqSpecs = {  method: 'POST',
                    headers: reqHeaders,
                    mode: 'cors',
                    cache: 'default',
                    body: reqBody}
  return new Promise((resolve, reject) => {
    fetch(host + '/save_environment_template', reqSpecs).then(function(resp) {
      alert("Saved")
      resolve(resp)
    }).catch(error => reject(Response.error(error)))
  })
}

function saveEnvironment() {
  return new Promise(resolve => {
    fetch(host + '/save_environment', getSpecs).then(function(resp) {
      alert("Saved")
      resolve(resp)
    }).catch(error => resolve(Response.error(error)))
  })
}

// Get Environment Files
function getEnvironmentFileList() {
  return new Promise(resolve => {
    fetch(host + '/get_environment_file_list', getSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => resolve(Response.error(error)))
  })
}
function getEnvironmentFile(fileName) {
  let reqBody = `{"fileName": "${fileName}"}`
  let reqSpecs = {  method: 'POST',
                    headers: reqHeaders,
                    mode: 'cors',
                    cache: 'default',
                    body: reqBody}
  return new Promise((resolve, reject) => {
    fetch(host + '/get_environment_file', reqSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => reject(Response.error(error)))
  })
}

// Get Environment Template Files
function getTemplateFileList() {
  return new Promise(resolve => {
    fetch(host + '/get_template_file_list', getSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => resolve(Response.error(error)))
  })
}
function getTemplateFile(fileName) {
  let reqBody = `{"fileName": "${fileName}"}`
  let reqSpecs = {  method: 'POST',
                    headers: reqHeaders,
                    mode: 'cors',
                    cache: 'default',
                    body: reqBody}
  return new Promise((resolve, reject) => {
    fetch(host + '/get_template_file', reqSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => reject(Response.error(error)))
  })
}

// Get Operation Files
function getOperationFileList() {
  return new Promise(resolve => {
    fetch(host + '/get_operation_file_list', getSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => resolve(Response.error(error)))
  })
}
function getOperationFile(fileName) {
  let reqBody = `{"fileName": "${fileName}"}`
  let reqSpecs = {  method: 'POST',
                    headers: reqHeaders,
                    mode: 'cors',
                    cache: 'default',
                    body: reqBody}
  return new Promise((resolve, reject) => {
    fetch(host + '/get_operation_file', reqSpecs).then(function(resp) {
      resolve(resp)
    }).catch(error => reject(Response.error(error)))
  })
}

export {pingServer, getElementTypes, getAnomalyTypes, getTerrainModificationTypes, getElementSeedForm, getAnomalyForm,
  getTerrainModificationForm, getCurrentState, newRandomEnvironment, buildCustomEnvironment, saveEnvironmentTemplate,
  saveEnvironment, getEnvironmentFileList, getEnvironmentFile, getTemplateFileList, getTemplateFile, getOperationFileList, getOperationFile}
