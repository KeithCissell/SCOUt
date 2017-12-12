
/*******************************************************************************
_____buildFormFields_____
Description
    Takes an element's seed parameters and builds HTML form fields
Parameters
    json:   json object holding fields and necessary data
*******************************************************************************/
function buildFormFields(json) {
  let formFields = []
  for (let key in json) {
    let item = json[key]
    switch (item.type) {
      case "number":
        let div = document.createElement('div')
        div.setAttribute("class", "custom-input-field")
        div.setAttribute("id", key + "-input-field")
        let label = buildLabel(key, key)
        let input = buildNumberField(key, item)
        let suffix = buildLabel(key, item.unit)
        div.appendChild(label)
        div.appendChild(input)
        div.appendChild(suffix)
        formFields.push(div)
        break;
      default:
        console.log("KEY: ", key, " not found")
    }
  }
  return formFields
}

/*******************************************************************************
_____buildLabel_____
Description
    Builds a label DOM element
Parameters
    forId:        the Id for the labels corresponding input field
    innerText:    the innerText for the label
*******************************************************************************/
function buildLabel(forId, innerText) {
  let label = document.createElement('label')
  label.setAttribute("for", forId)
  label.innerText = innerText
  return label
}

/*******************************************************************************
_____buildNumberField_____
Description
    Builds a "number" type input DOM element
Parameters
    id:           the Id to be given to the input field
    numberInfo:   json object with field parameters
*******************************************************************************/
function buildNumberField(id, numberInfo) {
  let numberField = document.createElement('input')
  numberField.setAttribute("class", "custom-input")
  numberField.setAttribute("type", "number")
  numberField.setAttribute("id", id)
  numberField.setAttribute("min", numberInfo.lowerBound)
  numberField.setAttribute("max", numberInfo.upperBound)
  numberField.value = numberInfo.value
  return numberField
}

export {buildFormFields}
