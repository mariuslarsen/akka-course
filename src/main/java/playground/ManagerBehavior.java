package playground;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.stream.IntStream;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

  private ManagerBehavior(ActorContext<ManagerBehavior.Command> context) {
    super(context);
  }

  public static Behavior<ManagerBehavior.Command> create() {
    return Behaviors.setup(ManagerBehavior::new);
  }

  @Override
  public Receive<ManagerBehavior.Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(
            InstructionCommand.class,
            command -> {
              if (command.instruction.equals("start")) {
                IntStream.range(0, 20)
                    .mapToObj(i -> getContext().spawn(WorkerBehavior.create(), "worker-" + i))
                    .forEach(
                        actor ->
                            actor.tell(
                                new WorkerBehavior.CalculateCommand(
                                    "start", getContext().getSelf())));
              }
              return this;
            })
        .onMessage(
            ResultCommand.class,
            result -> {
              System.out.println(result.prime);
              return this;
            })
        .build();
  }

  public sealed interface Command extends Serializable {}

  public record InstructionCommand(String instruction) implements Command {}

  public record ResultCommand(BigInteger prime) implements Command {}
}
