package blockchaincasestudy.blockchain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import blockchaincasestudy.model.Block;
import blockchaincasestudy.model.HashResult;
import blockchaincasestudy.utils.BlockChainUtils;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

  public record Command(Block block, int startNonce, int difficulty, ActorRef<HashResult> controller) {

  }

  private WorkerBehavior(ActorContext<Command> context) {
    super(context);
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(WorkerBehavior::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onAnyMessage(message -> {
          String hash = new String(new char[message.difficulty]).replace("\0", "X");
          String target = new String(new char[message.difficulty]).replace("\0", "0");

          int nonce = message.startNonce;
          while (!hash.substring(0, message.difficulty).equals(target)
              && nonce < message.startNonce + 1000) {
            nonce++;
            String dataToEncode =
                message.block.getPreviousHash() + message.block.getTransaction().getTimestamp()
                    + nonce + message.block.getTransaction();
            hash = BlockChainUtils.calculateHash(dataToEncode);
          }
          if (hash.substring(0, message.difficulty).equals(target)) {
            HashResult hashResult = HashResult.foundAHash(nonce, hash);

            // send hashresult to controller return hashResult;
            getContext().getLog().debug(hashResult.nonce() + " : " + hashResult.hash());
            message.controller.tell(hashResult);
            return Behaviors.same();
          } else {
            getContext().getLog().debug("null");
            return Behaviors.same();
          }
        })
        .build();
  }

}
