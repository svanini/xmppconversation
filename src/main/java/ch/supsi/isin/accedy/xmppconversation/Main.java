package ch.supsi.isin.accedy.xmppconversation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class Main {
    private final static String DH_KEY = "1267";
    private final static String CMD_ON = "On";
    private final static String CMD_OFF = "Off";
    private final static ArrayList<String> CMD_LIST = new ArrayList<>(Arrays.asList(CMD_ON, CMD_OFF));
    private static String idUser = UUID.randomUUID().toString();
    private static AbstractXMPPConnection senderXmppConnection;
    private static AbstractXMPPConnection receiverXmppConnection;
    private static ChatManager senderCm;
    private static ChatManager receiverCm;
    private static AES aes = new AES();
    static LinkedBlockingQueue<AbstractXMPPConnection> sendersXmppConnectionQueue = new LinkedBlockingQueue<AbstractXMPPConnection>(Constants.USERS_TO_CREATE);
    static LinkedBlockingQueue<AbstractXMPPConnection> receiversXmppConnectionQueue = new LinkedBlockingQueue<AbstractXMPPConnection>(Constants.USERS_TO_CREATE);

    //address http://172.16.20.187:8080/json.htm?type=command&param=switchlight&idx=99&switchcmd=Off


    static public void connect(ArrayList<XmppUser> xmppUsers) throws IOException, InterruptedException, XMPPException, SmackException {
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();

        for (XmppUser user : xmppUsers) {
            configBuilder.setUsernameAndPassword(user.getName(), user.getPassword());
            configBuilder.setHost(user.getHost());
            configBuilder.setXmppDomain(user.getHost());
            AbstractXMPPConnection connection = new XMPPTCPConnection(configBuilder.build());
            // Connect to the server and login
            connection.connect().login();
            connection.setReplyTimeout(300000);
            if (user.getType().equals(UserType.SENDER)) {
                sendersXmppConnectionQueue.add(connection);
            } else {
                receiversXmppConnectionQueue.add(connection);
            }
        }
    }

    /*private static void xmppLogin(String server, String username, String password) throws Exception {
        senderXmppConnection = connect(username, password, server);
        senderCm = ChatManager.getInstanceFor(senderXmppConnection);
    }*/

    /*public static void sendMessage(Message message, String username, String host, ChatManager chatManager) throws SmackException.NotConnectedException, InterruptedException, XmppStringprepException {
        EntityBareJid jid = JidCreate.entityBareFrom(username + "@" + host);
        org.jivesoftware.smack.chat2.Chat chat = chatManager.chatWith(jid);
        chat.send(message);
    }*/

    public static void addListeners() {
        senderCm.addIncomingListener((entityBareJid, message, chat) -> {
            String decryptedMsg = aes.decrypt(message.getBody(), DH_KEY);
            System.out.println("New message from " + entityBareJid + ": " + decryptedMsg);
            processMessage(decryptedMsg);
        });

        receiverCm.addIncomingListener((entityBareJid, message, chat) -> {
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

    private static void processMessage(String msg) {
        //msg is in the form switchcmd(On, Off):IDX
        String[] cmdList = msg.split(":");
        String command = cmdList[0];
        if (CMD_LIST.contains(command)) {
            System.out.println("Received command: " + command);
            String idx = cmdList[1];
            String url = "http://127.0.0.1:8080/json.htm?type=command&param=switchlight&idx=" + idx + "&switchcmd=" + command;
            new Thread(() -> {
                HttpURLConnection connection = null;
                try {
                    URL myURL = new URL(url);
                    connection = (HttpURLConnection)myURL.openConnection();
                    int responseCode = connection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        System.out.println("Unable to execute command");
                    } else {
                        Reader reader = new InputStreamReader(connection.getInputStream());
                        Type domoticzType = new TypeToken<DomoticzOutcome>() {}.getType();
                        Gson gson = new Gson();
                        DomoticzOutcome outcome = gson.fromJson(reader, domoticzType);
                        if (outcome.status.equals("Ok")) {
                            System.out.println("Command " + outcome.title + " successfully executed");
                        } else {
                            System.out.println("Command " + outcome.title + " failed");
                        }
                    }
                    //Json returned is in the form: {
                    //   "status" : "OK",
                    //   "title" : "SwitchLight"
                    //}
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }).start();
        } else {
            System.out.println("Unknown command received: " + command);
        }
    }

    /*private static void manageHttpRequest(String url) {
        //TODO: implement in a thread
        HttpURLConnection connection = null;
        try {
            URL myURL = new URL(url);
            connection = (HttpURLConnection)myURL.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Unable to execute command");
            } else {
                Reader reader = new InputStreamReader(connection.getInputStream());
                Type domoticzType = new TypeToken<DomoticzOutcome>() {}.getType();
                Gson gson = new Gson();
                DomoticzOutcome outcome = gson.fromJson(reader, domoticzType);
                if (outcome.status.equals("Ok")) {
                    System.out.println("Command " + outcome.title + " successfully executed");
                } else {
                    System.out.println("Command " + outcome.title + " failed");
                }
            }
            //Json returned is in the form: {
            //   "status" : "OK",
            //   "title" : "SwitchLight"
            //}
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }*/

    public static void main(final String[] args) {
        String server = "DTI-ISIN-052";
        XmppUserGenerator xmppUserGenerator = new XmppUserGenerator(server);
        ArrayList<XmppUser> sendXmppUsers = new ArrayList<>(Constants.USERS_TO_CREATE);
        xmppUserGenerator.createAccount(UserType.SENDER, sendXmppUsers);
        /*XmppUser xmppSender = new XmppUser();
        xmppSender.setName("FV7DG4cuEV ");
        xmppSender.setPassword("UrjJgP1cf5 ");
        xmppSender.setType(UserType.SENDER);
        xmppSender.setHost(server);*/
        ArrayList<XmppUser> recXmppUsers = new ArrayList<>(Constants.USERS_TO_CREATE);
        XmppUser xmppReceiver = xmppUserGenerator.createAccount(UserType.RECEIVER, recXmppUsers);
        /*XmppUser xmppReceiver = new XmppUser();
        xmppReceiver.setName("acq1ApUfSE ");
        xmppReceiver.setPassword("1JK(5h>uE<");
        xmppReceiver.setType(UserType.RECEIVER);
        xmppReceiver.setHost(server);*/

        try {
            connect(sendXmppUsers);
            connect(recXmppUsers);
            while (true) {
                AbstractXMPPConnection connection = sendersXmppConnectionQueue.poll();
                if (connection == null) break;
                EntityFullJid jid = connection.getUser();
                System.out.println(jid.toString());
                connection.disconnect();
            }

            while (true) {
                AbstractXMPPConnection connection = receiversXmppConnectionQueue.poll();
                if (connection == null) break;
                EntityFullJid jid = connection.getUser();
                System.out.println(jid.toString());
                connection.disconnect();
            }

            /*
            senderCm = ChatManager.getInstanceFor(senderXmppConnection);
            System.out.println("Sender chat initialised");
            receiverXmppConnection = connect(xmppReceiver.getName(), xmppReceiver.getPassword(), server);
            receiverCm = ChatManager.getInstanceFor(receiverXmppConnection);
            addListeners();

            // sender sends a message to receiver
            String recJid = xmppReceiver.getName() + "@" + xmppReceiver.getHost();
            EntityBareJid recEBJid = JidCreate.entityBareFrom(recJid);
            Message sendMessage = new Message();
            String sendEncryptedMsg = aes.encrypt("Message body SENDER", DH_KEY);
            sendMessage.setBody(sendEncryptedMsg);
            Chat sendChat = senderCm.chatWith(recEBJid);
            sendChat.send(sendMessage);
            System.out.println("Message sent to " + xmppReceiver.getName());

            // receiver sends a message to sender
            String sendJid = xmppSender.getName() + "@" + xmppSender.getHost();
            EntityBareJid sendEBJid = JidCreate.entityBareFrom(sendJid);
            Message recMessage = new Message();
            String recEncryptedMsg = aes.encrypt("Message body RECEIVER", DH_KEY);
            recMessage.setBody(recEncryptedMsg);
            Chat recChat = receiverCm.chatWith(sendEBJid);
            recChat.send(recMessage);
            System.out.println("Message sent to " + xmppSender.getName());
            */

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        //remember to disconnect when done
    }

    private static UserGeneratorCallback userGeneratorCallback = new UserGeneratorCallback() {

        @Override
        public void onXmppUserCreated(String server, String idUser, ArrayList<XmppUser> users, boolean fromPSL) {
            System.out.println("Users created");
        }
    };
}
