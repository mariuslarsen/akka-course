package bigprimes;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.concurrent.CompletionStage;

public class Main {

  public static void main(String[] args) {
    ActorSystem<ManagerBehavior.Command> actorSystem =
        ActorSystem.create(ManagerBehavior.create(), "manager");

    CompletionStage<SortedSet<BigInteger>> result = AskPattern.ask(actorSystem,
        ManagerBehavior.StartCommand::new, Duration.ofSeconds(20),
        actorSystem.scheduler());

    result.whenComplete((response, throwable) -> {
      if(response != null) {
        response.forEach(System.out::println);
      } else {
        System.out.println("The system didn't respond in time");
      }
      actorSystem.terminate();
    });

  }
}
