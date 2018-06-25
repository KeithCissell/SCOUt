
class BasicEnvironmentForm {
  constructor(name, height, width) {
    this.name = name
    this.height = height
    this.width = width
  }
}

class AnomalySelectionForm {
  constructor(anomalyTypes) {
    this.anomalyTypes = anomalyTypes
    this.counts = {}
    for (let i in anomalyTypes) {
      let type = anomalyTypes[i]
      this.counts[type] = 0
    }
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

class TerrainModificationSelectionForm {
  constructor(terrainModificationTypes) {
    this.terrainModificationTypes = terrainModificationTypes
    this.counts = {}
    for (let i in terrainModificationTypes) {
      let type = terrainModificationTypes[i]
      this.counts[type] = 0
    }
  }
}

export {BasicEnvironmentForm, AnomalySelectionForm, ElementSelectionForm, TerrainModificationSelectionForm}
