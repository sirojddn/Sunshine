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

import org.json.JSONException;

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
    public ArrayAdapter<String> myAdapter;
    public View rootView;

    public ForecastFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        myAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                myList);

        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView myView = (ListView) rootView.findViewById(R.id.listview_forecast);
        myView.setAdapter(myAdapter);


        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String myPostCode = "94043,usa";
        if (id == R.id.action_refresh) {
            new FetchWeatherTask().execute(myPostCode);

            //return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, ArrayList<String>> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        private final String QUERY_PARAM = "q";

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            //super.onPostExecute(strings);
            myAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.list_item_forecast,
                    R.id.list_item_forecast_textview,
                    strings);
            ListView myView = (ListView) rootView.findViewById(R.id.listview_forecast);
            myView.setAdapter(myAdapter);

        }

        private final String MODE_PARAM = "mode";
        private final String UNIT_PARAM = "units";
        private final String CNT_PARAM = "cnt";


        @Override
        protected ArrayList<String> doInBackground(String... postCode) {
            String format = "json";
            String units = "metric";
            int cnt = 7;
            String[] suhuMax = null;
            try {


                //there are two way to build URL, using below or using Uri.Builder method
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, postCode[0])
                        .appendQueryParameter(MODE_PARAM, format)
                        .appendQueryParameter(UNIT_PARAM, units)
                        .appendQueryParameter(CNT_PARAM, Integer.toString(cnt))
                        .build();

                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94040,usa&mode=json&units=metric&cnt=7");
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
                urlConnection.connect();


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
            //    Log.v(LOG_TAG, forecastJsonStr);

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error: ", e);

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            JSONparser parser = new JSONparser();
            try {
                suhuMax = parser.parseSuhuMax(forecastJsonStr, 7);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return new ArrayList<>(Arrays.asList(suhuMax));

        }


    }
}
