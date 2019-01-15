package birthdayToEmail;


import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;


public class DatabaseToEmail {

    public static final int TEXT = 0;
    public static final int PHOTO = 1;

    public static Connection getConnection() {
        try {
            Properties connInfo = new Properties();
            connInfo.put("user", "SYSDBA");
            connInfo.put("password", "masterkey");
            connInfo.put("charSet", "Cp1251");
            return DriverManager.getConnection("jdbc:firebirdsql://localhost:3050/birth", connInfo);
        } catch (Exception e) {
            Log.error(e.getMessage());
            return null;
        }
    }

    public static Statement getStatement() {
        try {
            return getConnection().createStatement();
        } catch (Exception e) {
            Log.error(e.getMessage());
            return null;
        }
    }

    public static ResultSet getResultSet(String query) {
        Log.add("Executing:" + query);
        try {
            return getStatement().executeQuery(query);
        } catch (Exception e) {
            Log.error(e.getMessage());
            return null;
        }
    }

    public static void executeUpdate(String query) {
        Log.add("Executing:" + query);
        Statement st = null;
        try {
            st = getStatement();
            st.executeUpdate(query);
        } catch (Exception e) {
            Log.error(e.getMessage());
        } finally {
            releaseResources(st);
        }
    }

    public static PreparedStatement getPreparedStatement(String sql) {
        try {
            return getConnection().prepareStatement(sql);
        } catch (Exception e) {
            Log.error(e.getMessage());
            return null;
        }
    }


    public static void executeUpdate(String query, List<Object> params) {
        Log.add("Executing:" + query);
        PreparedStatement st = null;
        try {
            st = getPreparedStatement(query);
            for (int i = 0; i < params.size(); i++) {
                st.setObject(i + 1, params.get(i));
            }
            st.executeUpdate();
        } catch (Exception e) {
            Log.error(e.getMessage());
        } finally {
            releaseResources(st);
        }
    }

    public static void releaseResources(Statement st) {
        try {
            Connection con = st.getConnection();
            con.close();
        } catch (SQLException e) {
            Log.error(e.getMessage());
        }
    }

    public static void releaseResources(ResultSet rs) {
        try {
            Statement st = rs.getStatement();
            releaseResources(st);
        } catch (SQLException e) {
            Log.error(e.getMessage());
        }
    }

