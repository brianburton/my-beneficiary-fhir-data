package gov.cms.bfd.migrator.app;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.cms.bfd.AbstractLocalStackTest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;

/** Integration tests for {@link SqsDao}. */
class SqsDaoIT extends AbstractLocalStackTest {
  /** Will be connected to the localstack SQS service. */
  private SqsDao dao;

  /** Create the {@link SqsDao} connected to our localstack SQS service. */
  @BeforeEach
  void setUp() {
    SqsClient client = createSqsClientForLocalStack(localstack);
    dao = new SqsDao(client);
  }

  /** Test creating a queue. */
  @Test
  void createQueue() {
    String queueName = "my-created-queue.fifo";
    String createdQueueUri = dao.createFifoQueue(queueName);
    String lookupQueueUri = dao.lookupQueueUrl(queueName);
    assertEquals(createdQueueUri, lookupQueueUri);
  }

  /** Test sending and receiving. */
  @Test
  void sendAndReceiveMessages() {
    String queueName = "my-test-queue.fifo";
    String queueUri = dao.createFifoQueue(queueName);
    String messageGroupId = "sendAndReceiveMessages";
    String message1 = "this is a first message";
    String message2 = "this is a second message";
    dao.sendMessage(queueUri, messageGroupId, "1", message1);
    dao.sendMessage(queueUri, messageGroupId, "2", message2);
    assertEquals(Optional.of(message1), dao.nextMessage(queueUri));
    assertEquals(Optional.of(message2), dao.nextMessage(queueUri));
    assertEquals(Optional.empty(), dao.nextMessage(queueUri));
  }

  /** Test conditions that can throw {@link QueueDoesNotExistException}. */
  @Test
  void variousNonExistentQueueScenarios() {
    assertThatThrownBy(() -> dao.lookupQueueUrl("no-such-queue-exists"))
        .isInstanceOf(QueueDoesNotExistException.class);
    assertThatThrownBy(
            () -> dao.sendMessage("no-such-queue-exists", "g1", "m1", "not gonna make it there"))
        .isInstanceOf(QueueDoesNotExistException.class);
    assertThatThrownBy(() -> dao.nextMessage("no-such-queue-exists"))
        .isInstanceOf(QueueDoesNotExistException.class);
  }

  /**
   * Create a {@link SqsClient} configured for the SQS service in the provided {@link
   * LocalStackContainer}.
   *
   * @param localstack the container info
   * @return the client
   */
  static SqsClient createSqsClientForLocalStack(LocalStackContainer localstack) {
    return SqsClient.builder()
        .region(Region.of(localstack.getRegion()))
        .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.SQS))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())))
        .build();
  }
}
