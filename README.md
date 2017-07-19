Bugrap
==============


Bugrap – an imaginary issue tracker


TL;DR - Running
===============
- mvn install
- cd bugrap-app
- mvn jetty:run
- (In browser) open http://localhost:8080

Project Structure
=================

The project consists of the following modules:

- parent project: common metadata and configuration
- distribution-bar: addon module, custom server and client side code 
- bugrap-app: main application module

Workflow
========

To compile the entire project, run "mvn install" in the parent project.

Other basic workflow steps:

- getting started
- compiling the whole project
  - run "mvn install" in parent project
- developing the application
  - edit code in the app module
  - run "mvn jetty:run" in app module
  - open http://localhost:8080
- adding add-ons to the project
  - edit POM in app module, add add-ons as dependencies
- client side changes
  - edit code in addon module
  - run "mvn install" in addon module
  - if a new add-on has an embedded theme, run "mvn vaadin:update-theme" in the app module
- debugging client side code
  - run "mvn vaadin:run-codeserver" in app module
  - activate Super Dev Mode in the debug window of the application
- enabling production mode
  - set vaadin.productionMode=true system property in the production server

