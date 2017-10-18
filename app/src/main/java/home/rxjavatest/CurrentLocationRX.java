package home.rxjavatest;


import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;
import com.patloew.rxlocation.RxLocation;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CurrentLocationRX implements LocationListener{

    private CompositeDisposable disposable = new CompositeDisposable();
    private RxLocation rxLocation;
    private LocationRequest locationRequest;
    private Context context;
    private LocationListener listener;

    public CurrentLocationRX(Context context,LocationListener listener) {
        this.context = context;
        this.listener = listener;
        init();
    }

    public Observable<Address> getLocation(double latitude, double longitude) {
        if (latitude != 0 && longitude != 0)
            return rxLocation.geocoding().fromLocation(latitude, longitude).toObservable();
        else
            return Observable.error(new NullPointerException("lat=0,lon=0"));
    }

    public Observable<Address> getLocation(String address) {
        return rxLocation.geocoding().fromLocationName(address).toObservable();
    }

    private void init() {
        rxLocation = new RxLocation(context);
        rxLocation.setDefaultTimeout(10000, TimeUnit.MILLISECONDS);
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setSmallestDisplacement(5f)
                .setInterval(10000);
        startLocationRefresh();
    }

    private void startLocationRefresh() {
        disposable.add(
                rxLocation.settings().checkAndHandleResolution(locationRequest)
                        .flatMapObservable(this::getLocationObservable)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::getLocation,
                                throwable -> Log.e("TAG", "Error fetching location/address updates " + throwable.toString()))
        );
    }

    private Observable<Location> getLocationObservable(boolean success) {
        try {
            if (success)
                return rxLocation.location().updates(locationRequest)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(this::getLocation);

            else
                return rxLocation.location().lastLocation()
                        .doOnSuccess(this::getLocation)
                        .toObservable();
        } catch (SecurityException e) {
            return Observable.error(e);
        }
    }

    public void removeUpdateLocation() {
        disposable.dispose();
    }

    @Override
    public void getLocation(Location location) {
       listener.getLocation(location);
    }

}
