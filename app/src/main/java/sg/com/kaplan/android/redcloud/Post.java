package sg.com.kaplan.android.redcloud;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Post extends PostId {

    private String user_id, image_url, title, desc, thumb_url;
    @ServerTimestamp
    private Date timestamp;

    public Post() {}

    public Post(String user_id, String image_url, String title, String desc, String thumb_url, Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.title = title;
        this.desc = desc;
        this.thumb_url = thumb_url;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
