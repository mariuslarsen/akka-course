package blockchaincasestudy.blockchain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import blockchaincasestudy.model.Block;
import blockchaincasestudy.model.HashResult;
import blockchaincasestudy.utils.BlocksData;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

class WorkerBehaviorTest {

  @Test
  void miningFailsIfNonceIsInRange() {
    BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(
        WorkerBehavior.create());
    TestInbox<HashResult> testInbox = TestInbox.create();
    Block block = BlocksData.getNextBlock(0, "0");
    WorkerBehavior.Command message = new WorkerBehavior.Command(block, 0, 5, testInbox.getRef());

    testActor.run(message);
    List<CapturedLogEvent> logMessages = testActor.getAllLogEntries();

    assertEquals(0, testInbox.getAllReceived().size());
    assertEquals(1, logMessages.size());
    assertEquals("null", logMessages.get(0).message());
    assertEquals(Level.DEBUG, logMessages.get(0).level());
  }

  @Test
  void miningPassesIfNonceIsInRange() {
    BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(
        WorkerBehavior.create());
    TestInbox<HashResult> testInbox = TestInbox.create();
    Block block = BlocksData.getNextBlock(0, "0");
    WorkerBehavior.Command message = new WorkerBehavior.Command(block, 82700, 5,
        testInbox.getRef());
    String expected = "82741 : 0000081e9d118bf0827bed8f4a3e142a99a42ef29c8c3d3e24ae2592456c440b";

    testActor.run(message);
    List<CapturedLogEvent> logMessages = testActor.getAllLogEntries();

    assertEquals(1, testInbox.getAllReceived().size());
    assertEquals(1, logMessages.size());
    assertEquals(expected, logMessages.get(0).message());
    assertEquals(Level.DEBUG, logMessages.get(0).level());
  }

  @Test
  void messageReceivedIfNonceInRange() {
    BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(
        WorkerBehavior.create());
    TestInbox<HashResult> testInbox = TestInbox.create();
    Block block = BlocksData.getNextBlock(0, "0");
    WorkerBehavior.Command message = new WorkerBehavior.Command(block, 82700, 5,
        testInbox.getRef());

    HashResult expectedHashResult = HashResult.foundAHash(82741,
        "0000081e9d118bf0827bed8f4a3e142a99a42ef29c8c3d3e24ae2592456c440b");
    testActor.run(message);

    testInbox.expectMessage(expectedHashResult);
  }

  @Test
  void messageReceivedIfNonceNotInRange() {
    BehaviorTestKit<WorkerBehavior.Command> testActor = BehaviorTestKit.create(
        WorkerBehavior.create());
    TestInbox<HashResult> testInbox = TestInbox.create();
    Block block = BlocksData.getNextBlock(0, "0");
    WorkerBehavior.Command message = new WorkerBehavior.Command(block, 0, 5,
        testInbox.getRef());

    testActor.run(message);

    assertFalse(testInbox.hasMessages());
  }
}