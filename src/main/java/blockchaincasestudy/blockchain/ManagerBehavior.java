package blockchaincasestudy.blockchain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.StashBuffer;
import blockchaincasestudy.model.Block;
import blockchaincasestudy.model.HashResult;
import java.io.Serializable;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

  private final StashBuffer<Command> stashBuffer;

  public interface Command extends Serializable {}

  public record MineBlockCommand(Block block, ActorRef<HashResult> sender, int difficulty)
      implements Command {}

  public record HashResultCommand(HashResult hashResult) implements Command {}

  private ManagerBehavior(ActorContext<Command> context, StashBuffer<Command> stashBuffer) {
    super(context);
    this.stashBuffer = stashBuffer;
  }

  public static Behavior<Command> create() {
    return Behaviors.withStash(
        10, stash -> Behaviors.setup(context -> new ManagerBehavior(context, stash)));
  }

  @Override
  public Receive<Command> createReceive() {
    return idleMessageHandler();
  }

  private Receive<Command> idleMessageHandler() {
    return newReceiveBuilder()
        .onSignal(Terminated.class, handler -> Behaviors.same())
        .onMessage(
            MineBlockCommand.class,
            message -> {
              this.sender = message.sender;
              this.block = message.block;
              this.difficulty = message.difficulty;
              currentlyMining = true;
              for (int i = 0; i < 10; i++) {
                startNextWorker();
              }
              return activeMessageHandler();
            })
        .build();
  }

  private Receive<Command> activeMessageHandler() {
    return newReceiveBuilder()
        .onSignal(
            Terminated.class,
            handler -> {
              startNextWorker();
              return Behaviors.same();
            })
        .onMessage(
            HashResultCommand.class,
            message -> {
              getContext().getChildren().forEach(worker -> getContext().stop(worker));
              currentlyMining = false;
              sender.tell(message.hashResult);
              return stashBuffer.unstashAll(idleMessageHandler());
            })
        .onMessage(
            MineBlockCommand.class,
            message -> {
              System.out.println("Delaying mining request");
              // getContext().getSelf().tell(message);
              if (!stashBuffer.isFull()) {
                stashBuffer.stash(message);
              }
              return Behaviors.same();
            })
        .build();
  }

  private ActorRef<HashResult> sender;
  private Block block;
  private int difficulty;
  private int currentNonce = 0;

  private boolean currentlyMining;

  private void startNextWorker() {
    if (currentlyMining) {
      // System.out.println("About to start mining with nonce starting at: " + currentNonce * 1000);
      Behavior<WorkerBehavior.Command> workerBehavior =
          Behaviors.supervise(WorkerBehavior.create()).onFailure(SupervisorStrategy.resume());
      ActorRef<WorkerBehavior.Command> worker =
          getContext().spawn(workerBehavior, "worker-" + currentNonce);
      getContext().watch(worker);
      worker.tell(
          new WorkerBehavior.Command(
              block, currentNonce * 1000, difficulty, getContext().getSelf()));
      currentNonce++;
    }
  }
}
