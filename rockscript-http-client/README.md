Fluent API for making HTTP calls synchronously.

This customized version of Apache (synchronous/blocking) http client fluent api: 
 - Throws runtime exceptions instead of checked exceptions.
 - Binds the latest server exception if a client exception is thrown.
 - Allows to specify a converter to write objects to the command body and read objects from the response body 
