package playground.rng;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

public class Manager extends AbstractBehavior<Manager.Command> {

  public interface Command extends Serializable {}

  public record Init() implements Command {}

  public record Result(int number) implements Command {}

  private record NoResponse(int attempts) implements Command {}

  private Manager(ActorContext<Manager.Command> context) {
    super(context);
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(Manager::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return initialize();
  }

  private Receive<Command> initialize() {
    return newReceiveBuilder()
        .onMessage(
            Init.class,
            message -> {
              ActorRef<RngProvider.Command> rngWorker =
                  getContext().spawn(RngProvider.create(), "RandomNumberGenerator");
              askForNumber(rngWorker, 1);
              return waitForReply(rngWorker);
            })
        .build();
  }

  private Receive<Command> waitForReply(ActorRef<RngProvider.Command> worker) {
    return newReceiveBuilder()
        .onMessage(
            Result.class,
            message -> {
              getContext().getLog().info("Received {}", message.number);
              return Behaviors.stopped();
            })
        .onMessage(
            NoResponse.class,
            message -> message.attempts < 5,
            message -> {
              getContext()
                  .getLog()
                  .warn("No response after {} attempts. Retrying...", message.attempts);
              askForNumber(worker, message.attempts + 1);
              return Behaviors.same();
            })
        .onMessage(
            NoResponse.class,
            message -> {
              getContext()
                  .getLog()
                  .error(
                      "No response after {} attempts. Terminating application", message.attempts);
              return Behaviors.stopped();
            })
        .build();
  }

  private void askForNumber(ActorRef<RngProvider.Command> worker, int attempts) {
    getContext()
        .ask(
            Command.class,
            worker,
            Duration.ofSeconds(1),
            RngProvider.GetNumber::new,
            (response, throwable) ->
                Objects.requireNonNullElseGet(response, () -> new NoResponse(attempts)));
  }
}
