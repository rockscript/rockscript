Extends https://github.com/sinetja/netty-router to an
easy to use Router based on https://netty.io/.

Usage:

```
Server server = new Server(new ServerConfiguration()
  .scan(SomeHttpPost.class)
  .scan(SomeHttpGet.class)
);

server.startup();
```

Where SomeHttpPost looks like this:

```
@Post("/some/url")
public class SomeHttpPost extends RequestHandler {

  @Override
  public void handle() {
    RequestBodyJsonBean command = command.getBodyJson(RequestBodyJsonBean.class);

    ... command handling code ...

    response.statusOk();
  }

}
```