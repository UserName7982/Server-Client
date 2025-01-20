

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String message;
    private String Sender;
    private String Recipent;

    public Message(String message, String sender, String recipent) {
        this.message = message;
        this.Sender = sender;
        this.Recipent = recipent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        Sender = sender;
    }

    public String getRecipent() {
        return Recipent;
    }

    public void setRecipent(String recipent) {
        Recipent = recipent;
    }
    @Override
    public String toString() {
        return "Message [message=" + message + ", Sender=" + Sender + ", Recipent=" + Recipent + "]";
    }

}
