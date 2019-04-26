Part a) Creates a single thread for each request
Part b) execute each request by calling a thread from the threadpool

ThreadPool has three fixed thread.

The serviceRequest class will execute the request. Thread sleep for 5s before exit.

The difference between part a) and b) is that in the ThreadName:
a) Thread 1
b) Thread pool 1 Thread 1

a) runs sequentially
b) runs concurrently

For execution:

javac DateServer430.java

javac DateClient430.java

// & to run in the background

java DateServer430 &

java DateClient430 

For killing the process

// Look for the process ID (PID) that connect to your port

netstat -tulpn

// Kill it. -9 for extra killing fatality style (cannot be blocked) 

kill -9 PID
