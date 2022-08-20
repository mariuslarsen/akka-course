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

  public record CalculatePrime(ActorRef<ManagerBehavior.Command> sender) implements
      Command {

  }

  private WorkerBehavior(ActorContext<Command> context) {
    super(context);
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(WorkerBehavior::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return handleWithoutCachedPrime();
  }

  public Receive<Command> handleWithoutCachedPrime() {
    return newReceiveBuilder()
        .onMessage(CalculatePrime.class, command -> {
          BigInteger bigInteger = new BigInteger(2000, new Random());
          BigInteger prime = bigInteger.nextProbablePrime();
          if(Math.random() < 0.4) {
              command.sender.tell(new ManagerBehavior.ResultCommand(prime));
              return Behaviors.same();
          }
          return handleWithCachedPrime(prime);
        })
        .build();
  }

  public Receive<Command> handleWithCachedPrime(BigInteger prime) {
    return newReceiveBuilder()
        .onMessage(CalculatePrime.class, command -> {
          command.sender.tell(new ManagerBehavior.ResultCommand(prime));
          return Behaviors.same();
        })
        .build();
  }
}
