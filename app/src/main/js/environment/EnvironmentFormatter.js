import {Element} from './classes/Element.js'
import {Cell} from './classes/Cell.js'
import {Environment} from './classes/Environment'
import {empty2D} from '../Utils.js'

/*******************************************************************************
_____formatEnvironment_____
Description
    Builds an Environment object from Json data
Parameters
    json:   Json formatted data to be parsed
*******************************************************************************/
function formatEnvironment(json) {
  let envName = json.environment.name
  let height = json.environment.height
  let width = json.environment.width
  let envGrid = empty2D(height, width)
  let envElementTypes = []
  let jGrid = json.environment.grid
  for (let key in jGrid) {
    let jCell = jGrid[key]
    let x = jCell.x
    let y = jCell.y
    let elements = new Map()
    let jElements = jCell.elements
    for(let key in jElements) {
      let jElement = jElements[key]
      let value = jElement.value
      let eName = jElement.name
      let unit = jElement.unit
      let constant = jElement.constant
      let circular = jElement.circular
      if (!envElementTypes.includes(eName)) envElementTypes.push(eName)
      elements.set(eName, new Element(value, eName, unit, constant, circular))
    }
    envGrid[x][y] = new Cell(x, y, elements)
  }
  return new Environment(envName, height, width, envGrid, envElementTypes)
}

export {formatEnvironment}
