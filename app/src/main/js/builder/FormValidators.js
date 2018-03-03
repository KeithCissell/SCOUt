
/*******************************************************************************
_____checkBasicInputs_____
Description
    Verifies that the user's input values are valid
*******************************************************************************/
function checkBasicInputs() {
  let valid = true
  let basicInputs = document.getElementsByClassName("basic-input")

  for (let i = 0; i < basicInputs.length; i++) {
    let input = basicInputs.item(i)
    let type = input.type
    let validation = ""
    switch (type) {
      case "text":
          validation = textValidation(input)
          break
      case "number":
          validation = numberValidation(input)
          break
      default:
          throw new Error('Type not found: ' + type)
    }
    if (validation != "valid") {
      alert(validation)
      valid = false
    }
  }

  return valid
}

/*******************************************************************************
_____checkCustomInputs_____
Description
    Verifies that the user's input values are valid
*******************************************************************************/
function checkCustomInputs() {
  let valid = true
  let customInputs = document.getElementById("custom-inputs").getElementsByClassName("custom-input")

  for (let i = 0; i < customInputs.length; i++) {
    let input = customInputs.item(i)
    let type = input.type
    let validation = ""
    switch (type) {
      case "text":
          validation = textValidation(input)
          break
      case "number":
          validation = numberValidation(input)
          break
      default:
          throw new Error('Type not found: ' + type)
    }
    if (validation != "valid") {
      alert(validation)
      valid = false
    }
  }

  return valid
}

/*******************************************************************************
_____textValidation_____
Description
    Validate a text input field
Parameters
    input:    input DOM element
*******************************************************************************/
function textValidation(input) {
  let name = input.id
  let value = input.value
  if (value == "") {return `${name} cannot be blank`}
  else return "valid"
}

/*******************************************************************************
_____numberValidation_____
Description
    Validate a number input field
Parameters
    input:    input DOM element
*******************************************************************************/
function numberValidation(input) {
  let name = input.id
  let value = parseFloat(input.value)
  let min = parseFloat(input.min)
  let max = parseFloat(input.max)
  if (!(value >= min && value <= max)) { return `${name} not in range (${input.min}, ${input.max})` }
  else return "valid"
}


export {checkBasicInputs, checkCustomInputs, textValidation, numberValidation}
