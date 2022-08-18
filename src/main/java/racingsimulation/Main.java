package racingsimulation;

import akka.actor.typed.ActorSystem;

public class Main {

  public static void main(String[] args) {
    ActorSystem<RaceControl.Command> raceControl = ActorSystem.create(RaceControl.create(),
        "RaceControl");
    raceControl.tell(new RaceControl.CreateRaceCommand());
  }

}
