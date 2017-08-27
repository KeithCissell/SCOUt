const d3 = require('d3')
const hsv = require('d3-hsv')

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

function loadEnvironmentDisplay(targetEnvironment) {
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
  for (let e in elementTypes) {
    let elementType = elementTypes[e]

  }
}

function switchLayer(newIndex) {
  if (newIndex < 0) displayLayer(elementTypes.length - 1)
  else if (newIndex >= elementTypes.length) displayLayer(0)
  else displayLayer(newIndex)
}


async function displayLayer(index) {
  currentLayerIndex = index
  console.log(index)
  let elementType = elementTypes[index]
  console.log(elementType)
  let layer = await environment.extractLayer(elementType)
  let layerJson = await layer.toJson()
  currentLayerName.innerText = layer.elementType
  message.innerHTML = ""






  var i0 = hsv.interpolateHsvLong(hsv.hsv(120, 1, 0.65), hsv.hsv(60, 1, 0.90)),
      i1 = hsv.interpolateHsvLong(hsv.hsv(60, 1, 0.90), hsv.hsv(0, 0, 0.95)),
      interpolateTerrain = function(t) { return t < 0.5 ? i0(t * 2) : i1((t - 0.5) * 2); },
      color = d3.scaleSequential(interpolateTerrain).domain([90, 190]);


  var n = layerJson.width
  let m = layerJson.length

  var canvas = d3.select("#canvas")
      .attr("width", n)
      .attr("height", m);
  // console.log(canvas)

  var context = canvas.node().getContext("2d"),
      image = context.createImageData(n, m);

  for (var j = 0, k = 0, l = 0; j < m; ++j) {
    for (var i = 0; i < n; ++i, ++k, l += 4) {
      var c = d3.rgb(color(layerJson.values[k]));
      image.data[l + 0] = c.r;
      image.data[l + 1] = c.g;
      image.data[l + 2] = c.b;
      image.data[l + 3] = 255;
    }
  }

  context.putImageData(image, 0, 0);





}


export {loadEnvironmentDisplay}
