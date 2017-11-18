
function loadCustomEnvironmentForm() {
  let environmentForm = document.getElementById("environment-form")
  let customInputs = document.createElement("div")
  customInputs.setAttribute("id", "custom-inputs")
  environmentForm.appendChild(customInputs, submitButtons)

  let submitButtons = document.getElementById("submit-buttons")
  submitButtons.innerHTML = `
    <button id="build-environment">Build Environment</button>
  `
}

export {loadCustomEnvironmentForm}
