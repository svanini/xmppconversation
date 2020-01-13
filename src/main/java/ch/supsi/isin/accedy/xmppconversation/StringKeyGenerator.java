package ch.supsi.isin.accedy.xmppconversation;

import java.security.SecureRandom;

public class StringKeyGenerator implements KeyGenerator {
    private static SecureRandom rnd = new SecureRandom();

    public Object getKey() {
        StringBuilder sbKey = new StringBuilder(Constants.XMPP_KEY_LENGTH);
        for(int i = 0; i < Constants.XMPP_KEY_LENGTH; i++)
            sbKey.append(Constants.AB_NOTTRIVIAL_CHARS.charAt(rnd.nextInt(Constants.AB_NOTTRIVIAL_CHARS.length())));

        return sbKey.toString();
    }
}
