import {getElementTypes, getAnomalyTypes, getTerrainModificationTypes, getElementSeedForm, getAnomalyForm, getTerrainModificationForm, buildCustomEnvironment} from '../SCOUtAPI.js'
import {loadEnvironmentBuilderPage, getBasicInputs} from './EnvironmentBuilder.js'
import {BasicEnvironmentForm, AnomalySelectionForm, ElementSelectionForm, TerrainModificationSelectionForm} from './FormClasses.js'
import {buildFormFields} from './FormBuilder.js'
import {checkBasicInputs, checkCustomInputs} from './FormValidators.js'
import {formatEnvironment} from '../environment/EnvironmentFormatter.js'
import {loadVisualizer} from '../visualizer/Visualizer.js'

/*******************************************************************************
_____WORKFLOW_____
Description
    User selects Environment load option:
        A. Load random environment
        B. Create a custom environment
        D. Load a previously saved environment
    A. Load Random
        1. Request is sent off, no further action required
    B. Build Custom
      a) Select types/presence
        1. Select anomaly presence (number of occurances for each type)
        2. Select element types present (some required, some not)
        3. Select number of terrain modifications to make
      b) Fill out forms
        1. Fill out all Element Seed forms
        2. Fill out all anomaly forms
        3. Fill out all terrain modifications
      c) Review and submit
    C. Load previously saved environment
      1. Select a file (setup data or environment itself)

*******************************************************************************/

// Global DOM Elements
let environmentForm
let basicInputs
let customInputs
let customInputsTitle
let customInputsContent
let submitButtons
let backButton
let nextButton

// Globals
let currentState = "SelectAnomalyTypes"
let maxAnomalies = 20
let maxTerrainModifications = 10
let anomalySelectionForm = null
let elementSelectionForm = null
let terrainModificationSelectionForm = null
let elementSeedForms = []
let elementSeedIndex = -1
let anomalyTypeIndexes = {}
let anomalyForms = []
let anomalyIndex = -1
let terrainModificationTypeIndexes = {}
let terrainModificationForms = []
let terrainModificationIndex = -1


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
  customInputs = document.getElementById("custom-inputs")
  customInputsTitle = document.getElementById("custom-inputs-title")
  customInputsContent = document.getElementById("custom-inputs-content")

  // create submit buttons
  submitButtons.innerHTML = `
    <button class="submit-button" id="back-button">Back</button>
    <button class="submit-button" id="next-button">Next</button>
  `
  backButton = document.getElementById("back-button")
  backButton.addEventListener("click", () => backButtonHandler())
  nextButton = document.getElementById("next-button")
  nextButton.addEventListener("click", () => nextButtonHandler())

  elementSeedIndex = -1
  terrainModificationIndex = -1
  anomalyIndex = -1

  setupEnvironmentFormData()
}

/*******************************************************************************
_____setupEnvironmentFormData_____
Description
    Sets up all the local structures for forms
    Calls 'loadAnomalySelectionForm' to kick off the form page process
*******************************************************************************/
async function setupEnvironmentFormData() {
  // Load anomaly selection types
  if (anomalySelectionForm == null) {
    let anomalyTypes = await getAnomalyTypes()
    await anomalyTypes.json().then((json) => {
      anomalySelectionForm = new AnomalySelectionForm(json["Anomaly Types"])
      setupAnomalyForms(json["Anomaly Types"])
    })
  }
  // Load element selection types
  if (elementSelectionForm == null) {
    let elementTypes = await getElementTypes()
    await elementTypes.json().then((json) => {
      elementSelectionForm = new ElementSelectionForm(json["Element Types"])
      setupElementForms(json["Element Types"])
    })
  }
  // Load terrain modification selection types
  if (terrainModificationSelectionForm == null) {
    let terrainModificationTypes = await getTerrainModificationTypes()
    await terrainModificationTypes.json().then((json) => {
      terrainModificationSelectionForm = new TerrainModificationSelectionForm(json["Terrain Modification Types"])
      setupTerrainModificationForms(json["Terrain Modification Types"])
    })
  }
  loadAnomalySelectionForm()
}

