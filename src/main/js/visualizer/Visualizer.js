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
let selectedLayer = "None" // initialize as "None" to display no layer

/*******************************************************************************
_____loadVisualizer_____
Description
    Main function to load visualization components
Parameters
    targetEnvironment:    Environment object to be loaded into the visualizer
*******************************************************************************/
function loadVisualizer(targetEnvironment) {
  environment = targetEnvironment
  elementSelections = environment.elementTypes
  elementSelections.unshift("None") // adds "None" to the front of array

  loadDisplay()
  loadToolbar()

  document.getElementById("Elevation-Toggle").click()
  message.innerHTML = ""
}

/*******************************************************************************
_____loadDisplay_____
Description
    Builds toolbar for adjusting the display
*******************************************************************************/
function loadDisplay() {
  loadPermanentLayer("Latitude", 10, 0, 0, 0, true)
  loadPermanentLayer("Longitude", 10, 0, 0, 0, true)
  loadElevationLayer()
}

/*******************************************************************************
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
*******************************************************************************/
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

/*******************************************************************************
_____loadToggleLayer_____
Description
    Attempts to find a layer in Environment and call drawLayer().
    Throws an Error if it does not find the layer.
*******************************************************************************/
function loadElevationLayer() {
  let elementType = "Elevation"
  let index = elementSelections.indexOf(elementType)
  if (index >= 0) {
    // Draw layer and remove it from list of remaining layers
    let elementType = elementSelections[index]
    let layer = environment.extractLayer(elementType)
    elementSelections.splice(index, 1)
    // Loads toggle button Elevation layer
    let toggleID = elementType + "-Toggle"
    addToggle(elementType, toggleID)
    let toggle = document.getElementById(toggleID)
    toggle.addEventListener("click", function() {
      if (this.checked) drawLayer(layer, 7, 0, 0, .2, true, true)
      else eraseLayer(elementType)
    })
  } else {
    // Throw error if layer was not found
    throw new Error(`${layerName} layer not found within Environment`)
  }
}

/*******************************************************************************
_____loadToolbar_____
Description
    Loads toolbar for manipulating the display
*******************************************************************************/
function loadToolbar() {
  // Load radio buttons for selectable layers
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

/*******************************************************************************
_____displayLayer_____
Description
    Displays a layer by type
Parameters
    elementType:   element type of the layer to be displayed
*******************************************************************************/
function displayLayer(elementType) {
  if (selectedLayer != "None") eraseLayer(selectedLayer)
  selectedLayer = elementType
  if (elementType != "None") {
    let layer = environment.extractLayer(elementType)
    drawLayer(layer, 4, 100, .5, 0.3, false)
  }
}

export {loadVisualizer}
