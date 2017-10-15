// External Libraries
const d3 = require('d3')
const d3Contour = require('d3-contour')
const hsv = require('d3-hsv')


// Globals
const display = document.getElementById("display")


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

  let layerJson = layer.toJson()

  let elementType = layerJson.elementType
  let width = layerJson.width
  let height = layerJson.length
  let values = layerJson.values
  let min = Math.min.apply(null, values)
  let max = Math.max.apply(null, values)

  let i0 = hsv.interpolateHsvLong(hsv.hsv(hue, saturation, .8, opacity), hsv.hsv(hue, saturation, .2, opacity))
  let i1 = hsv.interpolateHsvLong(hsv.hsv(hue, saturation, .8, opacity), hsv.hsv(hue, saturation, .2, opacity))
  let interpolateTerrain = function(t) { return t < 0.5 ? i0(t * 2) : i1((t - 0.5) * 2) }
  let color = d3.scaleSequential(interpolateTerrain).domain([min, max])

  let contours = d3Contour.contours()
      .size([width, height])
      .thresholds(d3.range(min, max, (max-min)/threshold))
      (values);

  let currentBottomNode = display.children[0]

  for (let i = 0; i < contours.length; i++) {
    let contour = contours[i]
    let newPath = document.createElementNS("http://www.w3.org/2000/svg", "path")
    let dFunc = d3.geoPath(d3.geoIdentity().scale(500 / width))
    let d = dFunc(contour)
    newPath.setAttribute("class", elementType)
    newPath.setAttribute("d", d)
    newPath.setAttribute("stroke",  "black")
    newPath.setAttribute("stroke-width", lines ? 1 : 0 )
    newPath.setAttribute("fill", color(contour.value))
    if (bottom) display.insertBefore(newPath, currentBottomNode)
    else display.appendChild(newPath)
  }

}


// Remove all child elements of display that have the given class name
function eraseLayer(layerName) {
  let children = display.children
  for (let i = 0; i < children.length; i++) {
    let child = children[i]
    if (child.className.baseVal === layerName) { display.removeChild(child); i-- }
  }
}


export {drawLayer, eraseLayer}
