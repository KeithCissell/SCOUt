import {drawLayer} from './Display.js'


// Document Elements
const header = document.getElementById("header")
const toolbar = document.getElementById("toolbar")
const currentLayerName = document.getElementById("current-layer-name")
const main = document.getElementById("main")
const message = document.getElementById("message")
const mainContent = document.getElementById("content")

// Globals
let environment
let elementTypes
let currentLayerIndex = 0

function loadVisualizer(targetEnvironment) {
  environment = targetEnvironment
  elementTypes = environment.elementTypes
  loadToolbar()
  displayLayer(currentLayerIndex)
}

function loadToolbar() {
  let previousLayerButton = document.createElement("button")
  previousLayerButton.textContent = " <<< "
  previousLayerButton.addEventListener("click", () => {
    switchLayer(currentLayerIndex - 1)
  })
  toolbar.insertBefore(previousLayerButton, currentLayerName)
  let nextLayerButton = document.createElement("button")
  nextLayerButton.textContent = " >>> "
  nextLayerButton.addEventListener("click", () => {
    switchLayer(currentLayerIndex + 1)
  })
  toolbar.appendChild(nextLayerButton)
}

function switchLayer(newIndex) {
  if (newIndex < 0) displayLayer(elementTypes.length - 1)
  else if (newIndex >= elementTypes.length) displayLayer(0)
  else displayLayer(newIndex)
}

function displayLayer(index) {
  currentLayerIndex = index
  let elementType = elementTypes[index]
  let layer = environment.extractLayer(elementType)
  currentLayerName.innerText = layer.elementType
  message.innerHTML = ""
  let contours = ["Elevation", "Latitude", "Longitude"]
  if (contours.indexOf(elementType) >= 0) drawLayer(layer, 4, 0, 0, true)
  else drawLayer(layer, 4, 0, .5, false)
}


export {loadVisualizer}
