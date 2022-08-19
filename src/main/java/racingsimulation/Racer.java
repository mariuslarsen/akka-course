package racingsimulation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.util.Random;

public class Racer extends AbstractBehavior<Racer.Command> {

  private int averageSpeedAdjustmentFactor;
  private Random random;
  private double currentSpeed = 0;

  private Racer(ActorContext<Command> context) {
    super(context);
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(Racer::new);
  }

  private double getMaxSpeed() {
    double defaultAverageSpeed = 48.2;
    return defaultAverageSpeed * (1 + ((double) averageSpeedAdjustmentFactor / 100));
  }

  private double getDistanceMovedPerSecond() {
    return currentSpeed * 1000 / 3600;
  }

  private void determineNextSpeed(int raceLength, double currentPosition) {
    if (currentPosition < (raceLength / 4)) {
      currentSpeed = currentSpeed + (((getMaxSpeed() - currentSpeed) / 10) * random.nextDouble());
    } else {
      currentSpeed = currentSpeed * (0.5 + random.nextDouble());
    }

    if (currentSpeed > getMaxSpeed()) {
      currentSpeed = getMaxSpeed();
    }

    if (currentSpeed < 5) {
      currentSpeed = 5;
    }

    if (currentPosition > (raceLength / 2) && currentSpeed < getMaxSpeed() / 2) {
      currentSpeed = getMaxSpeed() / 2;
    }
  }

  @Override
  public Receive<Command> createReceive() {
    return notYetStarted();
  }

  public Receive<Command> notYetStarted() {
    return newReceiveBuilder()
        .onMessage(
            StartCommand.class,
            command -> {
              random = new Random();
              averageSpeedAdjustmentFactor = random.nextInt(30) - 10;
              return running(command.raceLength, 0.0);
            })
        .build();
  }

  public Receive<Command> running(int raceLength, double currentPosition) {
    return newReceiveBuilder()
        .onMessage(
            PositionCommand.class,
            command -> {
              determineNextSpeed(raceLength, currentPosition);
              double newPosition = currentPosition + getDistanceMovedPerSecond();
              if (newPosition > raceLength) {
                newPosition = raceLength;
              }
              command.raceControl.tell(
                  new RaceControl.RacerUpdateCommand((int) newPosition, getContext().getSelf()));
              if (newPosition == raceLength) {
                return completed(raceLength);
              }
              return running(raceLength, newPosition);
            })
        .build();
  }

  public Receive<Command> completed(int position) {
    return newReceiveBuilder()
        .onMessage(
            PositionCommand.class,
            command -> {
              command.raceControl.tell(
                  new RaceControl.RacerUpdateCommand(position, getContext().getSelf()));
              command.raceControl.tell(
                  new RaceControl.RacerCompletedCommand(getContext().getSelf()));
              return Behaviors.ignore();
            })
        .build();
  }

  public sealed interface Command extends Serializable {}

  public record StartCommand(int raceLength) implements Command {}

  public record PositionCommand(ActorRef<RaceControl.Command> raceControl) implements Command {}
}
