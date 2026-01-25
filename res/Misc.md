# Design Changes Overview

## Model
No changes made to the Model (as the Model is completely isolated from the View).

## Controller
1. Created new Features interface which contains all the features supported by the GUI (invoked as callbacks):
    - Features interface is implemented by the Controller (ControllerImpl)
    - callback methods validate the input arguments and obtain the corresponding command object from command factory
    - command object's execute method is invoked

## View
1. GuiView Interface was created which extends the old ObservableView (following Open/Closed principle) to add addFeatures method
    - addFeatures method allows to add callbacks through which view communicates with the controller (ensuring view only has limited access to controller)
    - SwingGuiView is made up of several smaller components placed inside views/gui
    - this follows proper component composition and separation of concerns with each component having single responsibility (NorthPanel for navigation, WestPanel for calendar list, CenterPanel for month view, etc.)

---

**Note:**
- All features of the calendar as per the given requirements work to the best of our knowledge
- Design was kept extensible as much as possible right from Assignment 4 and adheres to SOLID principles (via: command design in controller, interface segregation through ObservableCalendar, composition in view components, builder in constructing events)
- Isolation/Coupling kept to a minimum between Model, View and Controller