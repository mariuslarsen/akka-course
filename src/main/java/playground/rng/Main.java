package playground.rng;

import akka.actor.typed.ActorSystem;

public class Main {
  public static void main(String[] args) {
      ActorSystem<Manager.Command> actorSystem = ActorSystem.create(Manager.create(), "RngManager");
      actorSystem.tell(new Manager.Init());
  }
}
