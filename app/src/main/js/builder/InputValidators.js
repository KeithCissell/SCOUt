
function textValidation(name, input) {
  if (input) {
    let value = input.value
    if (value == "") {return `${name} cannot be blank`}
    return "valid"
  } else { throw new Error(`INPUT ELEMENT NOT FOUND: ${name}`) }
}

function numberValidation(name, input) {
  if (input) {
    let value = parseInt(input.value)
    let min = parseInt(input.min)
    let max = parseInt(input.max)
    if (!(value >= min && value <= max)) { return `${name} not in range (${input.min}, ${input.max})` }
    return "valid"
  } else { throw new Error(`INPUT ELEMENT NOT FOUND: ${name}`) }
}

export {textValidation, numberValidation}
