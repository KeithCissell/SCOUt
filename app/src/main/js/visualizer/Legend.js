
function addLegendMainItem(name, value) {
  let mainTable = document.getElementById("legend-main-table")

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
  let selectedLayerTable = document.getElementById("legend-selected-layer-table")

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
  let selectedCellTable = document.getElementById("legend-selected-cell-table")

  let newTableRow = document.createElement("tr")
  let newTableName = document.createElement("td")
  newTableName.setAttribute("class", "legend-table-name")
  newTableName.innerText = name
  newTableRow.appendChild(newTableName)
  let newTableValue = document.createElement("td")
  newTableValue.setAttribute("id", name + "-value")
  newTableValue.setAttribute("class", "legend-table-value")
  newTableValue.innerText = value
  newTableRow.appendChild(newTableValue)
  selectedCellTable.appendChild(newTableRow)
}

export {addLegendMainItem, addLegendLayerItem, addLegendCellItem}
