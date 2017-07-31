

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
