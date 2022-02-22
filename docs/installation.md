# Installation

## Step 1: Install a Java Runtime Environment

You will need a Java Runtime Environment (JRE) to run Redmatch. To install a JRE go to [https://openjdk.java.net/install/](https://openjdk.java.net/install/) and follow the installation instructions for your platform.

Make sure a JRE is correctly installed by opening a command window and typing the following command:

```
java -version
```
The command should return a version number.

## Step 2: Install the Visual Studio Code Extension

A Visual Studio Code extension is now available in the Visual Studio Marketplace. In the extensions tab, search for 'Redmatch' and then click on the __Install__ button. Once the extension is installed, Visual Studio Code will recognise `.rdm` files and will automatically do syntax highlighting, validation and auto-completion. You can also run the transformation and generate a graph visualisation of the FHIR resources by right-clicking on a Redmatch rules file.


Alternatively, a ```.vsix``` file is distributed as part of the Redmatch release in Github. The current version can be downloaded from the [releases page](https://github.com/aehrc/redmatch/releases). The extension can be installed using the `vsce` tool (instructions are available at [https://code.visualstudio.com/api/working-with-extensions/publishing-extension#packaging-extensions](https://code.visualstudio.com/api/working-with-extensions/publishing-extension#packaging-extensions)). 

[Home](./index.html)
