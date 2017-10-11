// External Libraries
const d3 = require('d3')
const d3Contour = require('d3-contour')
const hsv = require('d3-hsv')


// Globals
const display = document.getElementById("display")


// Draws Layer Countour to SVG
function drawLayer(layer, threshold, colorValue, opacity, lines) {

  let layerJson = layer.toJson()

  let elementType = layerJson.elementType
  let width = layerJson.width
  let height = layerJson.length
  let values = layerJson.values
  let min = Math.min.apply(null, values)
  let max = Math.max.apply(null, values)

  let i0 = hsv.interpolateHsvLong(hsv.hsv(0, 0, 1, opacity), hsv.hsv(0, 0, 0, opacity))
  let i1 = hsv.interpolateHsvLong(hsv.hsv(0, 0, 1, opacity), hsv.hsv(0, 0, 0, opacity))
  let interpolateTerrain = function(t) { return t < 0.5 ? i0(t * 2) : i1((t - 0.5) * 2) }
  let color = d3.scaleSequential(interpolateTerrain).domain([min, max])

  let contours = d3Contour.contours()
      .size([width, height])
      .thresholds(d3.range(min, max, (max-min)/threshold))
      (values);

  for (let i = 0; i < contours.length; i++) {
    let contour = contours[i]
    let newPath = document.createElementNS("http://www.w3.org/2000/svg", "path")
    let dFunc = d3.geoPath(d3.geoIdentity().scale(500 / width))
    let d = dFunc(contour)
    newPath.setAttribute("id", elementType)
    newPath.setAttribute("d", d)
    newPath.setAttribute("stroke",  "black")
    newPath.setAttribute("stroke-width", lines ? 1 : 0 )
    newPath.setAttribute("fill", color(contour.value))
    display.appendChild(newPath)
  }

}

export {drawLayer}
