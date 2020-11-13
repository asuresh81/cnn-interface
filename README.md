
# Cell Annotation with CNN

Convolutional neural networks (CNNs) can be used to analyze images of cells and annotate boundaries instead of performing this tedious process by hand. We’re developing a UI for microscopists to quickly and efficiently annotate cell boundaries and make measurements using a CNN.

<br>

## Getting Started
___

You must have ImageJ installed as a prerequisite for this plugin. You can download it here.

Python 3 must also be installed in order to utilize any CNN model with the plugin.

There are a number of Python packages that need to be installed in order to use the CNN model that we provide for users. In order to make the setup process simple, we provide a pip_requirements.txt file that can be run using this command 

    pip install -r pip_requirements.txt 

Usage: 
+ Download Project_X.java from repo
+ Move Project_X.java to the plugins folder of ImageJ package.
+ Open ImageJ and in the toolbar, navigate to Plugins > Compile and Run
    + Select Project_X.java and continue
+ Close all ImageJ windows and restart the program
+ Open ImageJ once more and Project X should now be an option under the Plugins section in the toolbar

<br>

## Testing
___

The test suite is currently set up in Eclipse to drive the UI elements written solely in Java using JUnit test cases. UI components that rely on ImageJ-specific methods are manually tested. If a user would like to run our test suite, they can find our test cases and Java-only interface code inside the test folder. They will need to set up their Eclipse environment in order to properly run the suite.

<br>

## Deployment
___

There is no separate production environment for our project. The staging/pre-production and production environments are the same: ImageJ. Our project can be run only as an ImageJ plugin. Developers can run the code by compiling the .java file in the GitHub repo in ImageJ. From there it can be run anytime as an ImageJ plugin. 

When fully deployed, our project utilizes the Java code that drives the interface as well as a Python CNN.

There is no CI/CD enabled for this project

<br>

## Technologies
___

The source code for the plugin is written in Java.

Related Architecture Diagram Records (ADRs) can be found inside the adr folder. 

It should also be worth noting that the CNN models intended to be used with the plugin should be written in Python. 

<br>

## Contributing
___

We have a GitHub repository which houses all of our current code, as well as a Trello board to keep us organized. The developer will need access to our repository to work on the project, and it would be ideal if they had access to the Trello board as well, but it is not necessary.

Any changes to the source code will require re-compilation using ImageJ. Please refer to the usage steps in the “Getting Started” section.

For more background information, please visit our project website [here](https://tarheels.live/teamu/).

<br>

## Authors
___

+ Leowell Bacudio
+ Adhithya Narayanan
+ Aditya Suresh

<br>

## License
___

There is no license necessary at this time. 

<br>

## Acknowledgements
___

We would like to extend our thanks to our professor, Jeff Terrell, our mentor, Stacey Wright, and our client, the JonesLab team at UNC.  