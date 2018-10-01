import {pingServer,
        getCurrentState,
        newRandomEnvironment,
        getEnvironmentFileList,
        getEnvironmentFile,
        getTemplateFileList,
        getTemplateFile,
        getOperationFileList,
        getOperationFile} from '../SCOUtAPI.js'
import {formatEnvironment} from '../environment/EnvironmentFormatter.js'
import {BasicEnvironmentForm} from './FormClasses.js'
import {loadVisualizer} from '../visualizer/Visualizer.js'
import {loadCustomEnvironmentForm} from './CustomEnvironmentForm.js'
import {checkBasicInputs} from './FormValidators.js'



// Get Main Element
const main = document.getElementById("main")

/*******************************************************************************
_____loadEnvironmentBuilderPage_____
Description
    Loads the basic environment builder parameters
    User can:
        1. build a random environment
        2. build a custom environment
Parameters
    name:     associated name for random Environment
    height:   number of cells hgih the Environment will be
    width:    number of cells wide the Environment will be
*******************************************************************************/
function loadEnvironmentBuilderPage(name = "My Environment", height = "50", width = "50") {
  // Setup Environment Builder Page
  main.innerHTML = `
  <div id="home-page">
    <h1 id="home-title">Environment Builder</h1>
    <div id="home-content">
      <form id="environment-form">
        <div id="basic-inputs">
          <label for="environment-name">Environment Name</label>
          <input class="basic-input" type="text" id="environment-name">
          <label for="height">Height</label>
          <input class="basic-input" type="number" id="height" min="10" max="100">
          <label for="width">Width</label>
          <input class="basic-input" type="number" id="width" min="10" max="100">
        </div>
        <div id="custom-inputs">
          <h3 id="custom-inputs-title"></h3>
          <div id="custom-inputs-content" class="scroll-box rounded-border"></div>
        </div>
      </form>
    </div>
    <div id="submit-buttons">
      <button class="submit-button" id="random-environment-button">Random Env</button>
      <button class="submit-button" id="custom-environment-button">Custom Env</button>
      <button class="submit-button" id="load-environment-button">Load Env</button>
      <button class="submit-button" id="load-environment-template-button">Load Env Template</button>
      <button class="submit-button" id="load-operation-button">Load Operation</button>
    </div>
  </div>
  `
  // Set default input values
  document.getElementById("environment-name").value = name
  document.getElementById("height").value = height
  document.getElementById("width").value = width

  // Add event listeners
  // Random Environment
  document.getElementById("random-environment-button").addEventListener("click", () => {
    if (checkBasicInputs()) {
      let form = getBasicInputs()
      buildRandomEnvironment(form.name, form.height, form.width)
    }
  })
  // Custom Environment
  document.getElementById("custom-environment-button").addEventListener("click", () => { loadCustomEnvironmentForm() })
  // Load Environment
  document.getElementById("load-environment-button").addEventListener("click", () => { loadFileLists("environment") })
  // Load Environment Template
  document.getElementById("load-environment-template-button").addEventListener("click", () => { loadFileLists("template") })
  // Load Operation
  document.getElementById("load-operation-button").addEventListener("click", () => {loadFileLists("operation")})
}

/*******************************************************************************
_____loadFileLists_____
Description
    Display files that can be chosen from
*******************************************************************************/
async function loadFileLists(fileType) {
  // capture DOM elements
  document.getElementById("home-content").innerHTML = await `
  <div id="files" class="scroll-box rounded-border">
    <ul id="file-list"></ul>
  </div>
  `
  let fileList = await document.getElementById("file-list")
  let fileNames = await []
  if (fileType == "environment") {
    let filesJson = await getEnvironmentFileList()
    await filesJson.json().then((json) => {
      console.log(json)
    })
    // await for (fileName in filesJson) {}
  } else if (fileType == "template") {
    let filesJson = await getTemplateFileList()
    await filesJson.json().then((json) => {
      console.log(json)
    })
    // await for (fileName in filesJson) {}
  } else if (fileType == "operation") {
    let filesJson = await getOperationFileList()
    await filesJson.json().then((json) => {
      console.log(json)
    })
    // await for (fileName in filesJson) {}
  }

  // Populate Submit Button Div
  document.getElementById("submit-buttons").innerHTML = await `<button class="submit-button" id="back-button">Back</button>`
  await document.getElementById("back-button").addEventListener("click", () => { loadEnvironmentBuilderPage() })
}

/*******************************************************************************
_____getBasicInputs_____
Description
    Get basic input values from form fields
*******************************************************************************/
function getBasicInputs() {
  let basicInputs = document.getElementById("basic-inputs").getElementsByTagName("input")
  let name = basicInputs["environment-name"].value
  let height = basicInputs["height"].value
  let width = basicInputs["width"].value
  return new BasicEnvironmentForm(name, height, width)
}

/*******************************************************************************
_____buildRandomEnvironment_____
Description
    Build a new random environment
Parameters
    name:     associated name for random Environment
    height:   number of cells hgih the Environment will be
    width:    number of cells wide the Environment will be
*******************************************************************************/
async function buildRandomEnvironment(name, height, width) {
  let nre = await newRandomEnvironment(name, height, width)
  nre.json().then((json) => {
    let environment = formatEnvironment(json)
    console.log(environment)
    loadVisualizer(environment)
  }).catch((err) => { console.log(err) })
}

export {loadEnvironmentBuilderPage, getBasicInputs}
