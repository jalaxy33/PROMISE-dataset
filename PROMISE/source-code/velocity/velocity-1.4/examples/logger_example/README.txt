Welcome to Velocity!

As always, the if you have any questions :

1) Make sure you followed any directions :)  (did you build everything?)
2) Review documentation included in this package, or online at
      http://jakarta.apache.org/velocity
3) Ask on the velocity-user list.  This is a great source of support information.
   To join, read http://jakarta.apache.org/site/mail.html and then follow the 
   link at the bottom to join the lists.

logger_example
------------
This 'toy' example shows how to use the Velocity logger interface
to have any class function as a logging facility.  

To run :

./logger_example.sh

or 

logger_example.bat


Log4jCategoryExample
--------------------
This class demonstrates how to configure Velocity to use an 
 existing log4j category for logging.

No batchfiles are provided as this is considered an advanced example
(and we want to stop maintaining these batch files :)

To test this example, add the jars

   bin/velocity-dep-1.x.jar

and 

  build/lib/log4j-core-X.jar 

to your classpath, and compile and run.  You will see that the log4j
output will contain the output from velocity's initialization.
