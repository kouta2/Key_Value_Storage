To compile the code, run the following bash script from inside CS425MP2 directory
>> bash compile.sh

Now, you need to start a rmi registry to enable rpc calls. This command only needs to be executed once on a machine and needs to be ran inside the class_files directory.
>> cd class_files/
>> rmiregistry 2001 &

To check if rmiregistry is already running on a machine, type the following command and look for a running process called "rmiregistry 2001 &":
>> ps aux | grep rmiregistry

As long as rmiregistry is running, then type the following command inside the class_files directory:
>> java main

To kill a client, simply send a SIGINT (CTRL+C). 

To kill a rmiregistry type:
>> ps aux | grep rmiregistry
look for the pid of the serving running and type:
>>kill <pid>
