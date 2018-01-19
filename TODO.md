# Goal: expand script language support for real use cases

* if-then-else
* assignment (also of dereferenced properties)
* for loop

# Goal: add concurrency support

* system.resolveConcurrent({
  responseOne: http.get({...}),
  responseTwo: http.post({...})
}); 
* array.forEachConcurrent(function)  (this requires functions)
 
# Goal: add persistence 

# Goal: replication 
