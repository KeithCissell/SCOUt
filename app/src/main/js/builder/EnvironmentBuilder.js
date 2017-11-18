import {pingServer,
        getCurrentState,
        newRandomEnvironment} from '../SCOUtAPI.js'
import {formatEnvironment} from '../environment/EnvironmentFormatter.js'
import {BasicEnvironmentForm} from './FormClasses.js'
import {loadVisualizer} from '../visualizer/Visualizer.js'
import {loadCustomEnvironmentForm} from './CustomEnvironmentForm.js'



// Get Main Element
const main = document.getElementById("main")


function loadEnvironmentBuilderPage() {
  // Setup Environment Builder Page
  main.innerHTML = `
  <div id="home-page">
    <h1 id="home-title">Environment Builder</h1>
    <div id="home-content">
      <form id="environment-form">
        <div id="basic-inputs">
          <label for="environment-name">Environment Name</label>
          <input type="text" id="environment-name" value="My Environment">
          <label for="height">Height</label>
          <input type="number" id="height" value="10" min="10" max="100">
          <label for="width">Width</label>
          <input type="number" id="width" value="10" min="10" max="100">
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

  let customEnvironmentButton = document.getElementById("custom-environment-button")
  customEnvironmentButton.addEventListener("click", () => {
    loadCustomEnvironmentForm()
  })
}

function getBasicInputs() {
  let basicInputs = document.getElementById("basic-inputs").getElementsByTagName("input")
  let name = basicInputs["environment-name"].value
  let height = basicInputs["height"].value
  let width = basicInputs["width"].value
  return new BasicEnvironmentForm(name, height, width)
}

async function buildRandomEnvironment(name, height, width) {
  let nre = await newRandomEnvironment(name, height, width)
  nre.json().then((json) => {
    let environment = formatEnvironment(json)
    console.log(environment)
    loadVisualizer(environment)
  }).catch((err) => { console.log(err) })
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

function textValidation(name, input) {
  if (input) {
    let value = input.value
    if (value == "") {return `${name} cannot be blank`}
    return "valid"
  } else { throw new Error(`INPUT ELEMENT NOT FOUND: ${name}`) }
}

function numberValidation(name, input) {
  if (input) {
    let value = parseInt(input.value)
    let min = parseInt(input.min)
    let max = parseInt(input.max)
    if (!(value >= min && value <= max)) { return `${name} not in range (${input.min}, ${input.max})` }
    return "valid"
  } else { throw new Error(`INPUT ELEMENT NOT FOUND: ${name}`) }
}


export {loadEnvironmentBuilderPage}
