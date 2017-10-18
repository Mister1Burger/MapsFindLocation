package home.rxjavatest;

import android.widget.EditText;

import butterknife.BindView;

/**
 * Created by java on 18.10.2017.
 */

public class EditCoordinate {



    public double getLong(){
        String lon = editText_long.getText().toString();
        double lond = Double.parseDouble(lon);
        return lond;


    }
    public double getLat(){
        String lat = editText_lat.getText().toString();
        double latd = Double.parseDouble(lat);
        return latd;
    }
}


