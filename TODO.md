# Goal: enable replication through event state transfer 

### Strategy

* Ensure all updates to stores are done through the event stream
* Create central event dispatcher
* Create shortcut to dispatch events to local stores
* Create kafka dispatcher for replication
* Create kafka consumer that pushes remote events to local stores

### Tasks

* Create ScriptExecutionStore
  * In-memory version of a SCRIPT_EXECUTION_TABLE
  
* Migrate all the script execution queries to ScriptExecutionStore

* Transform EventStore to a LocalEventDispatcher 

* Create Job events

* Create single EventListener that dispatches the events to  
  * ScriptStore
  * ScriptExecutionStore
  * JobStore
