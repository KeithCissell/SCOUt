

class Layer {
  constructor(elementType, grid, length, width) {
    this.elementType = elementType
    this.grid = grid
    this.length = length
    this.width = width
  }

  toJson() {
    let obj = {}
    // Rotate the object 90 degrees counter-clockwise for visualization tool: (x, y) = (y, -x)
    obj.elementType = this.elementType
    obj.width = this.length
    obj.length = this.width
    obj.values = []
    for (let y = 0; y < this.length; y++) {
      let flipY = this.length - 1 - y
      for (let x = 0; x < this.width; x++) {
        obj.values.push(this.grid[x][flipY].value)
      }
    }
    return obj
  }


}

export {Layer}
