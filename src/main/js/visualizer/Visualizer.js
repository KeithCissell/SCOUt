import {drawLayer, eraseLayer} from './Display.js'


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
let currentLayerType = "None"

// Main function to load display and interactive tools
function loadVisualizer(targetEnvironment) {
  environment = targetEnvironment
  elementTypes = environment.elementTypes
  elementTypes.unshift("None") // adds "None" to the front of array
  loadDisplayFoundation()
  loadToolbar()
  displayLayer(currentLayerIndex)
  message.innerHTML = ""
}

// Loads/draws Layers that will remain permanent on the display
function loadDisplayFoundation() {
  loadPermanentLayer("Elevation", 7, 0, 0, .2, true)
  loadPermanentLayer("Latitude", 10, 0, 0, 0, true)
  loadPermanentLayer("Longitude", 10, 0, 0, 0, true)
}

/*
_____loadPermanentLayer_____
Description
    Attempts to find a layer in Environment and call drawLayer().
    Throws an Error if it does not find the layer.
Parameters
    layerName (string)  : elementType associated to layer in Environment
    threshold (int)     : how many contour-lines should be generated for display
    hue (int) [0,359]   : primary color between contour-lines
    saturation (flt) [0.0,1.0]
    opacity (flt) [0.0,1.0]     : opacity for the color between contour-lines
    lines (boolean)     : should contour-lines appear
*/
function loadPermanentLayer(layerName, threshold, hue, saturation, opacity, lines) {
  let index = elementTypes.indexOf(layerName)
  if (index >= 0) {
    let elementType = elementTypes[index]
    let layer = environment.extractLayer(elementType)
    drawLayer(layer, threshold, hue, saturation, opacity, lines)
    elementTypes.splice(index, 1)
  } else {
    throw new Error(`${layerName} layer not found within Environment`)
  }
}

// Loads toolbar for switching the displayed layer
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

// Assures that `currentLayerIndex` stays within bounds
function switchLayer(newIndex) {
  if (newIndex < 0) displayLayer(elementTypes.length - 1)
  else if (newIndex >= elementTypes.length) displayLayer(0)
  else displayLayer(newIndex)
}

// Requests to display a layer by index
function displayLayer(index) {
  if (currentLayerType != "None") eraseLayer(currentLayerType)

  let elementType = elementTypes[index]
  currentLayerIndex = index
  currentLayerType = elementType
  currentLayerName.innerText = elementType

  if (currentLayerType != "None") {
    let layer = environment.extractLayer(elementType)
    drawLayer(layer, 4, 220, .5, 0.3, false)
  }
}


export {loadVisualizer}
