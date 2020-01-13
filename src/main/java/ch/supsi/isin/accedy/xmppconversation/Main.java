package ch.supsi.isin.accedy.xmppconversation;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static String DH_KEY = "1267";
    private static String idUser = UUID.randomUUID().toString();
    private static AbstractXMPPConnection senderXmppConnection;
    private static AbstractXMPPConnection receiverXmppConnection;
    private static ChatManager senderCm;
    private static ChatManager receiverCm;
    private static AES aes = new AES();

    static public AbstractXMPPConnection connect(String username, String password, String host) throws IOException, InterruptedException, XMPPException, SmackException {
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(username, password);
        configBuilder.setHost(host);
        configBuilder.setXmppDomain(host);
        AbstractXMPPConnection connection = new XMPPTCPConnection(configBuilder.build());
        // Connect to the server and login
        connection.connect().login();
        connection.setReplyTimeout(300000);

        return connection;
    }

    private static void xmppLogin(String server, String username, String password) throws Exception {
        senderXmppConnection = connect(username, password, server);
        senderCm = ChatManager.getInstanceFor(senderXmppConnection);
    }

    /*public static void sendMessage(Message message, String username, String host, ChatManager chatManager) throws SmackException.NotConnectedException, InterruptedException, XmppStringprepException {
        EntityBareJid jid = JidCreate.entityBareFrom(username + "@" + host);
        org.jivesoftware.smack.chat2.Chat chat = chatManager.chatWith(jid);
        chat.send(message);
    }*/

    public static void addListeners() {
        senderCm.addIncomingListener((entityBareJid, message, chat) -> {
            processMessage(message);
            String decryptedMsg = aes.decrypt(message.getBody(), DH_KEY);
            System.out.println("New message from " + entityBareJid + ": " + decryptedMsg);
        });

        receiverCm.addIncomingListener((entityBareJid, message, chat) -> {
            processMessage(message);
            String decryptedMsg = aes.decrypt(message.getBody(), DH_KEY);
            System.out.println("New message from " + entityBareJid + ": " + decryptedMsg);
        });
    }

    public static void sendMessage(String destJid) throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {
        EntityBareJid jid = JidCreate.entityBareFrom(destJid);
        Message message = new Message();
        message.setSubject("Message subject");
        message.setBody("Message body");
        Chat chat = senderCm.chatWith(jid);
        chat.send(message);
    }

    private static void processMessage(Message message) {

    }

    public static void main(final String[] args) {
        String server = "DTI-ISIN-052";
        XmppUserGenerator xmppUserGenerator = new XmppUserGenerator(server);
        XmppUser xmppSender = xmppUserGenerator.createAccount(UserType.SENDER);
        /*XmppUser xmppSender = new XmppUser();
        xmppSender.setName("FV7DG4cuEV ");
        xmppSender.setPassword("UrjJgP1cf5 ");
        xmppSender.setType(UserType.SENDER);
        xmppSender.setHost(server);*/
        XmppUser xmppReceiver = xmppUserGenerator.createAccount(UserType.RECEIVER);
        /*XmppUser xmppReceiver = new XmppUser();
        xmppReceiver.setName("acq1ApUfSE ");
        xmppReceiver.setPassword("1JK(5h>uE<");
        xmppReceiver.setType(UserType.RECEIVER);
        xmppReceiver.setHost(server);*/

        try {
            senderXmppConnection = connect(xmppSender.getName(), xmppSender.getPassword(), server);
            senderCm = ChatManager.getInstanceFor(senderXmppConnection);
            System.out.println("Sender chat initialised");
            receiverXmppConnection = connect(xmppReceiver.getName(), xmppReceiver.getPassword(), server);
            receiverCm = ChatManager.getInstanceFor(receiverXmppConnection);
            addListeners();

            /* sender sends a message to receiver **/
            String recJid = xmppReceiver.getName() + "@" + xmppReceiver.getHost();
            EntityBareJid recEBJid = JidCreate.entityBareFrom(recJid);
            Message sendMessage = new Message();
            String sendEncryptedMsg = aes.encrypt("Message body SENDER", DH_KEY);
            sendMessage.setBody(sendEncryptedMsg);
            Chat sendChat = senderCm.chatWith(recEBJid);
            sendChat.send(sendMessage);
            System.out.println("Message sent to " + xmppReceiver.getName());

            /* receiver sends a message to sender **/
            String sendJid = xmppSender.getName() + "@" + xmppSender.getHost();
            EntityBareJid sendEBJid = JidCreate.entityBareFrom(sendJid);
            Message recMessage = new Message();
            String recEncryptedMsg = aes.encrypt("Message body RECEIVER", DH_KEY);
            recMessage.setBody(recEncryptedMsg);
            Chat recChat = receiverCm.chatWith(sendEBJid);
            recChat.send(recMessage);
            System.out.println("Message sent to " + xmppSender.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        //remember to disconnect when done
        senderXmppConnection.disconnect();
        receiverXmppConnection.disconnect();
    }

    private static UserGeneratorCallback userGeneratorCallback = new UserGeneratorCallback() {

        @Override
        public void onXmppUserCreated(String server, String idUser, ArrayList<XmppUser> users, boolean fromPSL) {
            System.out.println("Users created");
        }
    };
}
