package ch.supsi.isin.accedy.xmppconversation;

public class Constants {
    /** RASPISTILL COMMAND **/
    static public final String RASPISTILL_COMMAND = "raspistill";
    static public final String TIMEOUT_OPT = "--timeout";
    static public final String OUTPUT_OPT = "--output";
    static public final String WIDTH_OPT = "--width";
    static public final String HEIGHT_OPT = "--height";
    static public final String QRCODE_STR = "QR-Code:";
    static public final long CAPTURE_TIMEOUT = 5000; //msec
    static public final long COMMAND_TIMEOUT = 6000; //msec
    static public final String PICTURE_WIDTH = "800";
    static public final String PICTURE_HEIGHT = "600";
    /** ZBARIMG COMMAND **/
    static public final String ZBARIMG_COMMAND = "zbarimg";
    static public final String FILE_NAME = "/run/shm/scan.png";
    /** QR readings **/
    static public final int NUMBER_OF_QR_READING_ATTEMPTS = 3;
    /** AUDIO FILES **/
    static public final String WRONG_BUZZER = "wrong-buzzer.wav";
    static public final String CORRECT_SOUND = "sound-correct.wav";
    /** WEB **/
    static public final int WEB_SERVER_PORT = 8080;
    static public final String START_CAMERA_URI = "/startCamera";
    static public final String STOP_BLE_URI = "/stopBLE";
    static public final String QR_CODE_PARAM = "qrCode";
    /** COMMAND LIST **/
    static public final String DIFFIE_HELLMAN_TYPE = "DH";
    static public final String PROPOSED_SERVER_LIST_TYPE = "PSL";
    static public final String ACCEPTED_SERVER_LIST_TYPE = "ASL";
    static public final String JID_LIST_TYPE = "JL";
    static public final String ID_USER = "IDU";
    static public final String ID_HABOX = "IDH";
    static public final String ABORT = "ABORT";
    static public final String RESET = "RESET";
    /** CONFIG FILE **/
    static public final String CONFIG_FILE_NAME = "config.properties";
    static public final String DELIMITER = ";";
    /** XMPP **/
    static public final int XMPP_USER_LENGTH = 10;
    static public final int XMPP_PASSWORD_LENGTH = 10;
    static public final int XMPP_KEY_LENGTH = 15;
    static public final int USERS_TO_CREATE = 1; //of one type (SENDER | RECEIVER)
    static public final int MAX_CREATION_ATTEMPTS_NUMBER = 3;
    public static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final String AB_NOTTRIVIAL_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ+?!(){}.,;:/&-=%<>abcdefghijklmnopqrstuvwxyz";
    public static final int MIN_XMPP_USERS_THRESHOLD = 4;
    /** BOX **/
    static public final String HA_BOX_ID = "HaBoxID";
}
