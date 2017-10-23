const layerToggles = document.getElementById("layer-toggles")
const layerSelector = document.getElementById("layer-selector")
const layerTogglesHeader = document.getElementById("layer-toggles-header")
const layerSelectorHeader = document.getElementById("layer-selector-header")

/*******************************************************************************
_____addToggle_____
Description
    Adds a toggle button to allow turning display of a layer on/off
Parameters
    layerName:    element type of the layer
    toggleID:     associated ID that will be given to the DOM element
*******************************************************************************/
function addToggle(layerName, toggleID) {
  if (layerTogglesHeader.innerText == "") layerTogglesHeader.innerText = "Toggle Layers"
  let newLable = document.createElement("label")
  newLable.setAttribute("class", "switch")
  newLable.setAttribute("for", toggleID)
  let newToggle = document.createElement("input")
  newToggle.setAttribute("type", "checkbox")
  newToggle.setAttribute("id", toggleID)
  let newSlider = document.createElement("div")
  newSlider.setAttribute("class", "slider round")
  let newSpan = document.createElement("span")
  newSpan.setAttribute("class", "toggle-text")
  newSpan.innerText = layerName
  // Add into DOM
  newLable.appendChild(newToggle)
  newLable.appendChild(newSlider)
  newLable.appendChild(newSpan)
  layerToggles.appendChild(newLable)
}

/*******************************************************************************
_____addSelection_____
Description
    Adds a radio button to allow selection of a given element layer
Parameters
    layerName:    element type of the layer
    selectionID:  associated ID that will be given to the DOM element
*******************************************************************************/
function addSelection(layerName, selectionID) {
  if (layerSelectorHeader.innerText == "") layerSelectorHeader.innerText = "Current Layer"
  let newLable = document.createElement("label")
  newLable.setAttribute("class", "radio inline")
  newLable.setAttribute("for", selectionID)
  let newSelection = document.createElement("input")
  newSelection.setAttribute("type", "radio")
  newSelection.setAttribute("id", selectionID)
  newSelection.setAttribute("name", "Selection")
  newSelection.setAttribute("value", layerName)
  let newSpan = document.createElement("span")
  newSpan.innerText = layerName
  // Add into DOM
  newLable.appendChild(newSelection)
  newLable.appendChild(newSpan)
  layerSelector.appendChild(newLable)
}

export {addToggle, addSelection}
