A RockScript server stores a collection of scripts and each script has a collection
of script versions.  Each script execution runs against a particular script version.
Once started, script executions keep running in the same script version.

## Why

The RockScript server needs to store the scripts for 2 reasons:

1. Service functions can take a long time to complete.  While the script execution 
 is waiting, the execution state as well as the script version are persisted and 
 the server may evict those from the cache.  So when the service bridge signals 
 back to the RockScript server that the service function invocation is done, 
 the script version is necessary to deserialize the script execution runtime 
 state before the script execution can be resumed.
2. RockScript is designed with clustering in mind.  Callback that signal completion 
 of service functions may arrive at a different node of the cluster.  So the 
 in that case script execution also needs to continue from persistent storage 
 as well.

## Deploying scripts

The most common way to make scripts available for execution on the server is 
using the deploy command.   When we deploy the script to the server, we 

* Ensure a script exists. If no script with that name exists yet, it is created.
* Create a new script version with the given text and associate it with the script.
* Mark the new script version as the active script version.  
  
## The active script version 

The active script version is the version that is started when a script 
is specified with the `Start script` command.  The active script version 
is typically the latest version of a script.  That's why the deploy command 
is the most common.

With the `saveScript` command, it's also possible to save a new script without 
making it the active script version.

## Deleting old versions

Old versions of the script need to be kept on the server for as long as you 
want to keep the executions executed in those respective script versions.
