package birthdayToEmail;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class Message extends MimeMessage {


    String text;

    public Message(Session session) {
        super(session);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) throws MessagingException {
        this.text = text;
        super.setText(text);
    }

    public void addText(String text) throws MessagingException {
        this.text += text;
        super.setText(this.text);
    }
}
