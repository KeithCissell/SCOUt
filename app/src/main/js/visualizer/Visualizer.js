import {roundDecimalX} from '../Utils.js'
import {drawLayer, drawCell, eraseLayer, highlightAnomalies} from './Display.js'
import {addToggle, addSelection, addAnomaly} from './Toolbar.js'
import {addLegendMainItem, addLegendLayerItem, addLegendCellItem} from './Legend.js'
import {loadEnvironmentBuilderPage} from '../builder/EnvironmentBuilder.js'
import {getCustomEnvironmentForm, loadEnvironmentFromForm} from '../builder/CustomEnvironmentForm.js'


// Globals
let environment
let elementSelections
let anomalies
let selectedLayer
let selectedAnomalyType
let selectedCell


/*******************************************************************************
_____loadVisualizer_____
Description
    Main function to load visualization components
Parameters
    targetEnvironment:    Environment object to be loaded into the visualizer
*******************************************************************************/
function loadVisualizer(targetEnvironment) {
  // Load HTML
  main.innerHTML = `
    <div id="navigation">
      <button class="submit-button" id="new-environment">New Environment</button>
      <button class="submit-button" id="regenerate-environment">Regenerate Environment</button>
      <button class="submit-button" id="save-template">Save Template</button>
      <button class="submit-button" id="save-environment">Save Environment</button>
      <h1 id="message"></h1>
      <p id="content"></p>
    </div>
    <div id="toolbar">
      <h1 class="sidebar">Controls</h1>
      <h2 class="sidebar" id="layer-toggles-header"></h2>
      <div class="toolbar-container scroll-box" id="layer-toggles"></div>
      <h2 class="sidebar" id="layer-selector-header"></h2>
      <div class="toolbar-container scroll-box" id="layer-selector"></div>
      <h2 class="sidebar" id="anomaly-selector-header"></h2>
      <div class="toolbar-container scroll-box" id="anomaly-selector"></div>
    </div>
    <svg id="display"></svg>
    <div id="legend">
      <h1 class="sidebar">Legend</h1>
      <h2 class="sidebar" id="legend-environment-title"></h2>
      <table class="legend-table" id="legend-main-table"></table>
      <h2 class="sidebar" id="legend-layer-title"></h2>
      <table class="legend-table" id="legend-selected-layer-table"></table>
      <h2 class="sidebar" id="legend-cell-title"></h2>
      <div class="scroll-box" id="cell-table-holder">
        <table class="legend-table" id="legend-selected-cell-table"></table>
      </div>
    </div>
  `

  // Set globals
  environment = targetEnvironment
  elementSelections = environment.elementTypes.slice(0)
  if (elementSelections.length > 0) elementSelections.unshift("None") // adds "None" to the front of array
  anomalies = environment.anomalyTypes.slice(0)
  if (anomalies.length > 0) anomalies.unshift("None") // adds "None" to the front of array
  selectedLayer = "None"
  selectedAnomalyType = "None"
  selectedCell = "None"

  loadNavigation()
  loadDisplay()
  loadToolbar()
  loadLegend()

  document.getElementById("Elevation-Toggle").click()
  document.getElementById("Grid-Toggle").click()
}

/*******************************************************************************
_____loadNavigation_____
Description
    Load navigation bar
*******************************************************************************/
function loadNavigation() {
  // New Environment
  let newEnvironmentButton = document.getElementById("new-environment")
  newEnvironmentButton.addEventListener("click", () => {
    loadEnvironmentBuilderPage()
  })
  // Regenerate Environment
  let regenerateEnvironmentButton = document.getElementById("regenerate-environment")
  regenerateEnvironmentButton.addEventListener("click", () => {
    let customForm = getCustomEnvironmentForm()
    loadEnvironmentFromForm(customForm)
  })
  // Save Environment Template
  let saveTemplateButton = document.getElementById("save-template")
  saveTemplateButton.addEventListener("click", () => {
    let customForm = getCustomEnvironmentForm()
    console.log(customForm)

  })
  // Save Environment
  let saveEnvironmentButton = document.getElementById("save-environment")
  saveEnvironmentButton.addEventListener("click", () => {
    let environmentString = JSON.stringify(environment)
    console.log(environmentString)
  })
}

