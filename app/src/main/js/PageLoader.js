import {loadVisualizer} from './visualizer/Visualizer.js'

const main = document.getElementById("main")

function loadVisualizerPage(environment) {
  main.innerHTML = `
    <div id="navigation">
      <h1 id="message"></h1>
      <p id="content"></p>
    </div>
    <div id="toolbar">
      <h1 class="sidebar">Controls</h1>
      <h2 class="sidebar" id="layer-toggles-header"></h2>
      <div class="toolbar-container" id="layer-toggles"></div>
      <h2 class="sidebar" id="layer-selector-header"></h2>
      <div class="toolbar-container" id="layer-selector"></div>
    </div>
    <svg id="display"></svg>
    <div id="legend">
      <h1 class="sidebar">Legend</h1>
      <h2 class="sidebar" id="legend-environment-title"></h2>
      <table class="legend-table" id="legend-main-table"></table>
      <h2 class="sidebar" id="legend-layer-title"></h2>
      <table class="legend-table" id="legend-selected-layer-table"></table>
      <h2 class="sidebar" id="legend-cell-title"></h2>
      <table class="legend-cell-table" id="legend-selected-cell-table"></table>
    </div>
  `
  loadVisualizer(environment)
}

export {loadVisualizerPage}
