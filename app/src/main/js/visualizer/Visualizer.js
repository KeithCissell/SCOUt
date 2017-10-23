import {roundDecimalX} from '../Utils.js'
import {drawLayer, eraseLayer} from './Display.js'
import {addToggle, addSelection} from './Toolbar.js'
import {addLegendMainItem, addLegendLayerItem} from './Legend.js'


// Document Elements
const message = document.getElementById("message")
const legendEnvironmentTitle = document.getElementById("legend-environment-title")
const legendLayerTitle = document.getElementById("legend-layer-title")
const legendCurrentLayerTable = document.getElementById("legend-current-layer-table")

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
  elementSelections = environment.elementTypes.slice(0)
  elementSelections.unshift("None") // adds "None" to the front of array

  loadDisplay()
  loadToolbar()
  loadLegend()

  document.getElementById("Elevation-Toggle").click()
  message.innerHTML = ""
}

/*******************************************************************************
_____loadDisplay_____
Description
    Builds toolbar for adjusting the display
*******************************************************************************/
function loadDisplay() {
  loadGrid("Latitude", environment.width, 0, 0, 0, true)
  loadGrid("Longitude", environment.length, 0, 0, 0, true)
  loadElevationLayer()
}

/*******************************************************************************
_____loadGrid_____
Description
    Permanantly loads Longitude or Latitude grid lines into the display.
    Throws an Error if it does not find the layer.
Parameters
    layerName (string)  : elementType associated to layer in Environment
    threshold (int)     : how many contour-lines should be generated for display
    hue (int) [0,359]   : primary color between contour-lines
    saturation (flt) [0.0,1.0]
    opacity (flt) [0.0,1.0]     : opacity for the color between contour-lines
    lines (boolean)     : should contour-lines appear
*******************************************************************************/
function loadGrid(layerName, threshold, hue, saturation, opacity, lines) {
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
      loadLegendLayer(this.value)
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

function loadLegend() {
  // Load Main section
  legendEnvironmentTitle.innerText = environment.name
  addLegendMainItem("Dimensions", environment.length + " X " + environment.width)
  let elevationLayer = environment.extractLayer("Elevation")
  let elevationJson = elevationLayer.toJson()
  let elevationMin = Math.min.apply(null, elevationJson.values)
  let elevationMax = Math.max.apply(null, elevationJson.values)
  addLegendMainItem("Min Elevation", roundDecimalX(elevationMin, 3) + " " + elevationLayer.unit)
  addLegendMainItem("Max Elevation", roundDecimalX(elevationMax, 3) + " " + elevationLayer.unit)
  // Load legend layer <<<and selected cell>>> section<<<s>>>
  loadLegendLayer(selectedLayer)
}

function loadLegendLayer(layerName) {
  legendCurrentLayerTable.innerHTML = ""
  let unit = ""
  let min = "-"
  let max = "-"
  // let average = "-"
  if (layerName != "None") {
    legendLayerTitle.innerText = layerName
    let layer = environment.extractLayer(layerName)
    let layerJson = layer.toJson()
    unit = layerJson.unit
    min = roundDecimalX(Math.min.apply(null, layerJson.values), 3)
    max = roundDecimalX(Math.max.apply(null, layerJson.values), 3)
    // average =
  } else {
    legendLayerTitle.innerText = "No Layer Selected"
  }
  addLegendLayerItem("Min", min + " " + unit)
  addLegendLayerItem("Max", max + " " + unit)
  // addLegendLayerItem("Average", average)
}

export {loadVisualizer}
