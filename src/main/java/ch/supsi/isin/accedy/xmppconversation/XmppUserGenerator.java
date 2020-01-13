package ch.supsi.isin.accedy.xmppconversation;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.security.SecureRandom;
import java.util.*;

public class XmppUserGenerator {
    private static SecureRandom rnd = new SecureRandom();

    private String host;
    private KeyGenerator keyGenerator;

    public XmppUser createAccount(UserType userType, ArrayList<XmppUser> xmppUsers) {
        XmppUser xmppUser = null;
        try {
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setXmppDomain(host)
                    .setHost(host)
                    .build();

            AbstractXMPPConnection connection = new XMPPTCPConnection(config);
            connection.connect();

            int createdUsersNumber = 0;
            while (createdUsersNumber < Constants.USERS_TO_CREATE) {
                xmppUser = createRandomUser();
                // Registering the xmppUser
                try {
                    Localpart lp = Localpart.from(xmppUser.getName());
                    AccountManager accountManager = AccountManager.getInstance(connection);
                    accountManager.sensitiveOperationOverInsecureConnection(true); //TODO check if this must be done only for localhost ???
                    if (accountManager.supportsAccountCreation()) {
                        accountManager.createAccount(lp, xmppUser.getPassword());
                        System.out.println("User " + xmppUser.getName() + " password " + xmppUser.getPassword() + " created");
                        xmppUser.setKey(keyGenerator.getKey());
                        xmppUser.setType(userType);
                        xmppUsers.add(xmppUser);
                        createdUsersNumber++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                    if (connection != null) {
                        connection.connect();
                    }
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return xmppUser;
    }

    public XmppUserGenerator(String host) {
        this.host = host;
        keyGenerator = new StringKeyGenerator();
    }

    public XmppUser createRandomUser(){
        StringBuilder sbUser = new StringBuilder(Constants.XMPP_USER_LENGTH);
        //sbUser.append(idUser + "_");
        for(int i = 0; i < Constants.XMPP_USER_LENGTH; i++)
            sbUser.append(Constants.AB.charAt(rnd.nextInt(Constants.AB.length())));

        StringBuilder sbPassword = new StringBuilder(Constants.XMPP_PASSWORD_LENGTH);
        for(int i = 0; i < Constants.XMPP_PASSWORD_LENGTH; i++)
            sbPassword.append(Constants.AB_NOTTRIVIAL_CHARS.charAt(rnd.nextInt(Constants.AB_NOTTRIVIAL_CHARS.length())));

        return new XmppUser(sbUser.toString(), sbPassword.toString(), host);
    }

}
