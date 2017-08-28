import {Layer} from './Layer.js'
import {empty2D} from '../../Utils.js'

class Environment {
  constructor(name, length, width, grid, elementTypes) {
    this.name = name
    this.length = length
    this.width = width
    this.grid = grid
    this.elementTypes = elementTypes
  }

  extractLayer(elementType) {
    if (this.elementTypes.includes(elementType)) {
      let layer = empty2D(this.length, this.width)
      for (let x in this.grid) {
        for (let y in this.grid[x]) {
          let cell = this.grid[x][y]
          if (cell.elements.has(elementType)) layer[x][y] = cell.elements.get(elementType)
        }
      }
      return new Layer(elementType, layer, this.length, this.width)
    } else throw new Error("Element " + elementType + " not found.")
  }

}


export {Environment}
