package birthdayToEmail;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class BirthdayToEmail {
    public static void main(String[] args) {
        DatabaseToEmail bot = new DatabaseToEmail();
        Runnable r = new SendToTime(bot);
        Thread t = new Thread(r);
        PrintStream st = null;
        try {
            st = new PrintStream(new FileOutputStream("log.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setErr(st);
        System.setOut(st);
        t.start();
        bot.getTodayBirthdays("kalyachin@mail.ru");
    }
}
