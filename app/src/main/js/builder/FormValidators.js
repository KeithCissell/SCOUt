
/*******************************************************************************
_____checkBasicInputs_____
Description
    Verifies that the user's input values are valid
*******************************************************************************/
function checkBasicInputs() {
  let valid = true
  let basicInputs = document.getElementById("basic-inputs").getElementsByTagName("input")

  let name = basicInputs["environment-name"]
  let nameValidation = textValidation("environment-name", name)
  if (nameValidation != "valid") {
    alert(nameValidation)
    valid = false
  }

  let height = basicInputs["height"]
  let heightValidation = numberValidation("height", height)
  if (heightValidation != "valid") {
    alert(heightValidation)
    valid = false
  }

  let width = basicInputs["width"]
  let widthValidation = numberValidation("width", width)
  if (widthValidation != "valid") {
    alert(widthValidation)
    valid = false
  }

  return valid
}

/*******************************************************************************
_____textValidation_____
Description
    Validate a text input field
Parameters
    name:     name of input field
    input:    value in the input field
*******************************************************************************/
function textValidation(name, input) {
  if (input) {
    let value = input.value
    if (value == "") {return `${name} cannot be blank`}
    return "valid"
  } else { throw new Error(`INPUT ELEMENT NOT FOUND: ${name}`) }
}

/*******************************************************************************
_____numberValidation_____
Description
    Validate a number input field
Parameters
Parameters
    name:     name of input field
    input:    value in the input field
*******************************************************************************/
function numberValidation(name, input) {
  if (input) {
    let value = parseInt(input.value)
    let min = parseInt(input.min)
    let max = parseInt(input.max)
    if (!(value >= min && value <= max)) { return `${name} not in range (${input.min}, ${input.max})` }
    return "valid"
  } else { throw new Error(`INPUT ELEMENT NOT FOUND: ${name}`) }
}


export {checkBasicInputs, textValidation, numberValidation}
