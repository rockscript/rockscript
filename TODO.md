# Goal: expand script language support for real use cases

* comparison boolean expressions for if-then-else
* == & ===
* <, >, <=, >= etc
* Update + conversions
* for loop

# Goal: add concurrency support

* system.resolveConcurrent({
  responseOne: http.get({...}),
  responseTwo: http.post({...})
}); 
* array.forEachConcurrent(function)  (this requires functions)

# Goal: workers for cloud functions

Eg
* S3 operations
* Invoke AWS lambda
* Invoke Google cloud function 

# Goal: add persistence 

# Goal: replication 
