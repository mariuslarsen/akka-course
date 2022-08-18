package bigprimes;

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

  sealed interface Command extends Serializable {

  }

  public record CalculatePrime(String message, ActorRef<ManagerBehavior.Command> sender) implements
      Command {

  }

  private WorkerBehavior(ActorContext<Command> context) {
    super(context);
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(WorkerBehavior::new);
  }

  private BigInteger cachedPrime;

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(CalculatePrime.class, command -> {
          if (command.message.equals("start")) {
            if (cachedPrime != null) {
              command.sender.tell(new ManagerBehavior.ResultCommand(cachedPrime));
            } else {
              BigInteger bigInteger = new BigInteger(2000, new Random());
              cachedPrime = bigInteger.nextProbablePrime();
              command.sender.tell(new ManagerBehavior.ResultCommand(cachedPrime));
            }
          }
          return this;
        })
        .build();
  }
}
