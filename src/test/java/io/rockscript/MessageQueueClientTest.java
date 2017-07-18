package io.rockscript;

import java.io.IOException;
import java.util.concurrent.*;

import com.rabbitmq.client.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Pre-requisites: install (brew install rabbitmq) and run RabbitMQ on the default server and port (rabbitmq-server).
 */
public class MessageQueueClientTest {

  private static final String QUEUE_NAME = "test";
  private static Channel channel;
  private static Connection connection;
  private CompletableFuture<String> lastMessage;

  @BeforeClass
  public static void connectToMessageQueue() throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    connection = factory.newConnection();
    channel = connection.createChannel();
  }

  @AfterClass
  public static void closeConnection() throws IOException, TimeoutException {
    channel.close();
    connection.close();
  }

  @Before
  public void declareQueue() throws IOException {
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
  }

  @Before
  public void addConsumer() throws IOException {
    lastMessage = new CompletableFuture<>();
    Consumer consumer = new DefaultConsumer(channel) {

      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        String message = new String(body, "UTF-8");
        lastMessage.complete(message);
      }
    };
    channel.basicConsume(QUEUE_NAME, true, consumer);
  }

  @Test
  public void testMessageSends() throws IOException, ExecutionException, InterruptedException {
    // When I publish a message to the queue
    String message = "Hello World!";
    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

    // Then the future does not complete immediately
    assertFalse(lastMessage.isDone());

    // When I wait for the future to complete
    String lastMessageReceived = lastMessage.get();

    // Then the expected message was received
    assertEquals(message, lastMessageReceived);
  }
}
