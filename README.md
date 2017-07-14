# SCOUt
Surveillance Coordination and Operations Utility

## Description
This is an independent research project in the field of AI through Missouri State University for CSC 596.

The purpose of this project is to create an artificially intelligent "mind" to make observational decisions for a mobile robot. The idea is a robot is dropped into an environment that it knows nothing about, with a set goal to achieve. Example usages can be as simple as mapping out the environment to as complex as searching for survivors after a natural disaster. The robot will have internal and external limitations it must work with such as batter life and terrain obstacles respectively. The goal is to create the AI "mind" in such a way that it is able to achieve it's objective as efficiently as possible.

## Project Layout
In order to test out AI designs I will need to create fake environments for the robot to work within. There will also need to be a visual framework to display the robot maneuvering the environment. The visual framework will also need the ability to display details about the environment to aid in human comprehension with the AI's decision making process.

### Environments
The environments will be procedurally generated and stored using Scala. The data will be need to be relayed to a visual framework. To do so, there will be a server setup using Scala to allow communication of data using Json formatted text.

### Visual Display
I plan to use JavaScript in an Electron window for a visual display framework. The JavaScript will communicate with the Scala Server in order to retrieve and manipulate any of the Scala data structures used. The data retrieved from the Scala server will then need to be duplicated into JS data structures as well to allow usage for generating the display.

## Data Structure

```
Environment ---> Cells ---> Variables
```
Each environment contains a 2D grid of cells. Each cell in turn holds a position relative to the cell grid, as well as any environmental variables present at their location. A variable holds a single value (such as a temperature reading) along with information such as its name, unit and mutability.
