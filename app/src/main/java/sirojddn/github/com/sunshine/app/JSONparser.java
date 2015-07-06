package sirojddn.github.com.sunshine.app;

import android.text.format.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Created by sirojuddin on 7/6/15.
 */
public class JSONparser {

    private final String LOG_TAG = JSONparser.class.getSimpleName();
    private int dayIndex = 0;
    final String OWM_LIST = "list";
    final String OWM_WEATHER = "weather";
    final String OWM_TEMPERATURE = "temp";
    final String OWM_MAX = "max";
    final String OWM_MIN = "min";
    final String OWM_DESCRIPTION = "main";

    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }


    public String[] parseSuhuMax(String data, int numDays) throws JSONException {
        double suhuMax = 0;

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStr = new String[numDays];




        JSONObject myObj = new JSONObject(data);
        JSONArray weatherArray = myObj.getJSONArray(OWM_LIST);

        for (int i = 0; i< weatherArray.length(); i++){
            String day;
            String desc;
            String highAndLow;

            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            desc = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject suhuObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = suhuObject.getDouble(OWM_MAX);
            double low = suhuObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStr[i] = day + " - " + desc + " - " + highAndLow;

        }

        /*for (String s:resultStr){
            Log.v(LOG_TAG, s);
        }*/


        return resultStr;
    }


    /*private static JSONObject getObjectFromArray(String tagName, int arrayIndex, JSONObject myObj) throws JSONException {
        JSONObject statsForADay = myObj.getJSONArray(tagName).getJSONObject(arrayIndex);

        return statsForADay;
    }

    private static JSONArray getArray(String tagName, JSONObject myObj) throws JSONException {
        JSONArray weather = myObj.getJSONArray(tagName);
        return weather;
    }

    private static JSONObject getObject(String tagName, JSONObject myObj) throws JSONException {
        JSONObject myObject = myObj.getJSONObject(tagName);

        return myObject;
    }*/


}