/*******************************************************************************
_____setupAnomalyForms_____
Description
    Sets up a mapping of anomaly type to form data
*******************************************************************************/
async function setupAnomalyForms(anomalyTypes) {
  let indexCounter = 0
  for (let i in anomalyTypes) {
    let type = anomalyTypes[i]
    let formJson
    let formData = await getAnomalyForm(type)
    await formData.json().then((json) => {
      formJson = json
    })
    for (let i = 0; i < maxAnomalies; i++) {
      let form = await {}
      form["anomaly"] = await type
      form["selected"] = await false
      form["json"] = await formJson
      await anomalyForms.push(form)
    }
    anomalyTypeIndexes[type] = await indexCounter
    indexCounter += await maxAnomalies
  }
}

/*******************************************************************************
_____setupElementForms_____
Description
    To do
*******************************************************************************/
async function setupElementForms(elementTypes) {
  for (let type in elementTypes) {
    let seedForm = await {}
    seedForm["element"] = await type
    seedForm["selected"] = await elementTypes[type]
    let formData = await getElementSeedForm(type)
    await formData.json().then((json) => {
      seedForm["json"] = json
    })
    await elementSeedForms.push(seedForm)
  }
}

/*******************************************************************************
_____setupTerrainModificationForms_____
Description
    Sets up a mapping of terrain modification type to form data
*******************************************************************************/
async function setupTerrainModificationForms(terrainModificationTypes) {
  let indexCounter = 0
  for (let i in terrainModificationTypes) {
    let type = await terrainModificationTypes[i]
    let formJson
    let formData = await getTerrainModificationForm(type)
    await formData.json().then((json) => {
      formJson = json
    })
    for (let i = 0; i < maxTerrainModifications; i++) {
      let form = await {}
      form["terrain-modification"] = await type
      form["selected"] = await false
      form["json"] = await formJson
      await terrainModificationForms.push(form)
    }
    terrainModificationTypeIndexes[type] = await indexCounter
    indexCounter += await maxTerrainModifications
  }
}

/*******************************************************************************
_____backButtonHandler_____
Description
    Handles "Back" clicks while user is filling out the custom environment forms
*******************************************************************************/
function backButtonHandler() {
  switch(currentState) {
    case "SelectAnomalyTypes":
          saveAnomalyTypesForm()
          basicInputs = getBasicInputs()
          loadEnvironmentBuilderPage(basicInputs.name, basicInputs.height, basicInputs.width)
          break;
    case "SelectElementTypes":
          if (checkCustomInputs()) {
            saveElementTypesForm()
            loadAnomalySelectionForm()
          }
          break;
    case "SelectTerrainModificationTypes":
          if (checkCustomInputs()) {
            saveTerrainModificationTypesForm()
            loadElementSelectionForm()
          }
          break;
    case "ElementSeedForm":
          if (checkCustomInputs()) {
            saveElementSeedForm()
            loadPreviousElementSeedForm()
          }
          break;
    case "TerrainModificationForm":
          if (checkCustomInputs()) {
            saveTerrainModificationForm()
            loadPreviousTerrainModificationForm()
          }
          break;
    case "AnomalyForm":
          if (checkCustomInputs()) {
            saveAnomalyForm()
            loadPreviousAnomalyForm()
          }
          break;
    case "ReviewForm":
          document.getElementById("next-button").innerText = "Next"
          loadPreviousAnomalyForm()
  }
}

/*******************************************************************************
_____nextButtonHandler_____
Description
Handles "Next" clicks while user is filling out the custom environment forms
*******************************************************************************/
function nextButtonHandler() {
  switch(currentState) {
    case "SelectAnomalyTypes":
          saveAnomalyTypesForm()
          loadElementSelectionForm()
          break;
    case "SelectElementTypes":
          saveElementTypesForm()
          loadTerrainModificationSelectionForm()
          break;
    case "SelectTerrainModificationTypes":
          saveTerrainModificationTypesForm()
          loadNextElementSeedForm()
          break;
    case "ElementSeedForm":
          if (checkCustomInputs()) {
            saveElementSeedForm()
            loadNextElementSeedForm()
          }
          break;
    case "TerrainModificationForm":
          if (checkCustomInputs()) {
            saveTerrainModificationForm()
            loadNextTerrainModificationForm()
          }
          break;
    case "AnomalyForm":
          if (checkCustomInputs()) {
            saveAnomalyForm()
            loadNextAnomalyForm()
          }
          break;
    case "ReviewForm":
          if (checkBasicInputs) submitCustomEnvironment()
  }
}

