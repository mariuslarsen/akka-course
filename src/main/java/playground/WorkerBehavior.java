package playground;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

  public sealed interface Command extends Serializable {}

  public record CalculateCommand(String message, ActorRef<ManagerBehavior.Command> replyTo)
      implements Command {}

  private WorkerBehavior(ActorContext<Command> context) {
    super(context);
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(WorkerBehavior::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(
            CalculateCommand.class,
            command -> {
              if (command.message.equals("start")) {
                BigInteger number = new BigInteger(2000, new Random());
                command.replyTo.tell(new ManagerBehavior.ResultCommand(number.nextProbablePrime()));
              }

              return this;
            })
        .build();
  }
}
