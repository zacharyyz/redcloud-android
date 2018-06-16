package sg.com.kaplan.android.redcloud;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class PostId {

    @Exclude
    public String PostId;

    public <T extends PostId> T withId(@NonNull final String id) {
        this.PostId = id;
        return (T) this;
    }
}
