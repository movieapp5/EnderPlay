package br.com.endcraft.me.endcraft;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.mikepenz.iconics.context.IconicsContextWrapper;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.HashMap;

import br.com.endcraft.me.endcraft.Managers.DataMovie;
import br.com.endcraft.me.endcraft.Managers.UserSettings;
import br.com.endcraft.me.endcraft.serie.DataSerie;
import br.com.endcraft.me.endcraft.serie.Series;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

/**
 * Created by JonasXPX on 18.jul.2017.
 */
public class Play extends AppCompatActivity {

    private static final String DEBUG = "Play";

    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private final static String TAG = "PLAYER";
    private LoopingMediaSource loopingSource;
    private Play instance;
    private long seek;
    private String movie;
    private TextView movie_title;
    private final BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
    private Movie moviedata;
    private Series seriesdata;
    private UserSettings settings;
    private DefaultTrackSelector trackSelector;
    private TextView audio_track_selection;
    private HashMap<String, Boolean> audioTracks;
    private Dialog dialog_audio_track;
    private SubtitleView subtitleView;
    private TextView subtitles_icon;
    private TextView lock;
    private boolean in_progress = false;
    private View progress_bar;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        settings = new UserSettings();
        setContentView(R.layout.play);
        View view = this.getLayoutInflater().inflate(R.layout.movie_title, null, false);
        this.addContentView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        movie_title = (TextView) findViewById(R.id.movie_title);
        subtitles_icon = (TextView) findViewById(R.id.subtitles);
        audio_track_selection = (TextView) findViewById(R.id.audio_track_selection);
        progress_bar = findViewById(R.id.exo_progress);
        audio_track_selection.setVisibility(View.GONE);
        instance = this;
        audio_track_selection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(instance);
                View view_audio_track = instance.getLayoutInflater().inflate(R.layout.audio_track_selection, null);
                ListView listView = (ListView) view_audio_track.findViewById(R.id.tracks);
                final String[] itens = new String[audioTracks.size()];
                audioTracks.keySet().toArray(itens);
                listView.setAdapter(new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return itens.length;
                    }

                    @Override
                    public Object getItem(int position) {
                        return itens[position];
                    }

                    @Override
                    public long getItemId(int position) {
                        return 0;
                    }