/*******************************************************************************
_____loadAnomalySelectionForm_____
Description
    Loads form for number of each anomaly present
*******************************************************************************/
function loadAnomalySelectionForm() {
  currentState = "SelectAnomalyTypes"
  customInputsTitle.innerText = "Select Anomaly Presence"
  customInputsContent.innerHTML = ""
  let anomalySelectionList = document.createElement("ul")
  anomalySelectionList.setAttribute("id", "anomaly-selection-list")
  anomalySelectionList.setAttribute("class", "selection-list")
  for (let i in anomalySelectionForm.anomalyTypes) {
    let type = anomalySelectionForm.anomalyTypes[i]
    let typeId = type + "-count"
    // create list item element
    let newListItem = document.createElement("li")
    newListItem.setAttribute("id", type + "-list-item")
    // create nubmer input element
    let numberField = document.createElement('input')
    numberField.setAttribute("class", "custom-input")
    numberField.setAttribute("type", "number")
    numberField.setAttribute("id", typeId)
    numberField.setAttribute("min", 0)
    numberField.setAttribute("max", maxAnomalies)
    numberField.value = anomalySelectionForm.counts[type]
    // create label element for checkbox
    let newLabel = document.createElement("label")
    newLabel.setAttribute("for", typeId)
    newLabel.innerText = type
    // add elements to DOM
    newListItem.appendChild(numberField)
    newListItem.appendChild(newLabel)
    anomalySelectionList.appendChild(newListItem)
  }
  customInputsContent.appendChild(anomalySelectionList)
}

/*******************************************************************************
_____saveAnomalyTypesForm_____
Description
    Saves the form for the selected anomaly counts
*******************************************************************************/
function saveAnomalyTypesForm() {
  // Save anomaly types form
  let anomalyList = document.getElementById("anomaly-selection-list").children
  for (let i = 0; i < anomalyList.length; i++) {
    let item = anomalyList[i]
    let type = item.getElementsByTagName("label")[0].innerText
    let value = item.getElementsByTagName("input")[0].value
    anomalySelectionForm.counts[type] = value
  }
  // Sets whether anomaly form is used or not
  for (let type in anomalySelectionForm.counts) {
    let count = anomalySelectionForm.counts[type]
    let startIndex = anomalyTypeIndexes[type]
    for (let i = startIndex; i < startIndex + maxAnomalies; i++) {
      if (count != 0) {
        anomalyForms[i]["selected"] = true
        count -= 1
      }
      else anomalyForms[i]["selected"] = false
    }
  }
}

/*******************************************************************************
_____loadElementSelectionForm_____
Description
    Loads checkboxes for each of the possible element types that can be selected
*******************************************************************************/
function loadElementSelectionForm() {
  currentState = "SelectElementTypes"
  customInputsTitle.innerText = "Select Element Types"
  customInputsContent.innerHTML = ""
  let elementSelectionList = document.createElement("ul")
  elementSelectionList.setAttribute("id", "element-selection-list")
  elementSelectionList.setAttribute("class", "selection-list")
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
  customInputsContent.appendChild(elementSelectionList)
  // add event listeners
  for (let type in elementSelectionForm.selectables) {
    let typeId = type + "-selection"
    document.getElementById(typeId).addEventListener("click", () => {
      elementSelectionForm.selectables[type] = !elementSelectionForm.selectables[type]
    })
  }
}

