# Package Delivery

You can run it as a standard console application by "main" method of the PackageDelivery class. It accepts one
command line argument, which is name of a file with initial load. Once the application is running, it is waiting
for an input in the following format:

<weight: positive number, >0, maximal 3 decimal places, . (dot) as decimal separator><space><postal code: fixed 5 digits>

Input is validated via regular expression (^\d*\.?\d{0,3}\s\d{5}$).
Once per minute it writes a summary to console, which is not very good solution, since it can interfere with the user
input :). Application can be terminated by entering "quit" command.