/*******************************************************************************
_____loadDisplay_____
Description
    Builds toolbar for adjusting the display
*******************************************************************************/
function loadDisplay() {
  loadElevationLayer()
  loadGrid()
}

/*******************************************************************************
_____loadGrid_____
Description
    Permanantly loads Longitude or Latitude grid lines into the display.
    Throws an Error if it does not find the layer.
*******************************************************************************/
function loadGrid() {
  // Remove Longitude and Latitude form selectable elements
  let longitudeIndex = elementSelections.indexOf("Longitude")
  let latitudeIndex = elementSelections.indexOf("Latitude")
  if (longitudeIndex >= 0 && latitudeIndex >= 0) {
    elementSelections.splice(longitudeIndex, 1)
    elementSelections.splice(latitudeIndex, 1)
  } else {
    throw new Error(`Longitude or Latitude layer not found within Environment`)
  }
  // Draw each cell
  for (let x = 0; x < environment.height; x++) {
    for (let y = 0; y < environment.width; y++) {
      let cellID = "cell-" + x + "-" + y
      let cellData = environment.grid[x][y]

      drawCell(environment.height, environment.width, cellID, x, y, cellData)

      let cell = document.getElementById(cellID)
      cell.addEventListener("click", function() {
        if (this.selected == "false") selectCell(this)
        else deSelectCell(this)
      })
    }
  }
}

/*******************************************************************************
_____selectCell_____
Description
    Selects a cell to highlight and provide info on
Parameters
    DOM object for the cell selected
*******************************************************************************/
function selectCell(cell) {
  cell.selected = "true"
  cell.setAttribute("stroke-width", 3)
  cell.setAttribute("fill-opacity", .6)
  if (selectedCell != "None") deSelectCell(selectedCell)
  selectedCell = cell
  let cellData = environment.grid[cell.xValue][cell.yValue]
  loadLegendCell(cellData)
  // update layer legend
  if (selectedLayer != "None") {
    let cellValue = document.getElementById(selectedLayer + "-value").innerText
    let selected = document.getElementById("legend-layer-Selected-value")
    selected.innerText = cellValue
  }
}

