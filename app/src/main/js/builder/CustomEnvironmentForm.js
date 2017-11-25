import {getElementTypes} from '../SCOUtAPI.js'
import {loadEnvironmentBuilderPage} from './EnvironmentBuilder.js'
import {BasicEnvironmentForm, ElementSelectionForm, ElementSeedForm} from './FormClasses.js'


// Global DOM Elements
let environmentForm
let basicInputs
let customInputs
let submitButtons
let backButton
let nextButton

// Globals
let currentState = "SelectElementTypes"
let elementSelectionForm = null
let elementSeedForms = {}


/*******************************************************************************
_____loadCustomEnvironmentForm_____
Description
    Load custom environment form page
    Create new submit buttons
    Load the initial "Select Element Types" form page
*******************************************************************************/
function loadCustomEnvironmentForm() {
  // capture DOM elements
  environmentForm = document.getElementById("environment-form")
  basicInputs = document.getElementById("basic-inputs")
  submitButtons = document.getElementById("submit-buttons")

  // build custom inputs section
  let newCustomInputs = document.createElement("div")
  newCustomInputs.setAttribute("id", "custom-inputs")
  environmentForm.appendChild(newCustomInputs)
  customInputs = document.getElementById("custom-inputs")

  // create submit buttons
  submitButtons.innerHTML = `
    <button class="submit-button" id="back-button">Back</button>
    <button class="submit-button" id="next-button">Next</button>
  `
  backButton = document.getElementById("back-button")
  backButton.addEventListener("click", () => backButtonHandler())
  nextButton = document.getElementById("next-button")
  nextButton.addEventListener("click", () => nextButtonHandler())

  loadElementTypes()
}

/*******************************************************************************
_____backButtonHandler_____
Description
    Handles "Back" clicks while user is filling out the custom environment forms
*******************************************************************************/
function backButtonHandler() {
  switch(currentState) {
    case "SelectElementTypes":
          loadEnvironmentBuilderPage()
          break;
  }
}

/*******************************************************************************
_____nextButtonHandler_____
Description
Handles "Next" clicks while user is filling out the custom environment forms
*******************************************************************************/
function nextButtonHandler() {
  switch(currentState) {
    case "SelectElementTypes":
          // do stuff with selected elements
          break;
  }
}

/*******************************************************************************
_____loadElementTypes_____
Description
    Checks if element types have been grabed form the backend
    Calls to load the "Select Element Types" form
*******************************************************************************/
async function loadElementTypes() {
  if (elementSelectionForm == null) {
    let elementTypes = await getElementTypes()
    await elementTypes.json().then((json) => {
      setupElementForms(json["Element Types"])
    })
  }
  loadElementSelectionForm()
}

/*******************************************************************************
_____setupElementForms_____
Description
    To do
*******************************************************************************/
function setupElementForms(elementTypes) {
  elementSelectionForm = new ElementSelectionForm(elementTypes)
  for (let type in elementTypes) {
    console.log("Getting Element Seed:", type)
  }
}

/*******************************************************************************
_____loadElementSelectionForm_____
Description
    Loads checkboxes for each of the possible element types that can be selected
*******************************************************************************/
function loadElementSelectionForm() {
  let listLabel = document.createElement("h3")
  listLabel.innerText = "Select Element Types"
  let elementSelectionList = document.createElement("ul")
  elementSelectionList.setAttribute("id", "element-selection-list")
  for (let type in elementSelectionForm.elementTypes) {
    let typeId = type + "-selection"
    // create list item element
    let newListItem = document.createElement("li")
    newListItem.setAttribute("id", type + "-list-item")
    // create checkbox element
    let newCheckbox = document.createElement("input")
    newCheckbox.setAttribute("id", typeId)
    newCheckbox.setAttribute("type", "checkbox")
    newCheckbox.setAttribute("name", "")
    newCheckbox.setAttribute("value", type)
    if (elementSelectionForm.elementTypes[type]) {
      newCheckbox.setAttribute("checked", "checked")
      newCheckbox.setAttribute("disabled", "disabled")
    } else if (elementSelectionForm.selectables[type]) {
      newCheckbox.setAttribute("checked", "checked")
    }
    // create label element for checkbox
    let newLabel = document.createElement("label")
    newLabel.setAttribute("for", typeId)
    newLabel.innerText = type
    // add elements to DOM
    newListItem.appendChild(newCheckbox)
    newListItem.appendChild(newLabel)
    elementSelectionList.appendChild(newListItem)
  }
  customInputs.appendChild(listLabel)
  customInputs.appendChild(elementSelectionList)

  // add event listeners
  for (let type in elementSelectionForm.selectables) {
    let typeId = type + "-selection"
    document.getElementById(typeId).addEventListener("click", () => {
      elementSelectionForm.selectables[type] = !elementSelectionForm.selectables[type]
    })
  }
}

export {loadCustomEnvironmentForm}
