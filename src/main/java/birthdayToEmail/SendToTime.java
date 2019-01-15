package birthdayToEmail;

import java.sql.ResultSet;

public class SendToTime implements Runnable {
    DatabaseToEmail bot;

    public SendToTime(DatabaseToEmail bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String query = "select * from MAILS where NEED_IN_SENDING = 1";
                ResultSet rs = DatabaseToEmail.getResultSet(query);
                while (rs.next()) {
                    String email = rs.getString(1);
                    Runnable r = new ThreadSending(email, bot);
                    Thread t = new Thread(r);
                    t.start();
                }
                DatabaseToEmail.releaseResources(rs);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            try {
                Thread.sleep(59000);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}