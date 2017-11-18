import {pingServer,
        getCurrentState,
        newRandomEnvironment} from './SCOUtAPI.js'
import {formatEnvironment} from './environment/EnvironmentFormatter.js'
import {buildRandomEnvironment} from './builder/EnvironmentBuilder.js'
import {loadVisualizer} from './visualizer/Visualizer.js'


// Get Main Element
const main = document.getElementById("main")


function loadEnvironmentBuilderPage() {
  // Setup Environment Builder Page
  main.innerHTML = `
  <div id="home-page">
    <h1 id="home-title">Environment Builder</h1>
    <div id="home-content">
      <form>
        <div id="basic-inputs">
          <label for="environment-name">Environment Name</label>
          <input type="text" id="environment-name" value="My Environment">
          <label for="height">Height</label>
          <input type="number" id="height" value="10" min="10" max="100">
          <label for="width">Width</label>
          <input type="number" id="width" value="10" min="10" max="100">
        </div>
        <div id="custom-inputs">
        </div>
      </form>
    </div>
    <div id="submit-buttons">
      <button class="submit-button" id="random-environment-button">Generate Random Environment</button>
      <button class="submit-button" id="custom-environment-button">Build Custom Environment</button>
    </div>
  </div>
  `

  // Add event listeners
  let randomEnvironmentButton = document.getElementById("random-environment-button")
  randomEnvironmentButton.addEventListener("click", () => {
    if (checkBasicInputs()) {
      let form = getBasicInputs()
      buildRandomEnvironment(form.name, form.height, form.width)
    }
  })
}

function checkBasicInputs() {
  let valid = true
  let basicInputs = document.getElementById("basic-inputs").getElementsByTagName("input")

  let name = basicInputs["environment-name"]
  let nameValidation = textValidation("environment-name", name)
  if (nameValidation != "valid") {
    alert(nameValidation)
    valid = false
  }

  let height = basicInputs["height"]
  let heightValidation = numberValidation("height", height)
  if (heightValidation != "valid") {
    alert(heightValidation)
    valid = false
  }

  let width = basicInputs["width"]
  let widthValidation = numberValidation("width", width)
  if (widthValidation != "valid") {
    alert(widthValidation)
    valid = false
  }

  return valid
}

function getBasicInputs() {
  let basicInputs = document.getElementById("basic-inputs").getElementsByTagName("input")
  let name = basicInputs["environment-name"].value
  let height = basicInputs["height"].value
  let width = basicInputs["width"].value
  return new BasicEnvironmentForm(name, height, width)
}

function loadVisualizerPage(environment) {
  main.innerHTML = `
    <div id="navigation">
      <button class="submit-button" id="new-environment">New Environment</button>
      <h1 id="message"></h1>
      <p id="content"></p>
    </div>
    <div id="toolbar">
      <h1 class="sidebar">Controls</h1>
      <h2 class="sidebar" id="layer-toggles-header"></h2>
      <div class="toolbar-container" id="layer-toggles"></div>
      <h2 class="sidebar" id="layer-selector-header"></h2>
      <div class="toolbar-container" id="layer-selector"></div>
    </div>
    <svg id="display"></svg>
    <div id="legend">
      <h1 class="sidebar">Legend</h1>
      <h2 class="sidebar" id="legend-environment-title"></h2>
      <table class="legend-table" id="legend-main-table"></table>
      <h2 class="sidebar" id="legend-layer-title"></h2>
      <table class="legend-table" id="legend-selected-layer-table"></table>
      <h2 class="sidebar" id="legend-cell-title"></h2>
      <table class="legend-cell-table" id="legend-selected-cell-table"></table>
    </div>
  `
  loadVisualizer(environment)
}

export {loadVisualizerPage, loadEnvironmentBuilderPage}
