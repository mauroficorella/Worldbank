package com.progettoMP2018.clashers.worldbank.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.github.chrisbanes.photoview.PhotoView;
import com.progettoMP2018.clashers.worldbank.R;

import java.util.Objects;

//activity che serve a far vedere l'immagine a tutto schermo una volta selezionata dalla lista
public class FullImageActivity extends AppCompatActivity {
    String imagePath;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.blank_title);
        toolbar.setVisibility(View.GONE);

        Intent i = getIntent();

        imagePath = i.getExtras().getString("image_selected");

        PhotoView photoView = findViewById(R.id.full_image);
        photoView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        photoView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(toolbar.getVisibility() == View.GONE){
                    toolbar.setVisibility(View.VISIBLE);
                }else{
                    toolbar.setVisibility(View.GONE);
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        android.support.v7.widget.ShareActionProvider mShareActionProvider = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        imagePath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,"title", null);
        Uri bmpUri = Uri.parse(imagePath);
        //creo l'intent per lo sharing
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = getString(R.string.share_message);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        sharingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        sharingIntent.setType("image/png");
        //setto l'intent per lo sharing
        mShareActionProvider.setShareIntent(sharingIntent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            toolbar.setVisibility(View.GONE);
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
