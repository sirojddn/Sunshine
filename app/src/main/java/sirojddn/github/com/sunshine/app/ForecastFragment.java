package sirojddn.github.com.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String forecastJsonStr = null;
    static final String LOG_TAG="ForecastFragment";

    public ForecastFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String[] myForecast = {"Today - Sunny - 30/25",
                "Tomorrow - Foggy - 28/21",
                "Wednesday - Rain - 21/20",
                "Thursday - Sunny - 30/25",
                "Friday - Foggy - 28/21",
                "Saturday - Rain - 21/20",
                "Sunda - Rain - 21/20"};
        ArrayList<String> myList = new ArrayList<>(Arrays.asList(myForecast));
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                myForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView myView = (ListView) rootView.findViewById(R.id.listview_forecast);
        myView.setAdapter(myAdapter);


        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String myPostCode = "94040,usa";
        if(id == R.id.action_refresh){
            new FetchWeatherTask().execute(myPostCode);
            //return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class FetchWeatherTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            try {


                Uri.Builder weatherUri = new Uri.Builder();
                weatherUri.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter("q",params[0])
                        .appendQueryParameter("mode","json")
                        .appendQueryParameter("units","metric")
                        .appendQueryParameter("cnt","7");

                Log.i(LOG_TAG, "i'm here");
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94040,usa&mode=json&units=metric&cnt=7");
                URL url = new URL(weatherUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
                urlConnection.connect();

                int status = urlConnection.getResponseCode();

             /*   InputStream in;
                if (status >= HttpStatus.SC_BAD_REQUEST) {
                    in = urlConnection.getErrorStream();
                    Log.e("MainActivity","Error");
                } else {
                    in = urlConnection.getInputStream();
                }*/

                InputStream inputStream = urlConnection.getInputStream();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }

                if (builder == null) {
                    return null;
                }

                forecastJsonStr = builder.toString();
                Log.v(LOG_TAG, forecastJsonStr);

            } catch (Exception e) {
                Log.e("ForecastFragment", "Error: ", e);

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }
            return forecastJsonStr;

        }
    }
}
