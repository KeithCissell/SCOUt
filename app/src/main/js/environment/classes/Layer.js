

class Layer {
  constructor(elementType, unit, grid, height, width) {
    this.elementType = elementType
    this.unit = unit
    this.grid = grid
    this.height = height
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
    obj.width = this.height
    obj.height = this.width
    obj.values = []
    for (let y = 0; y < obj.height; y++) {
      let flipY = obj.height - 1 - y
      for (let x = 0; x < obj.width; x++) {
        obj.values.push(this.grid[x][flipY].value)
      }
    }
    return obj
  }


}

export {Layer}
