# SCOUt
__S__ urveillance __C__ oordination and __O__ perations __Ut__ ility

## Description
_This is an independent research project in the field of AI I am conducting as a student at Missouri State University_

The purpose of this project is to create an artificially intelligent "mind" to make observational decisions for a mobile robot. The mobile robot will have a set of sensors with which it can survey its surrounding environment. It will store this observed data, perform an analysis and plan its next move accordingly. The plan is to drop a robot into unfamiliar environments with a set goal to achieve and let it learn on its own the best way to approach the given situation. Usages can be as simple as mapping out an unknown area, to as difficult as searching for survivors after a natural disaster. The robot will have internal and external limitations it must work with such as battery life and terrain obstacles. The goal is to create AI that can learn how to use its observational skills to achieve a goal within a dynamic environment.

__[Get Started](#project-guide)__ - installation and operation guide for the project

## Project Layout
Simulation environments will be created to test and train the SCOUt AI. To assist with observing and debugging the AI's behavior, a visual client will be required. This will stretch the project across multiple languages and libraries of code. The development of this project will be split into several major steps. Continuous refinement and adjustment of previous steps will be made along the way to allow smooth integration of each new step.

__Setps__
1. Develop a data representation of an environment
2. Develop a method of communication to a visual client
3. Develop a visual client to represent the environment data
4. Develop a robot that can naively traverse and react to an environment
5. Develop AI that allows the robot to learn how to perform tasks in an environment

### Environments
The environments will be procedurally generated, stored and manipulated in [Scala](https://www.scala-lang.org/) language.

An _Environment_ holds a 2D grid of evenly distributed cells. Each _Cell_ represents a physical location in the environment. Cells hold their relative position along with a list of environmental elements at their location. An _Element_ holds a value (if known) along with relative information such as the unit of measurement and if its value is constant or dynamic to the environment.

__Environment Data Structure Overview__
```
Environment
  name
  grid
    row1
      Cell1
        x
        y
        elements
          elevation
          temperature
          longitude
          latitude
          ...
      Cell2
      ...
    row2
    ...              
```

### Communication
[http4s](http://http4s.org/) is used to create a Scala server to allow communication of data to the visualization framework. Communication will be handled using JSON formatted text via HTTP requests.

__Local Server:__ `http://localhost:8080`

__Request Paths:__
```
PATH                      TYPE    DESCRIPTION
/ping                     GET     Simply returns "pong"
/current_state            GET     Fetches the current state of the environment
/new_random_environment   POST    Takes a name, length and width for a random grid to create
```

### Visual Platform
[Electron](https://electron.atom.io/) is used for a visualization framework. It emulates a browser window as a desktop application. This allows the usage of JavaScript and HTML for front-end data storage and presentation.

JavaScript code communicates with the Scala server to retrieve and request manipulation of an _Environment_.

I plan to use [Contour Plot](https://bl.ocks.org/mbostock/4241134) and [Heatmap](https://bl.ocks.org/mbostock/3074470) from the D3.js library to display one layer of elements at a time. Eventually I would like to find a way to comfortably show multiple layers of elements at a time.

## Project Guide
todo
