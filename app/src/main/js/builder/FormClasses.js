
class BasicEnvironmentForm {
  constructor(name, height, width) {
    this.name = name
    this.height = height
    this.width = width
  }
}

class ElementSelectionForm {
  constructor(elementTypes) {
    this.elementTypes = elementTypes
    this.defaults = []
    this.selectables = {}
    for (let type in elementTypes) {
      if (elementTypes[type]) this.defaults.push(type)
      else this.selectables[type] = false
    }
  }
}

export {BasicEnvironmentForm, ElementSelectionForm}
