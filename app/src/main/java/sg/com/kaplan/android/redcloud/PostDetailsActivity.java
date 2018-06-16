package sg.com.kaplan.android.redcloud;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class PostDetailsActivity extends AppCompatActivity {

    private TextView postDetailTitle, postDetailDesc;
    private ImageView postDetailImage;
    private Toolbar postDetailToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        postDetailToolbar = findViewById(R.id.post_detail_toolbar);
        setSupportActionBar(postDetailToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postDetailImage = findViewById(R.id.post_detail_image);
        postDetailTitle = findViewById(R.id.post_detail_title);
        postDetailDesc = findViewById(R.id.post_detail_desc);

        // Get Extras
        Intent postDetailIntent = getIntent();
        String title = postDetailIntent.getExtras().getString("Title");
        String desc = postDetailIntent.getExtras().getString("Desc");
        String imageUri = postDetailIntent.getExtras().getString("Image");

        getSupportActionBar().setTitle(title);

        // Set Extras
        postDetailTitle.setText(title);
        postDetailDesc.setText(desc);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.image_placeholder);

        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(imageUri).into(postDetailImage);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share_post:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "RedCloud");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, "Check out my RedCloud app");
                startActivity(Intent.createChooser(sharingIntent, "Tell a friend via..."));
                break;
            default:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
