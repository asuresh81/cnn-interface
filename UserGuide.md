Convolutional neural networks (CNNs) can be used to analyze images of cells and annotate boundaries, instead of performing this tedious process by hand. We’re developing a UI for microscopists to quickly, efficiently annotate cell boundaries and make measurements using a CNN.

For more details, and to stay updated on our progress, please visit our website: https://tarheels.live/teamu/project/

# Cell Analysis CNN Interface ImageJ Plugin
Interface for Convolutional Neural Network to automatically annotate cell boundaries on microscope images.

## Installing and Running
You must have ImageJ installed as a prerequisite for this plugin. You can download it [here](https://imagej.nih.gov/ij/download.html).

You must also have Python 3 installed on your local machine to run the CNN. You can download it [here](https://www.python.org/downloads/). The specific Python 3.x.x. version you will need depends on the Python file used to run your saved CNN model. If the Python file th
at runs your CNN has code specific to a version of Python, you will need to have that version of Python installed. We don't support Python 2, but any version of Python 3.6 or greater should work with the CNN that we have provided.

To find the path of your Python 3 for Mac or Linux users, you can use the command:

```
which python3
```

The equivalent in the Windows command line would be:
```
where python3
```

### Steps
1. Open ImageJ. In the toolbar, navigate to Edit > Options > Compiler... and ensure that the Target is set to 1.8. If not, set it to 1.8.
2. In toolbar, navigate to Plugins > Install > CNN_Annotation.java.
3. Install CNN_Annotation.java within the plugins folder of the ImageJ application. We recommend creating a new folder within plugins named CNN Annotation, and installing CNN_Annotation within that folder.
4. ImageJ should run the plugin automatically. In the future, you can now select Plugins > CNN Annotation from toolbar to run.

## Usage
1. In Start menu, click Upload an Image to browse for an image to analyze. Click Upload Model Directory to specify a CNN to use. Click Reset to undo selections or click Continue to advance to the interface.
2. Within interface, you should be able to see the image and sliders to make selections for z-stack, time course, and channel. After making selections, click Run CNN to annotate the image. At any time, you can click Reset to take you back to the Start screen to select a different image or CNN.
3. To accept the results of the CNN, select Accept CNN on the right side panel under the CNN output. If you are unsatisfied with the result, you can select Deny and Edit Results. If you wish to undo your choice, you can click Return.
4. If you chose to Deny and Edit Results, if you want to manually annotate the image and make measurements, click Manual Annotation and use the opened ImageJ window with the ImageJ bar containing the drawing tools to annotate your image. You can make measurements by navigating to Analyze > Measure in the toolbar. You can choose to save these results if you wish to do so.