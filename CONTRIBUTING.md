# Contributing to _conf4j_

You have found a bug or you have an idea for a cool new feature? Contributing code is a great way to give something
back to the open source community. Before you dig right into the code there are a few guidelines that we need
contributors to follow so that we can have a chance of keeping on top of things.

## Getting Started

* Make sure you have a [GitHub account](https://github.com/signup/free).
* If you're planning to implement a bigger feature it makes sense to discuss your changes first.
  Please create an issue and explain the intended change.
  This way you can make sure you're not wasting your time on something that isn't considered to be in
  _con4j_ scope.
* Submit a ticket for your issue, assuming one does not already exist.
  * Clearly describe the issue including steps to reproduce when it is a bug.
  * Make sure you fill in the earliest version that you know has the issue.
* Fork the repository on GitHub.

## Making Changes

* Create a topic branch from where you want to base your work (this is usually the master/trunk branch).
* Make commits of logical units.
* Respect the original code style:
  * Only use spaces for indentation.
  * Create minimal diffs - disable on save actions like reformat source code or organize imports.
    If you feel the source code should be reformatted create a separate PR for this change.
  * Check for unnecessary whitespace with git diff --check before committing.
* Make sure your commit messages are in the proper format.
* Make sure you have added the necessary tests for your changes.
* Run all the tests with `mvn clean verify` to assure nothing else was accidentally broken.

## Submitting Changes

* Sign the [Contributor License Agreement][cla] if you haven't already.
* Push your changes to a topic branch in your fork of the repository.
* Submit a pull request to the repository.

## Additional Resources

+ [Contributor License Agreement][cla]
+ [General GitHub documentation](https://help.github.com/)
+ [GitHub pull request documentation](https://help.github.com/send-pull-requests/)

[cla]:https://cla-assistant.io/
