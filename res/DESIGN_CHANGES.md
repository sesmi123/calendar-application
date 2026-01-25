# Design Changes Overview

## Model
1. Collection of events was changed from HashSet to a TreeSet in order to make the lookup/search of events faster (O(log n)).
2. AbstractCalendarExporter which implements the existing CalendarExporter was created since now there is more than one exporter, namely - CsvCalendarExporter and IcalCalendarExporter. This was an inevitable change because we should not create an abstract class in anticipation of new implementations of the interface, rather Abstract classes are created only when there are multiple implementations that we want to abstract. The new ical exporter implements the existing CalendarExporter interface.
3. New interface for CalendarManger was created to support the management of multiple calendars.
4. Added 3 new incremental methods to the Calendar interface to support the 3 kinds of Copy operations.

## Controller
No changes in controller as such as it was designed to be extensible. Only the parser, which the controller uses to parse the input commands, was extended with capabilities:
1. Switch case was replaced with map in order to avoid cognitive complexity of if-else/switch statements and easy lookup/maintenance of the Command objects.
2. Extended to parse the new set of supported commands.

## View
No changes in view as it was designed to be extensible (even a GUI support in the future could be easily supported by implementing the ObservableView interface and replacing the view passed to the controller).
