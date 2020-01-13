package ch.supsi.isin.accedy.xmppconversation;

public class XmppUser {
    private String name;
    private String password;
    private String host;
    private Object key;
    private UserType type;

    public XmppUser(String name, String password, String host) {
        this.name = name;
        this.password = password;
        this.host = host;
    }

    public XmppUser() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getKey() { return key; }

    public UserType getType() {
        return type;
    }

    public void setType(UserType type) {
        this.type = type;
    }
}
