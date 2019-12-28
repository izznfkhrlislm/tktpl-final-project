package id.ac.ui.cs.mobileprogramming.izzanfi.musicx;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainMenuActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private ArrayList<Song> songList;
    private RecyclerView songListRv;

    private MusicService service;
    private Intent playIntent;
    private SongAdapter adapter;
    private boolean musicBound = false;

    private String currentTitle;
    private String currentArtist;
    private String currentAlbum;

    private MusicController controller;
    private BroadcastReceiver networkStatusReceiver;

    private boolean paused = false, playbackPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        songListRv = findViewById(R.id.song_list);
        songList = new ArrayList<>();

        networkStatusReceiver = new NetworkChangeReceiver();
        registerNetworkBroadcast();

        checkUserPermission();
        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song song, Song t1) {
                return song.getTitle().compareTo(t1.getTitle());
            }
        });

        adapter = new SongAdapter(songList, this);
        songListRv.setLayoutManager(new LinearLayoutManager(this));
        songListRv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        setController();
    }

    private void registerNetworkBroadcast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(networkStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(networkStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkBroadcast() {
        try {
            unregisterReceiver(networkStatusReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            System.out.println("onServiceConnected!");
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            service = binder.getService();
            service.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        System.out.println("onStart!");
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void songPicked(View view) {
        int indexNo = Integer.parseInt(view.findViewById(R.id.song_name).getTag().toString());

        Song currSong = songList.get(indexNo);
        this.currentTitle = currSong.getTitle();
        this.currentAlbum = currSong.getAlbum();
        this.currentArtist = currSong.getArtist();

        service.setSong(indexNo);
        service.playSong();
        if (playbackPaused) {
            playbackPaused = false;
            setController();
        }
        controller.show(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_end:
                stopService(playIntent);
                service = null;
                System.exit(0);
                break;
            case R.id.action_shuffle:
                boolean shuffle = service.toggleShuffleBtn();
                Toast.makeText(this, shuffle ? "Shuffle Mode on!" : "Shuffle mode off!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_now_playing:
                System.out.println("now playing clicked!");
                if (this.currentArtist != null) {
                    Intent intent = new Intent(this, NowPlayingActivity.class);
                    intent.putExtra("title", this.currentTitle);
                    intent.putExtra("album", this.currentAlbum);
                    intent.putExtra("artist", this.currentArtist);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "No music played yet!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getSongList() {
        ContentResolver musicContentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";

        Cursor songCursor = musicContentResolver.query(songUri, null, selection, null, null);
        if (songCursor != null) {
            if (songCursor.moveToFirst()) {
                do {
                    int titleColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int idColumn = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    int artistColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    int albumColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                    int urlColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                    long currentID = songCursor.getLong(idColumn);
                    String currentTitle = songCursor.getString(titleColumn);
                    String currentArtist = songCursor.getString(artistColumn);
                    String currentAlbum = songCursor.getString(albumColumn);
                    String currentUrl = songCursor.getString(urlColumn);

                    songList.add(new Song(currentID, currentTitle, currentArtist, currentAlbum, currentUrl));
                } while (songCursor.moveToNext());
            }
            songCursor.close();
            adapter = new SongAdapter(songList, this);
            adapter.notifyDataSetChanged();
        }
    }

    private void checkUserPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("You need to grant this permission for MusicX to operate properly.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainMenuActivity.this,
                                    new String[]{
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.RECORD_AUDIO,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                    }, 123);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
            return;
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainMenuActivity.this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                        }, 123);
                return;
            }
        }
        getSongList();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSongList();
                Toast.makeText(this, "Permissions Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (service != null && musicBound && service.isMediaPlayerPlaying()) {
            return service.getMediaPlayerCurrentSongPosition();
        } else return 0;
    }

    @Override
    public int getDuration() {
        if (service != null && musicBound && service.isMediaPlayerPlaying()) {
            return service.getCurrentSongDuration();
        } else return 0;
    }

    @Override
    public boolean isPlaying() {
        if (service != null && musicBound) {
            return service.isMediaPlayerPlaying();
        }

        return false;
    }

    @Override
    public void pause() {
        playbackPaused = true;
        service.pause();
    }

    @Override
    public void seekTo(int pos) {
        service.seekIntoPos(pos);
    }

    @Override
    public void start() {
        service.go();
    }

    private void setController() {
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSong();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousSong();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    private void nextSong() {
        service.playNextSongOnQueue();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private void previousSong() {
        service.previousTrack();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetworkBroadcast();
        stopService(playIntent);
        service = null;
    }
}
