package id.ac.ui.cs.mobileprogramming.izzanfi.musicx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class NowPlayingActivity extends AppCompatActivity {
    private TextView songTitleTv;
    private TextView songArtistTv;
    private TextView songAlbumTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nowplaying);

        songTitleTv = findViewById(R.id.nowplaying_title);
        songArtistTv = findViewById(R.id.nowplaying_artist);
        songAlbumTv = findViewById(R.id.nowplaying_album);

        Intent npIntent = getIntent();
        songTitleTv.setText(npIntent.getStringExtra("title"));
        songArtistTv.setText(npIntent.getStringExtra("artist"));
        songAlbumTv.setText(npIntent.getStringExtra("album"));

        FragmentManager nowPlayingFm = getSupportFragmentManager();
        FragmentTransaction nowPlayingFt = nowPlayingFm.beginTransaction();
        nowPlayingFt.replace(R.id.visualizerFragment, VisualizerFragment.newInstance())
                .commit();
    }
}
