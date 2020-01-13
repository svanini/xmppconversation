package ch.supsi.isin.accedy.xmppconversation;

public interface Encryption {

    public String encrypt(String message, String skey);
    public String decrypt(String encryptedMessage, String skey);
}
