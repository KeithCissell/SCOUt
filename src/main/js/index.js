window.$ = window.jQuery = require('jquery');
const fetch = require('node-fetch');


const header = document.getElementById("Header");


setTimeout(function() { testCall() }, 30000);


let myHeaders = new Headers();

let myInit = { method: 'GET',
               headers: myHeaders,
               mode: 'cors',
               cache: 'default' };

let myRequest = new Request('http://localhost:8080/ping', myInit);

function testCall() {
  fetch('http://localhost:8080/ping', myInit).then(function(response) {
    header.innerHTML = "We have contact!!";
    console.log(response.json());
  })
}
