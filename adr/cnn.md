
# CNN

## Summary

In order to use the CNN we decided to integrate it within the interface by invoking the Python script from Java rather than creating an API to provide the input and receive the output. This would avoid unnecessary complications in the application and would be more efficient to develop.

## Problem

The overall purpose of this interface is for users to be able to utilize the CNN developed by the JonesLab team without having to directly deal with it. We need a way of interacting with the CNN from the interface.

## Constraints

The CNN is written in Python and our application will be written in Java, so we need a way of interacting with and executing the Python code from our Java-based interface.

## Options

Option | Pros | Cons 
--- | --- | --- 
Develop an API | <ul><li>Method of interacting with the CNN that is programming language/framework agnostic</li></ul> | <ul><li>We would need to create the API, which would be another task we’d have to do, increasing the development time</li></ul>
Integrate within interface | <ul><li>Less work needed on our end in order to run the CNN</li><li>Can be called from within the interface using a Java library that allows for calling Python scripts</li></ul> | <ul><li>We have to invoke python within Java, which is not ideal</li></ul>

## Rationale

We decided to directly integrate the CNN into our interface and use python interpreters in our application in order to call the CNN as a script and collect its outputs. Jython is an existing Java implementation of Python, which is already used in ImageJ, and therefore should be straightforward to use and consistent with ImageJ. Additionally, this avoids complications such as creating an Application Programming Interface and maintains the simplicity of the application while allowing us to develop it much more efficiently.
