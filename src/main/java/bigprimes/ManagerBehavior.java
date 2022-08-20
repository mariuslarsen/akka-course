package bigprimes;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

  public sealed interface Command extends Serializable {}

  public record StartCommand() implements Command {}

  public record ResultCommand(BigInteger prime) implements Command {}

  private record NoResponseReceivedCommand(ActorRef<WorkerBehavior.Command> worker)
      implements Command {}

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
        .onMessage(
            StartCommand.class,
            command -> {
              IntStream.range(0, 20)
                  .mapToObj(i -> getContext().spawn(WorkerBehavior.create(), "worker-" + i))
                  .forEach(this::askWorkerForPrime);
              return Behaviors.same();
            })
        .onMessage(
            ResultCommand.class,
            command -> {
              primes.add(command.prime);
              System.out.println("Processed: " + primes.size() + " primes");
              if (primes.size() == 20) {
                primes.forEach(System.out::println);
              }
              return Behaviors.same();
            })
        .onMessage(
            NoResponseReceivedCommand.class,
            command -> {
              System.out.println("Retrying with worker " + command.worker);
              askWorkerForPrime(command.worker);
              return Behaviors.same();
            })
        .build();
  }

  private void askWorkerForPrime(ActorRef<WorkerBehavior.Command> worker) {
    getContext()
        .ask(
            Command.class,
            worker,
            Duration.ofSeconds(5),
            WorkerBehavior.CalculatePrime::new,
            (response, throwable) -> {
              if (response != null) {
                return response;
              } else {
                System.out.println("Worker " + worker.path() + " failed to respond.");
                return new NoResponseReceivedCommand(worker);
              }
            });
  }
}
