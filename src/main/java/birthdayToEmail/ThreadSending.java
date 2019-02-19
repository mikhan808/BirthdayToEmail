package birthdayToEmail;

public class ThreadSending implements Runnable {

    String email;
    DatabaseToEmail bot;

    public ThreadSending(String email, DatabaseToEmail bot) {
        this.email = email;
        this.bot = bot;
    }

    @Override
    public void run() {
        bot.getTodayInfo(email);
        try {
            String query = "update mails set time_last_sending=current_timestamp where email = '" + email + "'";
            DatabaseToEmail.executeUpdate(query);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}