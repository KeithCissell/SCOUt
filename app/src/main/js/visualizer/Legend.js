const mainLegendList = document.getElementById("main-legend-list")
const currentLayerLegendList = document.getElementById("current-layer-legend-list")

function loadLegendMainItem(name, value) {
  let newListItem = document.createElement("li")
  newListItem.innerHTML = `<strong class="legend-li-name">${name + ": "}</strong>${value}`
  mainLegendList.appendChild(newListItem)
  console.log(name + " = " + value)
}

export {loadLegendMainItem}
