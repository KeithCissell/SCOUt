import {pingServer,
        getCurrentState,
        newRandomEnvironment} from '../SCOUtAPI.js'
import {formatEnvironment} from '../environment/EnvironmentFormatter.js'
import {loadEnvironmentBuilderPage, loadVisualizerPage} from '../PageLoader.js'


async function buildRandomEnvironment(name, height, width) {
  let nre = await newRandomEnvironment(name, height, width)
  nre.json().then((json) => {
    let environment = formatEnvironment(json)
    console.log(environment)
    loadVisualizerPage(environment)
  }).catch((err) => { console.log(err) })
}

export {buildRandomEnvironment}
