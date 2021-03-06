package br.com.endcraft.me.endcraft.Managers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.Serializable;

import br.com.endcraft.me.endcraft.Movie;

/**
 * Created by JonasXPX on 19.jul.2017.
 */

public class DataMovie implements Serializable{

    private final String movie;
    private final Activity activity;
    private SharedPreferences.Editor editor;
    private SharedPreferences pre;
    public static final String PREFS_NAME = "ENDER";
    private static final String DEBUG = "DATA";

    public DataMovie(Movie movie, Activity activity) {
        this.movie = movie.getNome().replaceAll("\\s", "");
        this.activity = activity;
        Log.d(DEBUG, this.movie);
    }

    public DataMovie(String movieName, Activity activity){
        this.movie = movieName.replaceAll("\\s", "");
        this.activity = activity;
        Log.d(DEBUG, this.movie);
    }

    public void saveSeek(long position){
        pre = activity.getSharedPreferences(PREFS_NAME, 0);
        editor = pre.edit();
        editor.putLong(movie.replaceAll("\\s", "").toLowerCase(), position);
        editor.commit();
    }

    public long getSeekPosition(){
        pre = activity.getSharedPreferences(PREFS_NAME, 0);
        return pre.getLong(movie.replaceAll("\\s", "").toLowerCase(), 0);
    }
}
