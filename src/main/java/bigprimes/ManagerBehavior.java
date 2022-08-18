package bigprimes;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

  public sealed interface Command extends Serializable {

  }

  public record StartCommand() implements Command {

  }

  public record ResultCommand(BigInteger prime) implements Command {

  }

  private ManagerBehavior(ActorContext<Command> context) {
    super(context);
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(ManagerBehavior::new);
  }

  private final SortedSet<BigInteger> primes = new TreeSet<>();

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(StartCommand.class, command -> {
          IntStream.range(0, 20)
              .mapToObj(i -> getContext().spawn(WorkerBehavior.create(), "worker-" + i))
              .forEach(actor -> {
                actor.tell(new WorkerBehavior.CalculatePrime(getContext().getSelf()));
                actor.tell(new WorkerBehavior.CalculatePrime(getContext().getSelf()));
              });
          return this;
        })
        .onMessage(ResultCommand.class, command -> {
          primes.add(command.prime);
          System.out.println("Processed: " + primes.size() + " primes");
          if (primes.size() == 20) {
            primes.forEach(System.out::println);
          }
          return this;
        })
        .build();
  }
}
