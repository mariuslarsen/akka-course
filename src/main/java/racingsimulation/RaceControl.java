package racingsimulation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class RaceControl extends AbstractBehavior<RaceControl.Command> {
    public sealed interface Command extends Serializable {
    }

    public record CreateRaceCommand(String instruction) implements Command {}
    public record UpdateCommand(double position, ActorRef<Racer.Command> from) implements Command {}

    private RaceControl(ActorContext<RaceControl.Command> context) {
        super(context);
    }

    Map<String, Double> positions = new HashMap<>();

    @Override
    public Receive<RaceControl.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(CreateRaceCommand.class, command -> {
                    if(command.instruction.equals("create race")) {
                        // spawn racers
                        IntStream.range(0, 10).mapToObj(i -> getContext().spawn(Racer.create(), "racer-" + i))
                                .forEach(racer -> racer.tell(new Racer.StartCommand(100)));
                    }
                    return this;
                })
                .onMessage(UpdateCommand.class, command -> {
                    positions.put(command.from.path().name(), command.position);
                    System.out.println(positions);
                    return this;
                })
                .build();
    }


}
