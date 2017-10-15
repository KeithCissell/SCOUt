import {drawLayer, eraseLayer} from './Display.js'
import {addToggle, addSelection} from './Toolbar.js'


// Document Elements
const header = document.getElementById("header")
const main = document.getElementById("main")
const message = document.getElementById("message")
const mainContent = document.getElementById("content")
const toolbar = document.getElementById("toolbar")

// Globals
let environment
let elementSelections
let selectedLayer = "None"

// Main function to load display and interactive tools
function loadVisualizer(targetEnvironment) {
  environment = targetEnvironment
  elementSelections = environment.elementTypes
  elementSelections.unshift("None") // adds "None" to the front of array
  loadDisplayTools()
  displayLayer(selectedLayer)
  message.innerHTML = ""
}

// Builds toolbar for adjusting the display
function loadDisplayTools() {
  loadPermanentLayer("Latitude", 10, 0, 0, 0, true)
  loadPermanentLayer("Longitude", 10, 0, 0, 0, true)
  loadToggleLayer("Elevation", 7, 0, 0, .2, true, true)
  document.getElementById("Elevation-Toggle").click()
  loadLayerSelector()
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
  let index = elementSelections.indexOf(layerName)
  if (index >= 0) {
    let elementType = elementSelections[index]
    let layer = environment.extractLayer(elementType)
    drawLayer(layer, threshold, hue, saturation, opacity, lines, false)
    elementSelections.splice(index, 1)
  } else {
    throw new Error(`${layerName} layer not found within Environment`)
  }
}

/*
_____loadToggleLayer_____
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
    bottom (boolean)    : insert layer at the behind all existing layers
*/
function loadToggleLayer(layerName, threshold, hue, saturation, opacity, lines, bottom) {
  let index = elementSelections.indexOf(layerName)
  if (index >= 0) {
    // Draw layer and remove it from list of remaining layers
    let elementType = elementSelections[index]
    let layer = environment.extractLayer(elementType)
    elementSelections.splice(index, 1)

    // Add toggle to toolbar and event listner
    let toggleID = elementType + "-Toggle"
    addToggle(elementType, toggleID)
    let toggle = document.getElementById(toggleID)
    toggle.addEventListener("click", function() {
      if (this.checked) drawLayer(layer, threshold, hue, saturation, opacity, lines, bottom)
      else eraseLayer(elementType)
    })
  } else {
    // Throw error if layer was not found
    throw new Error(`${layerName} layer not found within Environment`)
  }
}

// Loads toolbar for switching the displayed layer
function loadLayerSelector() {
  for (let i = 0; i < elementSelections.length; i++) {
    let elementType = elementSelections[i]
    let selectionID = elementType + "-Selection"
    addSelection(elementType, selectionID)
    let selection = document.getElementById(selectionID)
    selection.addEventListener("click", function() {
      displayLayer(this.value)
    })
  }
  let noneSelection = document.getElementById("None-Selection")
  noneSelection.checked = true
}

// Requests to display a layer by type
function displayLayer(elementType) {
  if (selectedLayer != "None") eraseLayer(selectedLayer)
  selectedLayer = elementType
  if (elementType != "None") {
    let layer = environment.extractLayer(elementType)
    drawLayer(layer, 4, 220, .5, 0.3, false)
  }
}

export {loadVisualizer}
