// External Libraries
const d3 = require('d3')
const d3Contour = require('d3-contour')
const hsv = require('d3-hsv')


// Globals
const canvas = d3.select("#canvas")

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

  let context = canvas.node().getContext("2d")
  let image = context.createImageData(width, height)
  console.log(image)

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

  let contours = d3Contour.contours()
      .size([width, height])
      .thresholds(d3.range(min, max, (max-min)/4))
      (values);
  // console.log(contours)

  canvas.selectAll("path")
    .data(contours)
    .enter()
      .append("path")
      .attr("d", d3.geoPath(d3.geoIdentity().scale(500 / width)))
      .attr("stroke", "black")
      .attr("stroke-width", "1")
      // .attr("fill", "none")
      .attr("fill", function(d) { return color(d.value); })

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


export {drawCanvas, drawContourPlot}
