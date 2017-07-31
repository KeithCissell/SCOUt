// Document Elements
const header = document.getElementById("header")
const toolbar = document.getElementById("toolbar")
const currentLayerName = document.getElementById("current-layer-name")
const main = document.getElementById("main")
const message = document.getElementById("message")
const mainContent = document.getElementById("content")

// Globals
let layers
let elementTypes
let currentLayerIndex = 0

function loadEnvironmentDisplay(environment) {
  layers = environment.layers
  elementTypes = environment.elementTypes

  loadToolbar(elementTypes, layers)
  if (!layers.empty) displayLayer(currentLayerIndex)
  else currentLayerName.innerText = "!!No Layers Found!!"
}

function loadToolbar(elementTypes, layers) {
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
  for (let e in elementTypes) {
    let elementType = elementTypes[e]

  }
}

function displayLayer(index) {
  currentLayerIndex = index
  let layer = layers[index]
  currentLayerName.innerText = layer.elementType
}

function switchLayer(newIndex) {
  if (newIndex < 0) displayLayer(layers.length - 1)
  else if (newIndex > layers.length) displayLayer(0)
  else displayLayer(newIndex)
}

export {loadEnvironmentDisplay}