                    @Override
                    public View getView(final int position, final View convertView, ViewGroup parent) {
                        View view = instance.getLayoutInflater().inflate(R.layout.audio_track_selection_itens, parent, false);
                        final String name = (String) getItem(position);
                        final TextView trackName = (TextView) view.findViewById(R.id.track_name);
                        if(trackSelector.getParameters().preferredAudioLanguage != null && trackSelector.getParameters().preferredAudioLanguage.equalsIgnoreCase(name))
                            trackName.setTextColor(Color.BLACK);
                        else
                            trackName.setTextColor(Color.DKGRAY);
                        trackName.setEnabled(!audioTracks.get(name));
                        trackName.setText(Language.getNameByTag(name));
                        trackName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                trackSelector.setParameters(trackSelector.getParameters().withPreferredAudioLanguage((String) getItem(position)));
                                if(dialog_audio_track!=null && dialog_audio_track.isShowing()){
                                    dialog_audio_track.dismiss();
                                }
                            }
                        });
                        return view;
                    }
                });
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                builder.setView(view_audio_track);
                dialog_audio_track = builder.create();

                dialog_audio_track.show();

            }
        });

        Handler mainHandler = new Handler();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector( videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        //Layout request
        simpleExoPlayerView = new SimpleExoPlayerView(this);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoPlayer);
        simpleExoPlayerView.setUseController(true);
        simpleExoPlayerView.requestFocus();
        simpleExoPlayerView.setPlayer(player);

        //Seek and data
        seek = getIntent().getLongExtra("seek", 0) < 0 ? 0 : getIntent().getLongExtra("seek", 0);
        movie = getIntent().getStringExtra("movie");
        moviedata = (Movie) getIntent().getSerializableExtra("moviedata");
        seriesdata = (Series) getIntent().getSerializableExtra("seriesdata");

        //title [temp]
        movie_title.setText(moviedata != null ? moviedata.getNome() : seriesdata.getName());
        hiddleTitle();

        // Subtitles
        subtitleView = simpleExoPlayerView.getSubtitleView();
        subtitleView.setStyle(settings.getSubtitlesStyle());
        subtitleView.setFixedTextSize(COMPLEX_UNIT_SP, settings.getSubtitle_fontSize());
        configureSubtitles();

        Log.d(DEBUG, "Continuando: " + seek + "\nMovie:" + movie);
        play(Filmes.url_final);

    }
    public void play(String url){
        Uri videoUri = Uri.parse(url.replaceAll(" ", "%20").replaceAll("ó", "%C3%B3"));
        try{
            DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "EnderFilmes"), bandwidthMeterA);
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource videoSource;
            if(url.endsWith(".m3u8")){
                videoSource = new HlsMediaSource(videoUri, dataSourceFactory, null, null);
            } else {
                videoSource = new ExtractorMediaSource(videoUri, dataSourceFactory, extractorsFactory, null, null);
            }

            //subtitles by link
            if((moviedata != null && !moviedata.getSubtitleLink().equalsIgnoreCase("")
                    || (seriesdata != null  && seriesdata.getCurrent_episodio() != null && !seriesdata.getCurrent_episodio().getSubtitleUrl().equalsIgnoreCase("")))) {
                String link_sub = moviedata != null && !moviedata.getSubtitleLink().equalsIgnoreCase("") ? moviedata.getSubtitleLink() : seriesdata.getCurrent_episodio().getSubtitleUrl();
                Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE, Format.NO_VALUE, "en", null);
                MediaSource textMediaSource = new SingleSampleMediaSource(Uri.parse(link_sub), dataSourceFactory, textFormat, C.TIME_UNSET);
                MediaSource mediaSourceWithText = new MergingMediaSource(videoSource, textMediaSource);
                Log.d(DEBUG, "LEGENDA CARREGADA: " + link_sub);
                loopingSource = new LoopingMediaSource(mediaSourceWithText);
            } else {
                loopingSource = new LoopingMediaSource(videoSource);
            }

            player.prepare(loopingSource);
            player.seekTo(seek);

            player.addListener(new ExoPlayer.EventListener() {

                @Override
                public void onTimelineChanged(Timeline timeline, Object manifest) {
                    Log.v(DEBUG,"Listener-onTimelineChanged... TO: " + seek);
                    player.seekTo(seek);
                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                    Log.d(DEBUG,"Listener-onTracksChanged... Lenght = " + trackGroups.length);
                    audioTracks = new HashMap<>();
                    for(int x=0;x<trackGroups.length;x++){
                        if(trackGroups.get(x).getFormat(0).sampleMimeType.contains("audio")){
                            audioTracks.put(trackGroups.get(x).getFormat(0).language, false);
                        }
                    }
                    if(audioTracks.size() > 1)
                        audio_track_selection.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingChanged(boolean isLoading) {
                    Log.i(DEBUG,"Listener-onLoadingChanged... " + isLoading);
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    Log.i(DEBUG,"Listener-onPlayerStateChanged... state: " + playbackState + " - " + playWhenReady);
                    if(!playWhenReady)
                        movie_title.setVisibility(View.VISIBLE);
                    else
                        movie_title.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    if(error.getMessage() == null){
                            Toast.makeText(instance, "Erro ao executar", Toast.LENGTH_LONG).show();
                            instance.finish();
                        return;
                    }
                    Log.i(DEBUG,"Listener-onPlayerError..." + error.getMessage());
                    player.stop();
                    player.prepare(loopingSource);
                    player.setPlayWhenReady(true);
                }

                @Override
                public void onPositionDiscontinuity() {
                    Log.v(DEBUG,"Listener-onPositionDiscontinuity...");
                }

                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}
            });

            player.setPlayWhenReady(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        save();
        player.setPlayWhenReady(false);
        Log.d(DEBUG, "Player called onStop()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        Log.d(DEBUG, "Player called onRestart()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        save();
        player.release();
    }

    private void save(){
        DataMovie dataMovie = new DataMovie(movie, instance);
        dataMovie.saveSeek(player.getCurrentPosition() > 5000 ? player.getCurrentPosition() -5000: player.getCurrentPosition());
        if(seriesdata != null) {
            DataSerie dataSerie = new DataSerie(seriesdata, instance);
            String[] data = movie.split("(_|-)");
            dataSerie.setVisualized(Integer.parseInt(data[1]), Integer.parseInt(data[2]), player.getCurrentPosition());
        }
        Log.d(DEBUG, "Destruido: " + player.getCurrentPosition());
        Filmes.reloadAd();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            this.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void hiddleTitle(){
        if(in_progress){
            return;
        }
        in_progress = true;
        movie_title.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!movie_title.isShown()){
                    return;
                }
                movie_title.setVisibility(View.INVISIBLE);
                in_progress = false;
            }
        }, 5000);
    }

    private void configureSubtitles(){
        subtitles_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(subtitles_icon.getAlpha() == 1F){
                    subtitles_icon.setAlpha(0.5F);
                    subtitleView.setVisibility(View.GONE);
                } else {
                    subtitles_icon.setAlpha(1F);
                    subtitleView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private enum Language{
        INGLÊS(new String[]{"eng"}, "Inglês"),
        PORTUGUÊS(new String[]{"por"}, "Português"),
        ALEMÃO(new String[]{"ger", "deu"}, "Alemão"),
        FRANÇES(new String[]{"fre", "fra"}, "Françes"),
        RUSSO(new String[]{"rus", "rus"}, "Russo");

        private String name;
        private String[] sub;

        Language(String[] sub, String name){
            this.name = name;
            this.sub = sub;
        }

        public static String getNameByTag(String tag){
            for(Language l : Language.values()){
                if(Arrays.asList(l.sub).contains(tag)){
                    return l.name;
                }
            }
            return tag;
        }

    }

}
