const d3 = require('d3')
const d3Contour = require('d3-contour')
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

function displayLayer(index) {
  currentLayerIndex = index
  let elementType = elementTypes[index]
  let layer = environment.extractLayer(elementType)
  currentLayerName.innerText = layer.elementType
  message.innerHTML = ""

  if (elementType == "Elevation") drawContourPlot(layer)
  else drawCanvas(layer)
}


function drawCanvas(layer) {
  console.log(layer)
  let layerJson = layerToJson(layer)

  let width = layerJson.width
  let height = layerJson.length
  let values = layerJson.values
  let min = Math.min.apply(null, values)
  let max = Math.max.apply(null, values)

  let i0 = hsv.interpolateHsvLong(hsv.hsv(120, 1, 0.65), hsv.hsv(60, 1, 0.90))
  let i1 = hsv.interpolateHsvLong(hsv.hsv(60, 1, 0.90), hsv.hsv(0, 0, 0.95))
  let interpolateTerrain = function(t) { return t < 0.5 ? i0(t * 2) : i1((t - 0.5) * 2); }
  let color = d3.scaleSequential(interpolateTerrain)
              .domain([min, max])
              //.range(["purple", "red"])

  let canvas = d3.select("#canvas")
      .attr("width", width)
      .attr("height", height)
  // console.log(canvas)

  let context = canvas.node().getContext("2d")
  let image = context.createImageData(width, height)

  for (let i = 0; i < values.length; ++i) {
    let c = d3.rgb(color(values[i]))
    image.data[i * 4 + 0] = c.r
    image.data[i * 4 + 1] = c.g
    image.data[i * 4 + 2] = c.b
    image.data[i * 4 + 3] = 255
  }

  context.putImageData(image, 0, 0)
}

function drawContourPlot(layer) {
  console.log(layer)
  let layerJson = layerToJson(layer)
  // console.log(layerJson)

  let width = layerJson.width
  let height = layerJson.length
  let values = layerJson.values
  let min = Math.min.apply(null, values)
  let max = Math.max.apply(null, values)

  let i0 = hsv.interpolateHsvLong(hsv.hsv(120, 1, 0.65), hsv.hsv(60, 1, 0.90))
  let i1 = hsv.interpolateHsvLong(hsv.hsv(60, 1, 0.90), hsv.hsv(0, 0, 0.95))
  let interpolateTerrain = function(t) { return t < 0.5 ? i0(t * 2) : i1((t - 0.5) * 2) }
  let color = d3.scaleSequential(interpolateTerrain).domain([min, max])

  let canvas = d3.select("#canvas")
      .style("background-color", "white")
      .attr("width", 500)
      .attr("height", 500)

  let contours = d3Contour.contours()
      .size([width, height])
      .thresholds(d3.range(min, max, 2))
      (values);
  // console.log(contours)

  canvas.selectAll("path")
    .data(contours)
    .enter()
      .append("path")
      .attr("d", d3.geoPath(d3.geoIdentity().scale(500 / width)))
      .attr("stroke", "black")
      .attr("stroke-width", "1")
      .attr("fill", "none")
      // .attr("fill", function(d) { return color(d.value); })

}

function layerToJson(layer) {
  let obj = {}
  // Rotate the object 90 degrees counter-clockwise for visualization tool: (x, y) = (y, -x)
  obj.width = layer.length
  obj.length = layer.width
  obj.values = []
  for (let y = 0; y < layer.length; y++) {
    let flipY = layer.length - 1 - y
    for (let x = 0; x < layer.width; x++) {
      obj.values.push(layer.grid[x][flipY].value)
    }
  }
  return obj
}


export {loadEnvironmentDisplay}