/*******************************************************************************
_____deSelectCell_____
Description
    Deselects the currently selected cell
Parameters
    DOM object for the cell de-selected
*******************************************************************************/
function deSelectCell(cell) {
  let strokeWeight = 0
  let fillOpacity = 0
  let toggle = document.getElementById("Grid-Toggle")
  if (toggle.checked) strokeWeight = 1
  if (cell.getAttribute("fill") != "") fillOpacity = 1
  cell.selected = "false"
  cell.setAttribute("stroke", "black")
  cell.setAttribute("stroke-width", strokeWeight)
  cell.setAttribute("fill-opacity", fillOpacity)
  selectedCell = "None"
  loadLegendCell("None")
  let selected = document.getElementById("legend-layer-Selected-value")
  selected.innerText = "-"
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
  // Loads toggle button for Grid Display
  let toggleID = "Grid-Toggle"
  addToggle("Grid", toggleID)
  let toggle = document.getElementById(toggleID)
  toggle.addEventListener("click", function() {
    let cells = document.getElementsByClassName("display-cell")
    for (let i = 0; i < cells.length; i++) {
      if (toggle.checked) cells[i].setAttribute("stroke-width", 1)
      else cells[i].setAttribute("stroke-width", 0)
    }
  })
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
  if (noneSelection != null) noneSelection.checked = true
  // Load radio buttons for selectable anomalies
  for (let i = 0; i < anomalies.length; i++) {
    let anomalyType = anomalies[i]
    let selectionID = anomalyType + "-Anomaly"
    addAnomaly(anomalyType, selectionID)
    let anomaly = document.getElementById(selectionID)
    anomaly.addEventListener("click", function() {
      displayAnomalyType(this.value)
      if (selectedCell != "None") selectCell(selectedCell)
    })
  }
  let noneAnomaly = document.getElementById("None-Anomaly")
  if (noneAnomaly != null) noneAnomaly.checked = true
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

/*******************************************************************************
_____displayAnomalyType_____
Description
    Displays cells holding given anomaly type
Parameters
    anomalyType:   anomaly type to be displayed
*******************************************************************************/
function displayAnomalyType(anomalyType) {
  selectedAnomalyType = anomalyType
  if (anomalyType != "None") {
    let anomalyCells = environment.extractAnomalyType(anomalyType)
    highlightAnomalies(anomalyCells)
  } else highlightAnomalies([])
}

/*******************************************************************************
_____loadLegend_____
Description
    Loads legend to display information about the Environment
*******************************************************************************/
function loadLegend() {
  // Load Main section
  let legendEnvironmentTitle = document.getElementById("legend-environment-title")

  legendEnvironmentTitle.innerText = environment.name
  addLegendMainItem("Dimensions", environment.height + " X " + environment.width)
  let elevationLayer = environment.extractLayer("Elevation")
  let elevationJson = elevationLayer.toJson()
  let elevationMin = Math.min.apply(null, elevationJson.values)
  let elevationMax = Math.max.apply(null, elevationJson.values)
  addLegendMainItem("Min Elevation", roundDecimalX(elevationMin, 3) + " " + elevationLayer.unit)
  addLegendMainItem("Max Elevation", roundDecimalX(elevationMax, 3) + " " + elevationLayer.unit)
  // Load legend layer <<<and selected cell>>> section<<<s>>>
  loadLegendLayer(selectedLayer)
  loadLegendCell(selectedCell)
}

/*******************************************************************************
_____loadLegendLayer_____
Description
    Loads selected layer info into legend
Parameters
    layerName:  the element type of the layer selected
*******************************************************************************/
function loadLegendLayer(layerName) {
  let legendLayerTitle = document.getElementById("legend-layer-title")
  let legendSelectedLayerTable = document.getElementById("legend-selected-layer-table")
  legendSelectedLayerTable.innerHTML = ""
  let unit = ""
  let min = "-"
  let max = "-"
  let selected = "-"
  // let average = "-"
  if (layerName != "None") {
    legendLayerTitle.innerText = layerName
    let layer = environment.extractLayer(layerName)
    let layerJson = layer.toJson()
    unit = layerJson.unit
    min = roundDecimalX(Math.min.apply(null, layerJson.values), 3)
    max = roundDecimalX(Math.max.apply(null, layerJson.values), 3)
    let selectedValue = document.getElementById(layerName + "-value")
    if (selectedValue != null) selected = selectedValue.innerText
    // average =
  } else {
    legendLayerTitle.innerText = "No Layer Selected"
  }
  addLegendLayerItem("Min", min + " " + unit)
  addLegendLayerItem("Max", max + " " + unit)
  addLegendLayerItem("Selected", selected)
  // addLegendLayerItem("Average", average)
}

/*******************************************************************************
_____loadLegendCell_____
Description
    Loads selected cell info into legend
Parameters
    cellData:  cell class object or "None"
*******************************************************************************/
function loadLegendCell(cellData) {
  let legendCellTitle = document.getElementById("legend-cell-title")
  let legendSelectedCellTable = document.getElementById("legend-selected-cell-table")

  legendSelectedCellTable.innerHTML = ""
  if (cellData != "None") {
    legendCellTitle.innerText = `Cell (${cellData.x}, ${cellData.y})`
    cellData.elements.forEach(element => {
      let name = element.name
      let value = roundDecimalX(element.value, 4) + " " + element.unit
      addLegendCellItem(name, value)
    })
  } else {
    legendCellTitle.innerText = "No Cell Selected"
  }
}

export {loadVisualizer}
