package bigprimes;

import akka.actor.typed.ActorSystem;

public class Main {

//  public static void main(String[] args) {
//    ActorSystem<String> actorSystem = ActorSystem.create(FirstSimpleBehavior.create(),
//        "FirstActorSystem");
//
//    actorSystem.tell("Hello, are you there?");
//    actorSystem.tell("say hello");
//    actorSystem.tell("who are you");
//    actorSystem.tell("create a child");
//  }

  public static void main(String[] args) {
    ActorSystem<ManagerBehavior.Command> actorSystem = ActorSystem.create(ManagerBehavior.create(),
        "manager");
    actorSystem.tell(new ManagerBehavior.StartCommand());
  }

}
