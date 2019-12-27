package id.ac.ui.cs.mobileprogramming.izzanfi.musicx;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> songList;
    private Context context;

    public SongAdapter(List<Song> songList, Context context) {
        this.songList = songList;
        this.context = context;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        System.out.println("onCreateViewHolder");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_song, parent,
                false);

        SongViewHolder viewHolder = new SongViewHolder(itemView);
        System.out.println("onCreateViewHolder Position: " + viewHolder.position);
        itemView.setTag(viewHolder);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        System.out.println("onBindViewHolder");
        Song songObj = songList.get(position);

        holder.setData(songObj, position);
        System.out.println("onBindViewHolder Position: " + holder.position);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        private View songView;
        private int position = 0;
        private Song currentSongObj;

        public SongViewHolder(View songView) {
            super(songView);
            this.songView = songView;
        }

        public void setData(Song currentSongObj, int position) {
            this.currentSongObj = currentSongObj;
            this.position = position;

            Log.d("setData", String.valueOf(currentSongObj.getId()));
            TextView songTitleTv = songView.findViewById(R.id.song_name);
            songTitleTv.setTag(position);
            TextView songArtistTv = songView.findViewById(R.id.song_artist_and_album);

            songTitleTv.setText(this.currentSongObj.getTitle());
            songArtistTv.setText(this.currentSongObj.getArtist() + " - " + this.currentSongObj.getAlbum());

        }

    }
}
