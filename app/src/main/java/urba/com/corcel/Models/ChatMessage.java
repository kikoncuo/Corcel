package urba.com.corcel.Models;

/**
 * Created by aleja_000 on 08/07/2016.
 */
public class ChatMessage {
    private String id;
    private boolean isMe;
    private String message;
    private String user;
    private String userId;
    private String dateTime;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public boolean getIsme() {
        return isMe;
    }
    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return dateTime;
    }

    public void setDate(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getUser(){ return user;}

    public void setUser(String user) {
        this.user = user;
    }
}