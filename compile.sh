#!/bin/bash
javac -d class_files main.java Executor.java Stabilizer.java AcceptRPCConnections.java ConnectToOtherRPCs.java RPCFunctions.java FailureDetector.java
# rmiregistry 2001 &