    void sendAdmin(String email, String txt) {
        try {
            txt = "email=" + email + "\n" + txt;
            sendAdmin(txt);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }

    void sendAdmin(String txt) {
        try {
            String query = "select * from CHAT_INFO where NICKNAME = 'mikhan808'";
            Long id = (long) 0;
            ResultSet rs = getResultSet(query);
            while (rs.next()) {
                id = rs.getLong(1);
            }
            releaseResources(rs);
            Message message = buildMsg("kalyachin@mail.ru");
            addObjectToMsg(message, TEXT, txt);
            sendMsg(message);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }

    List<String> allEmails() {
        List<String> emails = new ArrayList<>();
        try {
            String query = "select * from MAILS";
            ResultSet rs = getResultSet(query);
            while (rs.next()) {
                emails.add(rs.getString(1));
            }
            releaseResources(rs);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return emails;
    }

    void sendAll(String txt) {
        try {
            Message message = buildMsg("kalyachin.m@mail.ru");
            addObjectToMsg(message, TEXT, txt);
            List<String> emails = allEmails();
            for (String email : emails) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            }
            sendMsg(message);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }

    /*String[] getDataForInsertBirthday(Chat chat) {
        String[] res = new String[8];
        for (int i = 0; i < 8; i++)
            res[i] = "NULL";
        try {
            String query = "SELECT * FROM DIALOGS_DATA WHERE CHAT = " + chat.getId();
            ResultSet rs = getResultSet(query);
            if (rs.next()) {
                for (int i = 2; i < 8; i++) {
                    String temp = rs.getString(i);
                    if (temp != null)
                        res[i - 1] = "'" + temp + "'";
                }
            }
            releaseResources(rs);
            return res;
        } catch (Exception e) {
            Log.error(e.getMessage());
            return res;
        }
    }*/

    int getPeopleID(String f, String i, String d) {
        return getPeopleID(f, i, null, d);
    }

    int getPeopleID(String f, String i, String o, String d) {
        if (o == null || o.trim().toUpperCase().equals("NULL") || o.trim().equals(""))
            return getPeopleID(f + " " + i, d);
        else return getPeopleID(f + " " + i + " " + o, d);
    }

    int getPeopleID(String full_name, String d) {
        int res = -1;
        try {
            String query = "select * from PEOPLE where FULL_NAME = '" + full_name + "' and BIRTHDAY = '" + d + "'";
            ResultSet rs = getResultSet(query);
            if (rs.next()) {
                res = rs.getInt(1);
            }
            releaseResources(rs);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return res;
    }

    /*void insertBirthday(Chat chat, boolean public_man) {
        try {
            String[] res = getDataForInsertBirthday(chat);
            String query = "INSERT INTO PEOPLE VALUES (\n";
            for (int i = 0; i < res.length; i++) {
                query += " " + res[i];
                if (i < res.length - 1)
                    query += ",";
            }
            query += ")";
            try {
                executeUpdate(query);
            } catch (Exception e) {
                Log.error(e.getMessage());
            }
            int id;
            if (res[3] != null)
                id = getPeopleID(res[1].replace("'", ""), res[2].replace("'", ""), res[3].replace("'", ""), res[5].replace("'", ""));
            else id = getPeopleID(res[1].replace("'", ""), res[2].replace("'", ""), res[5].replace("'", ""));
            if (public_man) {
                List<Long> chats = allChats();
                query = "INSERT INTO PUBLIC_PEOPLE VALUES (" + id + " )";
                executeUpdate(query);
                for (Long c : chats) {
                    query = "INSERT INTO VIEW_PEOPLE VALUES (" + c + " , " + id + " )";
                    executeUpdate(query);
                }
            } else {
                query = "INSERT INTO VIEW_PEOPLE VALUES (" + chat.getId() + " , " + id + " )";
                executeUpdate(query);
            }
            String txt = chat.getFirstName()+" "+chat.getLastName()+","+chat.getUserName()+" добавил человека:\n"+getFullNamePeople(id);
            sendAdmin(txt);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }*/

    /*void updateDataRecord(Chat chat, String field, String data) {
        try {
            String query = "UPDATE DIALOGS_DATA\n" +
                    "SET " + field + " = '" + data + "'\n" +
                    "where CHAT =  " + chat.getId();
            executeUpdate(query);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }*/

    /*void updateDataRecordAllNull(Chat chat) {
        try {
            String query = "UPDATE DIALOGS_DATA\n" +
                    "SET FAMILIYA = NULL,\n" +
                    "IMYA = NULL,\n" +
                    "OTCHESTVO = NULL,\n" +
                    "TELEFON = NULL,\n" +
                    "BIRTHDAY = NULL,\n" +
                    "DESCRIPTION = NULL\n" +
                    "where CHAT =  " + chat.getId();
            executeUpdate(query);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }*/


    /*void updateStatus(Chat chat, int status) {
        try {
            String query = "UPDATE DIALOGS\n" +
                    "SET STATUS = " + status + "\n" +
                    "where CHAT =  " + chat.getId();
            executeUpdate(query);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }*/

    String getFullNamePeople(int id) {
        String res = "Никакой Никак Никакович";
        try {
            String query = "select FULL_NAME from PEOPLE where ID =  " + id;
            ResultSet rs = getResultSet(query);
            if (rs.next())
                res = rs.getString(1);
            releaseResources(rs);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return res;
    }

    /*void deletePeople(Chat chat, int id) {
        try {
            String query = "delete from VIEW_PEOPLE where CHAT_ID =  " + chat.getId() + " AND PEOPLE_ID = " + id;
            executeUpdate(query);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        sendMsg(chat.getId(), "Мы вам больше не будем присылать сообщения о человеке, которого зовут:\n" + getFullNamePeople(id));
    }*/

    /*void deleteID(Chat chat) {
        try {
            String query = "delete from CHATS where ID =  " + chat.getId();
            executeUpdate(query);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }*/

    /*private void sendPhoto(String email, InputStream stream) {
        sendMsg( email, PHOTO, stream);
    }*/

    private Message buildMsg(String email) {
        try {
            Properties properties = new Properties();

            // Setup mail server
            properties.setProperty("mail.smtp.auth", "true");

            properties.setProperty("mail.smtp.starttls.enable", "true");

            properties.setProperty("mail.smtp.host", "smtp.gmail.com");

            properties.setProperty("mail.smtp.port", "587");
            Properties autenfication = new Properties();
            autenfication.load(new FileInputStream("server.properties"));

            // Get the default Session object.
            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(autenfication.getProperty("username"), autenfication.getProperty("password"));
                }
            });

            // Create a default MimeMessage object.
            Message message = new Message(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(autenfication.getProperty("username")));

            // Set To: header field of the header.
            message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(email));

            // Set Subject: header field
            message.setSubject("Дни рождения");

            return message;

            // Now set the actual message

        } catch (MessagingException mex) {
            mex.printStackTrace();
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void addObjectToMsg(Message message, int type, Object object) {
        try {
            if (type == TEXT) {
                if (message.getText() != null) {
                    message.addText((String) object);
                } else message.setText((String) object);
            }
            if (type == PHOTO) {
                Blob blob = (Blob) object;
                MimeMultipart mm = new MimeMultipart();
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setDataHandler(new DataHandler(new DataSource() {
                    @Override
                    public InputStream getInputStream() throws IOException {
                        try {
                            return blob.getBinaryStream();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public OutputStream getOutputStream() throws IOException {
                        try {
                            return blob.setBinaryStream(0);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public String getContentType() {
                        return "application/octet-stream";
                    }

                    @Override
                    public String getName() {
                        return "Фото";
                    }
                }));
                mm.addBodyPart(messageBodyPart);
                message.setContent(mm);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        // Send messag
    }

    void getTodayBirthdays(String email) {
        String query = "select * from PEOPLE P where extract( month from P.BIRTHDAY) = EXTRACT ( month from current_date)\n" +
                "and extract( day from P.BIRTHDAY) = EXTRACT ( day from current_date) ";
        String firstText = "Сегодня празднуют день рождения:";
        String vosrastText = "";
        String emptyMsg = "В нашем списке отсутствуют люди отмечающие сегодня день рождения.";
        sendInfoAboutPeople(email, query, firstText, vosrastText, emptyMsg, true, false);
    }

    void getBirthdaysForSchedule(String email) {
        String query = "select * from PEOPLE P where  extract( month from P.BIRTHDAY) = EXTRACT ( month from current_date)\n" +
                "and extract( day from P.BIRTHDAY) = EXTRACT ( day from current_date) ";
        String firstText = "Сегодня празднуют день рождения:";
        String vosrastText = "";
        String emptyMsg = null;
        sendInfoAboutPeople(email, query, firstText, vosrastText, emptyMsg, false, false);
    }

    void getBirthdaysInFewDays(String email, int x) {
        String query = "select * from PEOPLE P  where extract( month from P.BIRTHDAY) = EXTRACT ( month from current_date+" + x + ")\n" +
                "and extract( day from P.BIRTHDAY) = EXTRACT ( day from current_date+" + x + ") ";
        String firstText = "через " + x + " " + getDayFormated(x) + " празднуют день рождения:";
        String vosrastText = "На данный момент возраст:";
        String emptyMsg = "В нашем списке отсутствуют люди отмечающие день рождения через " + x + " " + getDayFormated(x) + ".";
        sendInfoAboutPeople(email, query, firstText, vosrastText, emptyMsg, true, true);
    }

    String getDayFormated(int x) {
        int y;
        if (x % 100 > 20)
            y = x % 10;
        else y = x % 100;
        switch (y) {
            case 1:
                return "день";
            case 2:
            case 3:
            case 4:
                return "дня";
            default:
                return "дней";
        }
    }

    String getYearFormated(int x) {
        int y;
        if (x % 100 > 20)
            y = x % 10;
        else y = x % 100;
        switch (y) {
            case 1:
                return "год";
            case 2:
            case 3:
            case 4:
                return "года";
            default:
                return "лет";
        }
    }

    void sendInfoAboutPeople(String email, String query, String firstMsg, String vozrastText, String emptyMsg, boolean useId, boolean showBirthday) {
        try {
            Message message = buildMsg(email);
            ResultSet rs = getResultSet(query);
            boolean first = true;
            while (rs.next()) {
                if (first) {
                    addObjectToMsg(message, TEXT, firstMsg + "\n");
                    first = false;
                }
                String id = rs.getString("ID");
                String f = rs.getString("FAMILIYA");
                String i = rs.getString("IMYA");
                String o = rs.getString("OTCHESTVO");
                String t = rs.getString("TELEFON");
                String d = rs.getString("DESCRIPTION");
                Date date = rs.getDate("BIRTHDAY");
                Blob photo = rs.getBlob("PHOTO");
                int v = rs.getInt("VOZRAST");
                String text = "\n" + f + " " + i;
                if (o != null)
                    text += " " + o;
                if (showBirthday) {
                    if (date != null)
                        text += " " + date;
                }
                text += "\n" + vozrastText + v + " " + getYearFormated(v);
                if (t != null) {
                    text += "\nТелефон:" + t;
                }
                if (d != null) {
                    text += "\n" + d;
                }
                if (useId) {
                    text += "\n№" + id + "\n";
                }
                addObjectToMsg(message, TEXT, text);
            }
            if (first) {
                if (emptyMsg != null)
                    addObjectToMsg(message, TEXT, emptyMsg);
            }
            sendMsg(message);
            releaseResources(rs);
        } catch (Exception e) {
            Log.error(e.getMessage());
            Message message = buildMsg(email);
            addObjectToMsg(message, TEXT, e.getMessage());
            sendMsg(message);
        }
    }


    void getBirthdayOfName(String email, String name) {
        String query = "select * from PEOPLE P  where UPPER(P.IMYA)='" + name.trim().toUpperCase() + "'";
        String firstText = "Люди с именем " + name + " :";
        String vosrastText = "На данный момент возраст:";
        String emptyMsg = "В нашем списке отсутствуют люди с именем " + name;
        sendInfoAboutPeople(email, query, firstText, vosrastText, emptyMsg, true, true);
    }

    private void sendMsg(MimeMessage message) {
        try {
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
