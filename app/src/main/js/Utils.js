// Utility functions for javascript code

/*******************************************************************************
_____empty2D_____
Description
    Builds and returns a 2-D array of null elements
Parameters
    length:   size of the 1st matrix dimension
    width:    size of the 2nd matrix dimension
*******************************************************************************/
function empty2D(length, width) {
  let empty2D = []
  for (let i = 0; i < length; i++) {
    empty2D.push([])
    for (let j = 0; j < width; j++) {
      empty2D[i].push(null)
    }
  }
  return empty2D
}

/*******************************************************************************
_____roundDecimalX_____
Description
    Rounds a double or float to X decimal values
Parameters
    num:    double or float to round
    x:      number of decimal places to round to (must be > 0)
*******************************************************************************/
function roundDecimalX(num, x) {
  if (x > 0) {
    let rounder = 10 ** x
    return Math.round(num * rounder) / rounder
  } else return num

}

export {empty2D, roundDecimalX}