/*******************************************************************************
_____saveElementTypesForm_____
Description
    Saves the form for the selected element types
*******************************************************************************/
function saveElementTypesForm() {
  for (let i = 0; i < elementSeedForms.length; i++) {
    let type = elementSeedForms[i]["element"]
    if (!elementSelectionForm.elementTypes[type]) {
      elementSeedForms[i]["selected"] = elementSelectionForm.selectables[type]
    }
  }
}

/*******************************************************************************
_____loadTerrainModificationSelectionForm_____
Description
    Loads form for number of each terrain modification to be made
*******************************************************************************/
function loadTerrainModificationSelectionForm() {
  currentState = "SelectTerrainModificationTypes"
  customInputsTitle.innerText = "Select Terrain Modificatinos to be Made"
  customInputsContent.innerHTML = ""
  let terrainModificationSelectionList = document.createElement("ul")
  terrainModificationSelectionList.setAttribute("id", "terrain-modification-selection-list")
  terrainModificationSelectionList.setAttribute("class", "selection-list")
  for (let i in terrainModificationSelectionForm.terrainModificationTypes) {
    let type = terrainModificationSelectionForm.terrainModificationTypes[i]
    let typeId = type + "-count"
    // create list item element
    let newListItem = document.createElement("li")
    newListItem.setAttribute("id", type + "-list-item")
    // create nubmer input element
    let numberField = document.createElement('input')
    numberField.setAttribute("class", "custom-input")
    numberField.setAttribute("type", "number")
    numberField.setAttribute("id", typeId)
    numberField.setAttribute("min", 0)
    numberField.setAttribute("max", maxTerrainModifications)
    numberField.value = 0
    // create label element for checkbox
    let newLabel = document.createElement("label")
    newLabel.setAttribute("for", typeId)
    newLabel.innerText = type
    // add elements to DOM
    newListItem.appendChild(numberField)
    newListItem.appendChild(newLabel)
    terrainModificationSelectionList.appendChild(newListItem)
  }
  customInputsContent.appendChild(terrainModificationSelectionList)
}

/*******************************************************************************
_____saveTerrainModificationTypesForm_____
Description
    Saves the form for the selected terrain modification counts
*******************************************************************************/
function saveTerrainModificationTypesForm() {
  let terrainModificationList = document.getElementById("terrain-modification-selection-list").children
  for (let i = 0; i < terrainModificationList.length; i++) {
    let item = terrainModificationList[i]
    let type = item.getElementsByTagName("label")[0].innerText
    let value = item.getElementsByTagName("input")[0].value
    terrainModificationSelectionForm.counts[type] = value
  }
  // Sets whether terrain modification form is used or not
  for (let type in terrainModificationSelectionForm.counts) {
    let count = terrainModificationSelectionForm.counts[type]
    let startIndex = terrainModificationTypeIndexes[type]
    for (let i = startIndex; i < startIndex + maxTerrainModifications; i++) {
      if (count != 0) {
        terrainModificationForms[i]["selected"] = true
        count -= 1
      }
      else terrainModificationForms[i]["selected"] = false
    }
  }
}

/*******************************************************************************
_____loadPreviousElementSeedForm_____
Description
    Moves to previous selected element seed form or returns to element type selection form
*******************************************************************************/
function loadPreviousElementSeedForm() {
  currentState = "ElementSeedForm"
  elementSeedIndex -= 1
  if (elementSeedIndex == -1) loadTerrainModificationSelectionForm()
  else if (elementSeedForms[elementSeedIndex].selected) loadElementSeedForm()
  else loadPreviousElementSeedForm()
}

/*******************************************************************************
_____loadNextElementSeedForm_____
Description
    Moves to next selected element seed form or moves to terrain modification forms
*******************************************************************************/
function loadNextElementSeedForm() {
  currentState = "ElementSeedForm"
  elementSeedIndex += 1
  if (elementSeedIndex == elementSeedForms.length) loadNextTerrainModificationForm()
  else if (elementSeedForms[elementSeedIndex].selected) loadElementSeedForm()
  else loadNextElementSeedForm()
}

