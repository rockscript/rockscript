#### Set up

RockScript comes in the form of a server.  The server stores scripts and script executions
and it also performs the script executions.

RockScript has out of the box services such as plain HTTP with functions get, post, put
and delete.

It's easy to plug in your own services. A service bridge is a component you write in any
programming language and host separately from the RockScript server.  As long as it complies
with the HTTP based RockScript service function SPI, you can make any REST API, SaaS product
API or other operation available in RockScript as a service function.

#### Built-in services

#### The Service function SPI

TODO explain 
