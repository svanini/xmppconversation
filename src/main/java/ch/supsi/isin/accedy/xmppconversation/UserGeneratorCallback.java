package ch.supsi.isin.accedy.xmppconversation;

import java.util.ArrayList;

public interface UserGeneratorCallback {
    void onXmppUserCreated(String server, String idUser, ArrayList<XmppUser> users, boolean fromPSL);
}
