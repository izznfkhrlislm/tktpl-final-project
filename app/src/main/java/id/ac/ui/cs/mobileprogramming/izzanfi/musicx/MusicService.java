package id.ac.ui.cs.mobileprogramming.izzanfi.musicx;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

import static id.ac.ui.cs.mobileprogramming.izzanfi.musicx.App.CHANNEL_ID;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    static {
        System.loadLibrary("native-lib");
    }

    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songs;
    private int songPos;
    private final IBinder musicBinder = new MusicBinder();
    private String songTitle = "";
    private String songArtist = "";
    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random rand;

    @Override
    public void onCreate() {
        System.out.println("MusicService onCreate called!");
        super.onCreate();
        songPos = 0;
        rand = new Random();
        mediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    public void setList(ArrayList<Song> songList) {
        songs = songList;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();

        return false;
    }

    public void playSong() {
        mediaPlayer.reset();
        Song playedSong = songs.get(songPos);
        songTitle = playedSong.getTitle();
        songArtist = playedSong.getArtist();
        long currentSongID = playedSong.getId();
        Uri currentSongUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currentSongID
        );

        try {
            System.out.println(currentSongUri.getPath());
            mediaPlayer.setDataSource(playedSong.getUrl());
        } catch (Exception e) {
            Log.e("MusicService class", "Error getting data source", e);
        }

        mediaPlayer.prepareAsync();
    }

    public void setSong(int songIndex) {
        songPos = songIndex;
    }

    @Override
    public void onCompletion(@NotNull MediaPlayer mediaPlayer) {
        if (mediaPlayer.getCurrentPosition() > 0) {
            mediaPlayer.reset();
            playNextSongOnQueue();
        }
    }

    @Override
    public boolean onError(@NotNull MediaPlayer mediaPlayer, int i, int i1) {
        Log.v("MusicPlayer", "Playback error!");
        mediaPlayer.reset();

        return false;
    }

    @Override
    public void onPrepared(@NotNull MediaPlayer mediaPlayer) {
        mediaPlayer.start();

        //notification things
        Intent notificationIntent = new Intent(this, MainMenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setTicker(songTitle)
                .setContentTitle("Now Playing")
                .setContentText(songTitle + " - " + songArtist)
                .setOngoing(true)
                .build();

        startForeground(NOTIFY_ID, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public int getMediaPlayerCurrentSongPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getCurrentSongDuration() {
        return mediaPlayer.getDuration();
    }

    public boolean isMediaPlayerPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void seekIntoPos(int pos) {
        mediaPlayer.seekTo(pos);
    }

    public void go() {
        mediaPlayer.start();
    }

    public void previousTrack() {
        songPos--;
        if (songPos < 0) {
            songPos = songs.size() - 1;
        }
        playSong();
    }

    public void playNextSongOnQueue() {
        if (shuffle) {
            int newSongPosition = songPos;
            while (newSongPosition == songPos) {
                newSongPosition = rand.nextInt(songs.size());
            }
            songPos = newSongPosition;
        } else {
            songPos++;
            if (songPos >= songs.size()) {
                songPos = 0;
            }
        }

        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public boolean toggleShuffleBtn() {
        shuffle = toggleShuffle(shuffle);
        return shuffle;
    }

    public native boolean toggleShuffle(boolean shuffleStatus);
}