/*******************************************************************************
_____loadElementSeedForm_____
Description
    Loads in the form for the element seed at the current index
*******************************************************************************/
function loadElementSeedForm() {
  currentState = "ElementSeedForm"
  let elementType = elementSeedForms[elementSeedIndex]["element"]
  customInputsTitle.innerText = elementType
  customInputsContent.innerHTML = ""
  let formData = elementSeedForms[elementSeedIndex]["json"]["fields"]
  let formFields = buildFormFields(formData)
  for (let i in formFields) {
    let field = formFields[i]
    customInputsContent.appendChild(field)
  }
}

/*******************************************************************************
_____saveElementSeedForm_____
Description
    Saves the form for the current element seed
*******************************************************************************/
function saveElementSeedForm() {
  let elementType = elementSeedForms[elementSeedIndex]["element"]
  let formEntries = document.getElementById("custom-inputs").getElementsByClassName("custom-input")
  for (let i = 0; i < formEntries.length; i++) {
    let input = formEntries.item(i)
    let fieldName = input.id
    elementSeedForms[elementSeedIndex]["json"]["fields"][fieldName]["value"] = input.value
  }
}

/*******************************************************************************
_____loadPreviousTerrainModificationForm_____
Description
    Moves to previous terrain modification form or returns to previous element seed forms
*******************************************************************************/
function loadPreviousTerrainModificationForm() {
  currentState = "TerrainModificationForm"
  terrainModificationIndex -= 1
  if (terrainModificationIndex == -1) loadPreviousElementSeedForm()
  else if (terrainModificationForms[terrainModificationIndex]["selected"]) loadTerrainModificationForm()
  else loadPreviousTerrainModificationForm()
}

/*******************************************************************************
_____loadNextTerrainModificationForm_____
Description
    Moves to next terrain modification form or moves to anomaly forms
*******************************************************************************/
function loadNextTerrainModificationForm() {
  currentState = "TerrainModificationForm"
  terrainModificationIndex += 1
  if (terrainModificationIndex == terrainModificationForms.length) loadNextAnomalyForm()
  else if (terrainModificationForms[terrainModificationIndex]["selected"]) loadTerrainModificationForm()
  else loadNextTerrainModificationForm()
}

/*******************************************************************************
_____loadTerrainModificationForm_____
Description
    Loads in the form for the terrain modification at the current index
*******************************************************************************/
function loadTerrainModificationForm() {
  currentState = "TerrainModificationForm"
  let terrainModificationType = terrainModificationForms[terrainModificationIndex]["terrain-modification"]
  customInputsTitle.innerText = terrainModificationType + " " + (terrainModificationIndex - terrainModificationTypeIndexes[terrainModificationType] + 1)
  customInputsContent.innerHTML = ""
  let formData = terrainModificationForms[terrainModificationIndex]["json"]["fields"]
  let formFields = buildFormFields(formData)
  for (let i in formFields) {
    let field = formFields[i]
    customInputsContent.appendChild(field)
  }
}

/*******************************************************************************
_____saveTerrainModificationForm_____
Description
    Saves the form for the current terrain modification
*******************************************************************************/
function saveTerrainModificationForm() {
  let terrainModificationType = terrainModificationForms[terrainModificationIndex]["terrain-modification"]
  let formEntries = document.getElementById("custom-inputs").getElementsByClassName("custom-input")
  for (let i = 0; i < formEntries.length; i++) {
    let input = formEntries.item(i)
    let fieldName = input.id
    terrainModificationForms[terrainModificationIndex]["json"]["fields"][fieldName]["value"] = input.value
  }
}

/*******************************************************************************
_____loadPreviousAnomalyForm_____
Description
    Moves to previous anomaly form or returns to previous terrain modification forms
*******************************************************************************/
function loadPreviousAnomalyForm() {
  currentState = "AnomalyForm"
  anomalyIndex -= 1
  if (anomalyIndex == -1) loadPreviousTerrainModificationForm()
  else if (anomalyForms[anomalyIndex]["selected"]) loadAnomalyForm()
  else loadPreviousAnomalyForm()
}

