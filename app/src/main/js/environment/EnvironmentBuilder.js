import {pingServer,
        getCurrentState,
        newRandomEnvironment} from '../SCOUtAPI.js'
import {formatEnvironment} from './EnvironmentFormatter.js'


function buildRandomEnvironment(name, height, width) {
  let nre = newRandomEnvironment(name, height, width)
  console.log(nre)
  let environment = formatEnvironment(nre)
  return(environment)
  // nre.json().then((json) => {
  //   let environment = formatEnvironment(json)
  //   console.log(environment)
  //   return environment
  // }).catch((err) => { throw new Error(err) })
}

export {buildRandomEnvironment}
