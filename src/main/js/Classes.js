

class Element {
  constructor(value, name, unit, constant, circular, lowerBound, upperBound) {
    this.value = value
    this.name = name
    this.unit = unit
    this.constant = constant
    this.circular = circular
    this.lowerBound = lowerBound
    this.upperBound = upperBound
  }
}

class Cell {
  constructor(x, y, elements) {
    this.x = x
    this.y = y
    this.elements = elements
  }

}
