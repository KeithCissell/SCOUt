import {Element} from './classes/Element.js'
import {Cell} from './classes/Cell.js'
import {Environment} from './classes/Environment'

// Takes JSON and returns an Environment class
function buildEnvironment(json) {
  console.log(json)
  let envName = json.environment.name
  let length = json.environment.length
  let width = json.environment.width
  let envGrid = []
  for (let i = 0; i < length; i++) {
    envGrid.push([])
    for (let j = 0; j < width; j++) {
      envGrid[i].push(null)
    }
  }
  let jGrid = json.environment.grid
  for (let key in jGrid) {
    let jCell = jGrid[key]
    let x = jCell.x
    let y = jCell.y
    let elements = []
    let jElements = jCell.elements
    for(let key in jElements) {
      let jElement = jElements[key]
      let value = jElement.value
      let eName = jElement.name
      let unit = jElement.unit
      let constant = jElement.constant
      let circular = jElement.circular
      elements.push(new Element(value, eName, unit, constant, circular))
    }
    envGrid[x][y] = new Cell(x, y, elements)
  }
  return new Environment(envName, length, width, envGrid)
}

export {buildEnvironment}
