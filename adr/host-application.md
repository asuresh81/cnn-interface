
# Host Application

## Summary

In order to develop our interface that meets our client’s constraints, we decided to build an ImageJ plugin in Java. This plugin will be easily available and easy to use for microscopists who are already using ImageJ, as it is a standard application that these researchers use.

## Problem

We need to develop our interface using a platform that is easy to use and will function across operating systems seamlessly. It should be an application that requires minimal technical knowledge as users will likely have a wide range of technical backgrounds. We need to identify the best platform on which to develop our interface so that it can be easily used and widely accessed.

## Constraints

The client preferred that our project is open source and easy to access and use. Additionally, the client wanted an application that would run locally on the user’s system, as opposed to an application hosted on the web so it needs to work independently of the host operating system. The application should be usable on all operating systems.

## Options

Option | Pros | Cons 
--- | --- | --- 
Embed ImageJ into an external, standalone application | <ul><li>Standalone applications give us more control over what programming languages/frameworks we use (both in terms of functionality and visual aesthetics)</li><li>We can embed ImageJ within the application so that we can still take advantage of the robust image analysis tools that already exist</li></ul> | <ul><li>Longer development time</li><li>Have to build the application in a way that is operating system agnostic, which can restrict our language/framework options anyway</li><li>Users would have to download another application instead of using one they already know well</li><li>Embedding ImageJ into a standalone application seems to be difficult, not a lot of references for guidance</li></ul>
Replicate ImageJ’s image analysis capabilities in an external, standalone application | <ul><li>Maintains the pros of creating the standalone application</li><li>By replicating the specific ImageJ tooling we need, we can avoid dealing with the challenges that come from attempting to embed ImageJ</li></ul> | <ul><li>Replicating ImageJ capabilities has a steep learning curve and will lead to significantly more challenging and time-consuming development</li><li>Suffers from the same cons as embedding ImageJ into the application</li></ul>
Build directly into ImageJ as a plugin | <ul><li>Standard for all plugins, existing documentation on how to develop a plugin</li><li>More accessible and easier to use for researchers who already use ImageJ</li><li>Already open source</li></ul> | <ul><li>One option for application design and architecture</li><li>Might face some issues interacting with the CNN, written in Python since the plugins are written in Java</li></ul>

## Rationale

ImageJ is an application that provides tooling for image analysis and is widely used in the microscopy community. By building our application as an ImageJ plugin we will be able to make use of the sophisticated image analysis tools that already exist in ImageJ. We will be able to cater more seamlessly to a wider audience and meet the JonesLab team’s desire to develop an open-source interface. Additionally, ImageJ and its plugins are written in Java as a standard, and therefore this plugin will be written in Java as well.
