//Made by Arman Bahraini

package arman.sc2;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Locale;




public class MainActivity extends ActionBarActivity
{
    private LocationManager locationManager;
    private LocationListener locationListener;
    private SharedPreferences prefs;
    private TextView lastLocationTV;
    private ToggleButton saveToggle;
    private double currentLongitude;
    private double currentLatitude;

    private Context context;
    private static final String latKey = "com.example.app.latitude";
    private static final String longKey = "com.example.app.longitude";
    //private static final String savedKey = "com.example.app.saved";
    private boolean fromRequest = false;//will only store the coordinates if true
    //meaning the request button was pressed
    private boolean fromNavigate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastLocationTV = (TextView) findViewById(R.id.lastLocation);
        saveToggle = (ToggleButton)findViewById(R.id.saveToggle);
        context = getApplicationContext();
        prefs = this.getSharedPreferences(
                "com.example.app", Context.MODE_PRIVATE);

        double oldLatitude = Double.longBitsToDouble(prefs.getLong(latKey, 0));
        double oldLongitude = Double.longBitsToDouble(prefs.getLong(longKey,0));

        if (oldLatitude == 0 && oldLongitude == 0)
        {
            lastLocationTV.setText("Touch SAVE MY LOCATION");
        }
        else
        {
            lastLocationTV.setText("You have location " + oldLatitude
                    + "  " + oldLongitude + " saved, touch NAVIGATE to go to it");
        }
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {


                if (fromRequest && saveToggle.isChecked())
                {

                    fromRequest = false;
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();

                    //Double.ToLongBits() to keep precision of a double (64 bits)
                    //bc shareprefs stores floats (32 bits)
                    prefs.edit().putLong(latKey, Double.doubleToLongBits(currentLatitude)).apply();
                    prefs.edit().putLong(longKey, Double.doubleToLongBits(currentLongitude)).apply();
                    //     prefs.edit().putBoolean(savedKey, true);
                    double oldLatitude = Double.longBitsToDouble(prefs.getLong(latKey, 0));
                    double oldLongitude = Double.longBitsToDouble(prefs.getLong(longKey,0));

                    lastLocationTV.setText("You have location " + oldLatitude
                            + "  " + oldLongitude + " saved, touch NAVIGATE to go to it!");
                    showToast("Saved!", Toast.LENGTH_SHORT);
                }

                if (fromNavigate)
                {
                    fromNavigate = false;
                    double oldLatitude = Double.longBitsToDouble(prefs.getLong(latKey, 0));
                    double oldLongitude = Double.longBitsToDouble(prefs.getLong(longKey,0));

                    if (oldLatitude == 0 && oldLongitude == 0)
                    {
                        showToast("No location saved!", Toast.LENGTH_SHORT);
                    }
                    else
                    {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        //open google maps and navigate from current location to old
                        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f",
                                currentLatitude, currentLongitude,
                                oldLatitude,  oldLongitude);

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void saveOnClick(View view)
    {
        // locationManager.requestLocationUpdates("gps", -1, -1, locationListener);

        if (!saveToggle.isChecked())
        {
            showToast("Saving is off!", Toast.LENGTH_SHORT);
        }
        else
        {
            fromRequest = true;
            showToast("Saving in progress...", Toast.LENGTH_LONG);
            locationManager.requestSingleUpdate("gps", locationListener, null);
        }
    }

    public void navigateButtonOnClick(View v)
    {
        fromNavigate = true;
        locationManager.requestSingleUpdate("gps", locationListener,null );
        showToast("Navigating...", Toast.LENGTH_LONG);
        //TODO: YOU CURRENTLY HAVE TO WAIT FOR LOAD TIMES?? but maybe not since
        //looper is null the request takes this thread's time??
    }


    private void showToast(CharSequence cs, int duration)
    {
        Toast toast = Toast.makeText(context, cs, duration);
        toast.show();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (saveToggle.isChecked())
        {
            saveToggle.setChecked(false);
        }
        //showToast("poop", 500);
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        if (id == R.id.action_about)
        {
            showAboutDialog();
            return true;
        }

        if (id == R.id.action_purpose)
        {
            showPurposeDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public void showAboutDialog()
    {
        FragmentManager manager = getFragmentManager();
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.show(manager, "AD");
    }
    public void showPurposeDialog()
    {
        FragmentManager manager = getFragmentManager();
        PurposeDialog purposeDialog = new PurposeDialog();
        purposeDialog.show(manager, "PD");
    }

}
