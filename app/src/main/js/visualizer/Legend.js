const mainTable = document.getElementById("legend-main-table")
const currentLayerTable = document.getElementById("legend-current-layer-table")

function addLegendMainItem(name, value) {
  let newTableRow = document.createElement("tr")
  let newTableName = document.createElement("td")
  newTableName.setAttribute("class", "legend-table-name")
  newTableName.innerText = name
  newTableRow.appendChild(newTableName)
  let newTableValue = document.createElement("td")
  newTableValue.setAttribute("class", "legend-table-value")
  newTableValue.innerText = value
  newTableRow.appendChild(newTableValue)
  mainTable.appendChild(newTableRow)
}

function addLegendLayerItem(name, value) {
  let newTableRow = document.createElement("tr")
  let newTableName = document.createElement("td")
  newTableName.setAttribute("class", "legend-table-name")
  newTableName.innerText = name
  newTableRow.appendChild(newTableName)
  let newTableValue = document.createElement("td")
  newTableValue.setAttribute("class", "legend-table-value")
  newTableValue.innerText = value
  newTableRow.appendChild(newTableValue)
  currentLayerTable.appendChild(newTableRow)
}

export {addLegendMainItem, addLegendLayerItem}
