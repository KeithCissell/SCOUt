const mainTable = document.getElementById("legend-main-table")
const selectedLayerTable = document.getElementById("legend-selected-layer-table")
const selectedCellTable = document.getElementById("legend-selected-cell-table")

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
  selectedLayerTable.appendChild(newTableRow)
}

function addLegendCellItem(name, value) {
  let newTableRow = document.createElement("tr")
  newTableRow.setAttribute("class", "legend-cell-row")
  let newTableName = document.createElement("td")
  newTableName.setAttribute("class", "legend-table-name")
  newTableName.innerText = name
  newTableRow.appendChild(newTableName)
  let newTableValue = document.createElement("td")
  newTableValue.setAttribute("class", "legend-table-value")
  newTableValue.innerText = value
  newTableRow.appendChild(newTableValue)
  selectedCellTable.appendChild(newTableRow)
}

export {addLegendMainItem, addLegendLayerItem, addLegendCellItem}
