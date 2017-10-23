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

export {empty2D}