/*******************************************************************************
_____loadNextAnomalyForm_____
Description
    Moves to next anomaly form or moves to review page
*******************************************************************************/
function loadNextAnomalyForm() {
  currentState = "AnomalyForm"
  anomalyIndex += 1
  if (anomalyIndex == anomalyForms.length) loadReviewPage()
  else if (anomalyForms[anomalyIndex]["selected"]) loadAnomalyForm()
  else loadNextAnomalyForm()
}

/*******************************************************************************
_____loadAnomalyForm_____
Description
    Loads in the form for the anomaly at the current index
*******************************************************************************/
function loadAnomalyForm() {
  currentState = "AnomalyForm"
  let anomalyType = anomalyForms[anomalyIndex]["anomaly"]
  customInputsTitle.innerText = anomalyType + " " + (anomalyIndex - anomalyTypeIndexes[anomalyType] + 1)
  customInputsContent.innerHTML = ""
  let formData = anomalyForms[anomalyIndex]["json"]["fields"]
  let formFields = buildFormFields(formData)
  for (let i in formFields) {
    let field = formFields[i]
    customInputsContent.appendChild(field)
  }
}

/*******************************************************************************
_____saveAnomalyForm_____
Description
    Saves the form for the current anomaly
*******************************************************************************/
function saveAnomalyForm() {
  let anomalyType = anomalyForms[anomalyIndex]["anomaly"]
  let formEntries = document.getElementById("custom-inputs").getElementsByClassName("custom-input")
  for (let i = 0; i < formEntries.length; i++) {
    let input = formEntries.item(i)
    let fieldName = input.id
    anomalyForms[anomalyIndex]["json"]["fields"][fieldName]["value"] = input.value
  }
}

/*******************************************************************************
_____loadReviewPage_____
Description
    Loads a page for the user to quickly review the environment before submiting
*******************************************************************************/
function loadReviewPage() {
  currentState = "ReviewForm"
  customInputsTitle.innerHTML = "Review"
  customInputsContent.innerHTML = ""
  document.getElementById("next-button").innerText = "Build Environment"
  // Environment Seeds
  for (let i = 0; i < elementSeedForms.length; i++) {
    if (elementSeedForms[i].selected == true && elementSeedForms[i].json["field-keys"]){
      let form = elementSeedForms[i]
      let title = form.element
      let titleId = title + "-review-header"
      let newTitle = document.createElement("h3")
      newTitle.setAttribute("id", titleId)
      newTitle.setAttribute("class", "review")
      newTitle.innerText = title
      customInputsContent.appendChild(newTitle)
      document.getElementById(titleId).addEventListener("click", () => goToFormPage("element-seed", i))
      let jsonData = form.json
      for (let j = 0; j < jsonData["field-keys"].length; j++) {
        let inputName = jsonData["field-keys"][j]
        let inputValue = jsonData["fields"][inputName]["value"]
        let inputUnit = jsonData["fields"][inputName]["unit"]
        let newInput = document.createElement("h4")
        newInput.setAttribute("class", "review")
        newInput.innerText = `${inputName}: ${inputValue} ${inputUnit}`
        customInputsContent.appendChild(newInput)
      }
    }
  }
  // Terrain Modifications
  for (let i = 0; i < terrainModificationForms.length; i++) {
    if (terrainModificationForms[i].selected == true && terrainModificationForms[i].json["field-keys"]){
      let form = terrainModificationForms[i]
      let title = form["terrain-modification"]
      let indexTitle = i - terrainModificationTypeIndexes[title] + 1
      let titleId = title + "-" + indexTitle + "-" + "-review-header"
      let newTitle = document.createElement("h3")
      newTitle.setAttribute("id", titleId)
      newTitle.setAttribute("class", "review")
      newTitle.innerText = title + " " + indexTitle
      customInputsContent.appendChild(newTitle)
      document.getElementById(titleId).addEventListener("click", () => goToFormPage("terrain-modification", i))
      let jsonData = form.json
      for (let j = 0; j < jsonData["field-keys"].length; j++) {
        let inputName = jsonData["field-keys"][j]
        let inputValue = jsonData["fields"][inputName]["value"]
        let inputUnit = jsonData["fields"][inputName]["unit"]
        let newInput = document.createElement("h4")
        newInput.setAttribute("class", "review")
        newInput.innerText = `${inputName}: ${inputValue} ${inputUnit}`
        customInputsContent.appendChild(newInput)
      }
    }
  }
  // Anomalies
  for (let i = 0; i < anomalyForms.length; i++) {
    if (anomalyForms[i].selected == true && anomalyForms[i].json["field-keys"]){
      let form = anomalyForms[i]
      let title = form["anomaly"]
      let indexTitle = i - anomalyTypeIndexes[title] + 1
      let titleId = title + "-" + indexTitle + "-" + "-review-header"
      let newTitle = document.createElement("h3")
      newTitle.setAttribute("id", titleId)
      newTitle.setAttribute("class", "review")
      newTitle.innerText = title + " " + indexTitle
      customInputsContent.appendChild(newTitle)
      document.getElementById(titleId).addEventListener("click", () => goToFormPage("anomaly", i))
      let jsonData = form.json
      for (let j = 0; j < jsonData["field-keys"].length; j++) {
        let inputName = jsonData["field-keys"][j]
        let inputValue = jsonData["fields"][inputName]["value"]
        let inputUnit = jsonData["fields"][inputName]["unit"]
        let newInput = document.createElement("h4")
        newInput.setAttribute("class", "review")
        newInput.innerText = `${inputName}: ${inputValue} ${inputUnit}`
        customInputsContent.appendChild(newInput)
      }
    }
  }
}

