

class Layer {
  constructor(elementType, unit, grid, length, width) {
    this.elementType = elementType
    this.unit = unit
    this.grid = grid
    this.length = length
    this.width = width
  }

  /*******************************************************************************
  _____toJson_____
  Description
      Creates a Json object of the layer to be used for display purposes
  Notes
      Rotate the object 90 degrees counter-clockwise for proper visualization:
          (x, y) = (y, -x)
  *******************************************************************************/
  toJson() {
    let obj = {}
    obj.elementType = this.elementType
    obj.unit = this.unit
    obj.width = this.length
    obj.length = this.width
    obj.values = []
    for (let y = 0; y < obj.length; y++) {
      let flipY = obj.length - 1 - y
      for (let x = 0; x < obj.width; x++) {
        obj.values.push(this.grid[x][flipY].value)
      }
    }
    return obj
  }


}

export {Layer}
