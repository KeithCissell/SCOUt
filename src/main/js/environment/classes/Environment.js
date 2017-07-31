import {Layer} from './Layer.js'
import {empty2D} from '../../Utils.js'

class Environment {
  constructor(name, length, width, grid, elementTypes) {
    this.name = name
    this.length = length
    this.width = width
    this.grid = grid
    this.elementTypes = elementTypes
    this.layers = extractLayers(grid, elementTypes, length, width)
  }

}

function extractLayers(grid, elementTypes, l, w) {
  let layerList = []
  for (let i in elementTypes) {
    let eType = elementTypes[i]
    let layer = empty2D(l, w)
    for (let x in grid) {
      for (let y in grid[x]) {
        let cell = grid[x][y]
        for (let e in cell.elements) {
          let element = cell.elements[e]
          if (element.name == eType) { layer[x][y] = element }
        }
      }
    }
    layerList.push(new Layer(eType, layer))
  }
  return layerList
}

export {Environment}
