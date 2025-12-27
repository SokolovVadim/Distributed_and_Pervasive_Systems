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

Open second terminal, type

```angular2html
./gradlew runPlant1
```
Expect to see

```angular2html
 ./gradlew runPlant1                                         1 ↵  1133  15:46:35 
Starting a Gradle Daemon, 1 busy and 1 stopped Daemons could not be reused, use --status for details

> Task :runPlant1
Starting plant id=plant1, grpcPort=30031, admin=localhost:8090
Registered successfully. Current plants in the system:
  - PlantInfo{id='plant1', host='localhost', grpcPort=30031}
ThermalPlant is now idle. Press Control+C to stop.
<===========--> 85% EXECUTING [4m 41s]
> :runPlant1
```

Open the third terminal

```angular2html
./gradlew runPlant2
```

Expecting to see 

```angular2html
./gradlew runPlant2                                         1 ↵  1134  15:46:49 
Starting a Gradle Daemon, 2 busy and 1 stopped Daemons could not be reused, use --status for details

> Task :runPlant2
Starting plant id=plant2, grpcPort=30032, admin=localhost:8090
Registered successfully. Current plants in the system:
  - PlantInfo{id='plant1', host='localhost', grpcPort=30031}
  - PlantInfo{id='plant2', host='localhost', grpcPort=30032}
ThermalPlant is now idle. Press Control+C to stop.
<===========--> 85% EXECUTING [3m 45s]
> :runPlant2
```
