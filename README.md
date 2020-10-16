## CNN-Interface - A COMP 523 Project

### Project Summary
Convolutional neural networks (CNNs) can be used to analyze images of cells and annotate boundaries, instead of performing this tedious process by hand. We’re developing a UI for microscopists to quickly, efficiently annotate cell boundaries and make measurements using a CNN.

For more details, and to stay updated on our progress, please visit our website: https://tarheels.live/teamu/project/

# Cell Analysis CNN Interface ImageJ Plugin
Interface for Convolutional Neural Network to automatically annotate cell boundaries on microscope images.
## Installing and Running
You must have ImageJ installed as a prerequisite for this plugin. You can download it [here](https://imagej.nih.gov/ij/download.html).
### Steps
1. Move Project_X.java to plugins folder of ImageJ package.
2. In toolbar, navigate to Plugins > Compile and Run > Project_X.java.
3. Select Plugins > Project X from toolbar to run.
## Usage
1. In Start menu, click Upload an Image to browse for an image to analyze. Click Upload Model Directory to specify a CNN to use. Click Reset to undo selections or click Continue to advance to the interface.
2. Within interface, you should be able to see the image and sliders to make selections for z-stack, time course, and channel. After making selections, click Run CNN to annotate the image. At any time, you can click Reset to take you back to the Start screen to select a different image or CNN.

