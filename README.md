# Distributed and Pervasive Systems

## 1.0 Iterative server for sums

Client sends message with two numbers to the server.
The server replies with the sum of these two numbers.

## 1.1 Multi-threaded server for sums

The same task but server should be multi-threaded.

# Project

Run it from the 'project_setup/' dir

```
./gradlew run --args="8090"
```

Expecting to see:

```
> Task :run
AdminServer listening on port 8090
<===========--> 85% EXECUTING [2m 10s]
> :run
```
