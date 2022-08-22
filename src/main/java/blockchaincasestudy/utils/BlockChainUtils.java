package blockchaincasestudy.utils;

import blockchaincasestudy.model.Block;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BlockChainUtils {

  public static String calculateHash(String data) {

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] rawHash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < rawHash.length; i++) {
        String hex = Integer.toHexString(0xff & rawHash[i]);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  public static boolean validateBlock(Block block) {
    String dataToEncode =
        block.getPreviousHash()
            + block.getTransaction().getTimestamp()
            + block.getNonce()
            + block.getTransaction();
    String checkHash = calculateHash(dataToEncode);
    return (checkHash.equals(block.getHash()));
  }
}
