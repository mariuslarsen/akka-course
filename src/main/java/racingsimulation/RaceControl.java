package racingsimulation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.io.Serializable;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class RaceControl extends AbstractBehavior<RaceControl.Command> {

  public sealed interface Command extends Serializable {

  }

  public record CreateRaceCommand() implements Command {

  }

  public record RacerUpdateCommand(int position, ActorRef<Racer.Command> from) implements Command {

  }

  private record GetPositionsCommand() implements Command {

  }

  private RaceControl(ActorContext<RaceControl.Command> context) {
    super(context);
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(RaceControl::new);
  }

  private Map<ActorRef<Racer.Command>, Integer> currentPositions;
  private long start;
  private final int RACE_LENGTH = 100;

  private Objects timer_key;

  private void displayRace() {
    int displayLength = 160;
    for (int i = 0; i < 50; ++i) {
      System.out.println();
    }
    System.out.println(
        "Race has been running for " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
    System.out.println("    " + new String(new char[displayLength]).replace('\0', '='));
    currentPositions.keySet().forEach(racer ->
        System.out.println(racer.path().name() + " : " + new String(
            new char[currentPositions.get(racer) * displayLength / 100]).replace('\0', '*'))
    );
  }

  @Override
  public Receive<RaceControl.Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(CreateRaceCommand.class, command -> {
          start = System.currentTimeMillis();
          currentPositions = new LinkedHashMap<>();
          IntStream.range(0, 10).mapToObj(i -> getContext().spawn(Racer.create(), "racer-" + i))
              .forEach(racer -> {
                currentPositions.put(racer, 0);
                racer.tell(new Racer.StartCommand(RACE_LENGTH));
              });
          return Behaviors.withTimers(timer -> {
            timer.startTimerAtFixedRate(timer_key, new GetPositionsCommand(),
                Duration.ofSeconds(1));
            return this;
          });
        })
        .onMessage(GetPositionsCommand.class, command -> {
          currentPositions.keySet().forEach(racer -> racer.tell(new Racer.PositionCommand(
              getContext().getSelf())));
          displayRace();
          return this;
        })
        .onMessage(RacerUpdateCommand.class, command -> {
          currentPositions.put(command.from, command.position);
          return this;
        })
        .build();
  }


}
