import {drawHeatmap,
        drawContourPlot} from './Display.js'


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
  loadToolbar(elementTypes)
  displayLayer(currentLayerIndex)
}

function loadToolbar(elementTypes) {
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
  // drawContourPlot(layer)
  if (elementType == "Elevation") drawContourPlot(layer)
  else drawHeatmap(layer)
}


export {loadVisualizer}
