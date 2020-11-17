
# Storage

## Summary

In order to store data persistently, we decided to use the user’s filesystem as it should be less complex and outputs should be more easily accessible to users. There does not seem to be a need for storage of data to be used by the application, so a database is not necessary in this case.

## Problem

We need a way of storing outputs from the Convolutional Neural Network after image analysis is done. This includes raw image data as well as analysis done by the CNN and/or the user. If users would like to revisit their analyses or verify the analysis done by a different researcher/team of researchers, they need a way of storing the outputs from the CNN and their own subsequent analyses. Therefore, we need a persistent storage solution.

## Constraints

The application should run locally and be easy to access and use, and the application should not be any more complex than it has to be, given that it is built as a plugin for an existing application and that its functions are relatively straightforward.

## Options

Option | Pros | Cons 
--- | --- | --- 
Local File System | <ul><li>Easy to read from and write to the filesystem in any programming language</li><li>Easy to understand and use for users who may not have a solid tech background</li><li>Output of the application is easily viewable in a universal application such as Excel</li></ul> | <ul><li>Less organized and standardized than a database</li></ul>
Proper Database | <ul><li>Can be used independently of the user’s file system to keep things separate and more organized</li></ul> | <ul><li>Can be more complicated to interact with for people who do not have tech backgrounds</li><li>Adds unnecessary complications during development including building an API to interact with it</li></ul>

## Rationale

There’s no need to use an actual database, as everything the user needs to use the tool, as well as the outputs from running the tool, are easily stored on the user’s machine. Since the tool runs locally, it’s easier and more convenient for the user if the data is all in one place and with the input files coming from the local filesystem, and it makes sense that any output data would be stored there as well.
