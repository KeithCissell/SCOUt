// External Libraries
const d3 = require('d3')
const d3Contour = require('d3-contour')
const hsv = require('d3-hsv')


/*
_____drawLayer_____
Description
    Uses d3-contour to create multiple contour layers from Environment layer.
    Converts the contour data into SVG paths and appends them to the display.
Parameters
    layerName (string)  : elementType associated to layer in Environment
    threshold (int)     : how many contour-lines should be generated for display
    hue (int) [0,359]   : primary color between contour-lines
    saturation (flt) [0.0,1.0]
    opacity (flt) [0.0,1.0]     : opacity for the color between contour-lines
    lines (boolean)     : should contour-lines appear
    bottom (boolean)    : insert layer at the behind all existing layers
*/
function drawLayer(layer, threshold, hue, saturation, opacity, lines, bottom) {
  let display = document.getElementById("display")

  let layerJson = layer.toJson()

  let elementType = layerJson.elementType
  let height = layerJson.height
  let width = layerJson.width
  let values = layerJson.values
  let min = Math.min.apply(null, values)
  let max = Math.max.apply(null, values)
  let displaySize = Math.min(display.height.baseVal.value, display.width.baseVal.value) - 1
  let scaleFactor = displaySize / Math.max(height, width)

  let i0 = hsv.interpolateHsvLong(hsv.hsv(hue, saturation, .8, opacity), hsv.hsv(hue, saturation, .2, opacity))
  let i1 = hsv.interpolateHsvLong(hsv.hsv(hue, saturation, .8, opacity), hsv.hsv(hue, saturation, .2, opacity))
  let interpolateTerrain = function(t) { return t < 0.5 ? i0(t * 2) : i1((t - 0.5) * 2) }
  let color = d3.scaleSequential(interpolateTerrain).domain([min, max])

  let contours = d3Contour.contours()
      .size([width, height])
      .thresholds(d3.range(min, max, (max-min)/threshold))
      (values);

  let currentBottomNode = bottom ? display.children[0] : display.getElementsByClassName("display-cell")[0]

  for (let i = 0; i < contours.length; i++) {
    let contour = contours[i]
    let newPath = document.createElementNS("http://www.w3.org/2000/svg", "path")
    let dFunc = d3.geoPath(d3.geoIdentity().scale(scaleFactor))
    let d = dFunc(contour)
    if (d != null) {
      newPath.setAttribute("class", elementType)
      newPath.setAttribute("d", d)
      newPath.setAttribute("stroke", "black")
      newPath.setAttribute("stroke-width", lines ? 1 : 0 )
      newPath.setAttribute("fill", color(contour.value))
      display.insertBefore(newPath, currentBottomNode)
    }
  }

}


function drawCell(width, height, cellID, x, y, cellData) {
  let display = document.getElementById("display")

  let displaySize = Math.min(display.height.baseVal.value, display.width.baseVal.value) - 1
  let scaleFactor = displaySize / Math.max(height, width)
  let xPos = x * scaleFactor
  let yPos = (height - 1 - y) * scaleFactor // flip y axis for visualization

  let newCell = document.createElementNS("http://www.w3.org/2000/svg", "rect")
  newCell.setAttribute("id", cellID)
  newCell.setAttribute("class", "display-cell")
  newCell.setAttribute("x", xPos)
  newCell.setAttribute("y", yPos)
  newCell.setAttribute("height", scaleFactor)
  newCell.setAttribute("width", scaleFactor)
  newCell.setAttribute("stroke", "black")
  newCell.setAttribute("stroke-width", 1)
  newCell.setAttribute("fill-opacity", 0)
  newCell.selected = "false"
  newCell.xValue = x
  newCell.yValue = y
  display.appendChild(newCell)
}


/*******************************************************************************
_____eraseLayer_____
Description
    Remove all svg paths of a given element type from the display
Parameters
    layerName:    element type of the layer to remove from the svg
*******************************************************************************/
function eraseLayer(layerName) {
  let children = display.children
  for (let i = 0; i < children.length; i++) {
    let child = children[i]
    if (child.className.baseVal === layerName) { display.removeChild(child); i-- }
  }
}

/*******************************************************************************
_____highlightAnomalies_____
Description
    Highlights cells that contain anomalies
Parameters
    cells:    list of cells that contain anomalyType
*******************************************************************************/
function highlightAnomalies(cells) {
  // Reset cells
  let children = display.children
  for (let i = 0; i < children.length; i++) {
    let child = children[i]
    let cellData = child.id.split("-")
    if (cellData[0] === "cell") {
      child.setAttribute("fill-opacity", 0)
      child.setAttribute("fill", "")
    }
  }
  // Highlight cells
  for (let i in cells) {
    let c = cells[i]
    let cellId = "cell-" + c.x + "-" + c.y
    let cell = document.getElementById(cellId)
    cell.setAttribute("fill-opacity", 1)
    cell.setAttribute("fill", "red")
  }
}


export {drawLayer, drawCell, eraseLayer, highlightAnomalies}