/*******************************************************************************
_____goToFormPage_____
Description
    Goes to a designated form page
Parameters
    formIndex:  index of the form in elementSeedForm list
*******************************************************************************/
function goToFormPage(formType, formIndex) {
  document.getElementById("next-button").innerText = "Next"
  switch(formType) {
    case "SelectAnomalyTypes":
          if (formIndex < elementSeedForms.length && elementSeedForms[formIndex].selected) {
            elementSeedIndex = formIndex
            terrainModificationIndex = -1
            anomalyIndex = -1
            loadElementSeedForm()
          }
          break;
    case "terrain-modification":
          if (formIndex < terrainModificationForms.length && terrainModificationForms[formIndex].selected) {
            elementSeedIndex = elementSeedForms.length
            terrainModificationIndex = formIndex
            anomalyIndex = -1
            loadTerrainModificationForm()
          }
          break;
    case "anomaly":
          if (formIndex < anomalyForms.length && anomalyForms[formIndex].selected) {
            elementSeedIndex = elementSeedForms.length
            terrainModificationIndex = terrainModificationForms.length
            anomalyIndex = formIndex
            loadAnomalyForm()
          }
          break;
  }
}

/*******************************************************************************
_____submitCustomEnvironment_____
Description
    Grabs all user input data and submits it to build a custom environment
*******************************************************************************/
function submitCustomEnvironment() {
  let basicInputs = getBasicInputs()
  let elements = []
  let elementSeeds = {}
  for (let i = 0; i < elementSeedForms.length; i++) {
    let seedForm = elementSeedForms[i]
    let elementType = seedForm["element"]
    if (seedForm["selected"]){
      elements.push(elementType)
      elementSeeds[elementType] = seedForm["json"]
    }
  }
  loadCustomEnvironment(basicInputs.name, basicInputs.height, basicInputs.width, elements, elementSeeds)
}

/*******************************************************************************
_____loadCustomEnvironment_____
Description
    Makes a server request for a custom environment and loads it into the visualizer
Parameters
    name:           associated name for random Environment
    height:         number of cells hgih the Environment will be
    width:          number of cells wide the Environment will be
    elements:       list of all elements to be included in the environment
    elementSeeds:   seed data for each element included
*******************************************************************************/
async function loadCustomEnvironment(name, height, width, elements, elementSeeds) {
  let customEnvironment = await buildCustomEnvironment(name, height, width, elements, elementSeeds)
  customEnvironment.json().then((json) => {
    let environment = formatEnvironment(json)
    console.log(environment)
    loadVisualizer(environment)
  }).catch((err) => { console.log(err) })
}

export {loadCustomEnvironmentForm}
