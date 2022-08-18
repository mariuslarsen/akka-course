package playground;


import akka.actor.typed.ActorSystem;

public class Main {

  public static void main(String[] args) {
    ActorSystem<ManagerBehavior.Command> actorSystem = ActorSystem.create(ManagerBehavior.create(),
        "manager");
    actorSystem.tell(new ManagerBehavior.InstructionCommand("start"));
  }

}
