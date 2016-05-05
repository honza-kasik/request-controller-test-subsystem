## About
This test subsystem should be added as a module (similar to picketlink) when tested and then should called predefined management operation which will run tests.

### Why subsystem
It looks like request controller is not visible (not possible to inject) to deployed application, so the subsystem was second choice.

## What is done
Almost nothing. There was a lot of things with higher priority, although test operation class should be partially working. This subsystem has never been built and run.

## What's next and where to start?
I found out the right inspiration in small subsystems, already integrated in Wildfly - such as request controller itself or in this [nice article about example subsystem](https://docs.jboss.org/author/display/WFLY10/Example+subsystem), although methods used there are mostly depracated it is a good place where to start (probably only place too).