import {Layer} from './Layer.js'
import {empty2D} from '../../Utils.js'

class Environment {
  constructor(name, height, width, grid, elementTypes, anomalyTypes) {
    this.name = name
    this.height = height
    this.width = width
    this.grid = grid
    this.elementTypes = elementTypes
    this.anomalyTypes = anomalyTypes
  }

  /*******************************************************************************
  _____extractLayer_____
  Description
      Creates a Layer object of a given element type
  Parameters
      elementType:    name of element type to extract
  *******************************************************************************/
  extractLayer(elementType) {
    if (this.elementTypes.includes(elementType)) {
      let unit = ""
      let layer = empty2D(this.height, this.width)
      for (let x in this.grid) {
        for (let y in this.grid[x]) {
          let cell = this.grid[x][y]
          if (cell.elements.has(elementType)) {
            let element = cell.elements.get(elementType)
            layer[x][y] = element
            if (unit == "") { unit = element.unit }
          }
        }
      }
      return new Layer(elementType, unit, layer, this.height, this.width)
    } else throw new Error("Element " + elementType + " not found.")
  }

  /*******************************************************************************
  _____extractAnomlyType_____
  Description
      Creates a Layer object of a given anomaly type
  Parameters
      anomalyType:    name of anomaly type to extract
  *******************************************************************************/
  extractAnomalyType(anomalyType) {
    if (this.anomalyTypes.includes(anomalyType)) {
      // let unit = ""
      // let layer = empty2D(this.height, this.width)
      // for (let x in this.grid) {
      //   for (let y in this.grid[x]) {
      //     let cell = this.grid[x][y]
      //     if (cell.elements.has(anomalyType)) {
      //       let element = cell.elements.get(anomalyType)
      //       layer[x][y] = element
      //       if (unit == "") { unit = element.unit }
      //     }
      //   }
      // }
      // return new Layer(anomalyType, unit, layer, this.height, this.width)
    } else throw new Error("Anomaly " + anomalyType + " not found.")
  }

}


export {Environment}
