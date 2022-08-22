package blockchaincasestudy.model;

public record HashResult(int nonce, String hash, boolean completed) {
  public static HashResult foundAHash(int nonce, String hash) {
    return new HashResult(nonce, hash, true);
  }

  public static HashResult emptyHash() {
    return new HashResult(0, "", false);
  }
}
