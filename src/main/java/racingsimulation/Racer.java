package racingsimulation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;

public class Racer extends AbstractBehavior<Racer.Command> {

    public sealed interface Command extends Serializable {}

    public record StartCommand(int raceLength) implements Command {}
    public record PositionCommand(ActorRef<RaceControl.Command> raceControl) implements Command {}

    private Racer(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Racer::new);
    }

    double position = 0.0;

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, command -> {
                    return this;
                })
                .onMessage(PositionCommand.class, command -> {
                    position += 1;
                    return this;
                })
                .build();
    }
}
