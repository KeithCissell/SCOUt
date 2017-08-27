

class Layer {
  constructor(elementType, grid, length, width) {
    this.elementType = elementType
    this.grid = grid
    this.length = length
    this.width = width
  }

  toJson() {
    let obj = {}
    console.log(this)
    obj.width = this.width
    obj.length = this.length
    obj.values = []
    for (let x in this.grid) {
      for (let y in this.grid[x]) {
        obj.values.push(this.grid[x][y].value)
      }
    }
    return obj
  }

}

export {Layer}
