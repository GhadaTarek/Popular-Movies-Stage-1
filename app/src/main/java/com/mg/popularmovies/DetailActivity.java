package com.mg.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {

    Integer id;
    TextView title, rate, date, synopsis;
    ImageView poster;
    String api_key = "8c3c2ba70994da771208baea73641794";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = (TextView) findViewById(R.id.title);
        rate = (TextView) findViewById(R.id.user_rating);
        date = (TextView) findViewById(R.id.release_date);
        synopsis = (TextView) findViewById(R.id.synopsis);
        poster = (ImageView) findViewById(R.id.poster_image);

        Intent intent = getIntent();
        id = intent.getIntExtra("Movie ID", 0);

        FetchMovieDetails fetchMovieDetails = new FetchMovieDetails();
        fetchMovieDetails.execute();
    }

    public class FetchMovieDetails extends AsyncTask<Void, Void, Void> {

        String LOG_TAG = "FetchMovieDetails";
        String original_title, releaseDate, plotSynopsis, poster_path;
        Double ratings;

        @Override
        protected Void doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;

            try {
                String base_url = "https://api.themoviedb.org/3/movie/";
                URL url = new URL(base_url + id + "?api_key=" + api_key);
                Log.d(LOG_TAG,"URL: " + url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if(buffer.length() == 0) {
                    return null;
                }
                movieJsonStr = buffer.toString();
                Log.d(LOG_TAG, "JSON Parsed: " + movieJsonStr);

                JSONObject main = new JSONObject(movieJsonStr);
                original_title = main.getString("original_title");
                releaseDate = main.getString("release_date");
                ratings = main.getDouble("vote_average");
                plotSynopsis = main.getString("overview");
                poster_path = "http://image.tmdb.org/t/p/w185" + main.getString("poster_path");

            }catch(Exception e){
                Log.e(LOG_TAG, "Error", e);
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            title.setText(original_title);
            rate.setText("User Ratings: " + Double.toString(ratings));
            date.setText("Release Date: " + releaseDate);
            synopsis.setText(plotSynopsis);
            poster.setScaleType(ImageView.ScaleType.CENTER_CROP);
            poster.setPadding(8, 8, 8, 8);
            Picasso.with(DetailActivity.this).load(poster_path).into(poster);
        }
    }
}
