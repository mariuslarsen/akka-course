package playground.rng;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.util.Random;

public class RngProvider extends AbstractBehavior<RngProvider.Command> {

  public interface Command extends Serializable {}

  public record GetNumber(ActorRef<Manager.Command> replyTo) implements Command {}

  private RngProvider(ActorContext<Command> context) {
    super(context);
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(RngProvider::new);
  }

  private final Random random = new Random();

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(
            GetNumber.class,
            message -> {
              if (Math.random() < 0.5) {
                message.replyTo.tell(new Manager.Result(random.nextInt()));
              }

              return Behaviors.same();
            })
        .build();
  }
}
