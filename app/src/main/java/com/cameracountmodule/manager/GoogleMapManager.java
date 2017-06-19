package com.cameracountmodule.manager;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GoogleMapManager implements OnMapReadyCallback, LocationListener {

    private GoogleMap googleMap;
    private Context context;
    private LatLng latLng = new LatLng(10.7703238, 106.671691);
    public boolean checkInHistoryDetail, selectLocation;
    private SupportMapFragment mapFragment;
    private Location location;
    private LocationManager locationManager;
    private String mprovider;
    public LatLng myLatLng;
    Geocoder geocoder;
    List<Address> addresses;
    boolean autoCompleteFlag;
    Marker mMarker;

    public GoogleMapManager(Context context, SupportMapFragment mapFragment, LatLng latLng, boolean autoCompleteFlag) {
        this.context = context;
        this.latLng = latLng;
        myLatLng = latLng;
        this.autoCompleteFlag = autoCompleteFlag;
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        Criteria criteria = new Criteria();
        googleMap.clear();
        mMarker = googleMap.addMarker(new MarkerOptions().position( new LatLng(10.7703238, 106.671691)).title("Location").draggable(true));
        mprovider = locationManager.getBestProvider(criteria, false);
        location = getLastBestLocation();
        if (location != null && autoCompleteFlag ==false)
            myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        locationManager.requestLocationUpdates(mprovider, 15000, 1, this);

        if (checkInHistoryDetail && latLng != null && autoCompleteFlag ==false) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            mMarker.setPosition(latLng);
            myLatLng = latLng;
        }

        if (!checkInHistoryDetail && myLatLng != null && autoCompleteFlag == false) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 18));
            mMarker.setPosition(myLatLng);
        }
        if (autoCompleteFlag==true)
        {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 18));
            mMarker.setPosition(myLatLng);
        }

        this.googleMap.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                if (checkInHistoryDetail == true && !selectLocation)
                    return;
                if (currentMaker != null)
                    currentMaker.remove();

                myLatLng = latLng;

//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.position(latLng);
//                Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.blue);
//                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(Utils.scaleDown(icon, 40, true)));

                GoogleMapManager.this.googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                mMarker.setPosition(latLng);

            }
        });
    }

    private Marker currentMaker = null;

    @Override
    public void onLocationChanged(Location location) {
        if (checkInHistoryDetail && latLng != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            mMarker.setPosition(latLng);

            myLatLng = latLng;
        }

        if (!checkInHistoryDetail && myLatLng != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 18));
            mMarker.setPosition(myLatLng);

        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    private Location getLastBestLocation() {
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }
    private String getLocationString(LatLng latLng)
    {
        String address="";
        if (latLng!=null)
        {
            try {
                geocoder = new Geocoder(context, Locale.getDefault());
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if(addresses != null && addresses.size() > 0){

                    address = addresses.get(0).getAddressLine(0);

                    if (addresses.get(0).getAddressLine(1)!=null)
                    {
                        address += ", ";
                        address += addresses.get(0).getAddressLine(1);
                    }
                    if (addresses.get(0).getAddressLine(2)!=null)
                    {
                        address += ", ";
                        address += addresses.get(0).getAddressLine(2);
                    }
                    if (addresses.get(0).getAddressLine(3)!=null)
                    {
                        address += ", ";
                        address += addresses.get(0).getAddressLine(3);
                    }
                    if (addresses.get(0).getAddressLine(4)!=null)
                    {
                        address += ", ";
                        address += addresses.get(0).getAddressLine(4);
                    }

                    if (addresses.get(0).getAddressLine(5)!=null)
                    {
                        address += ", ";
                        address += addresses.get(0).getAddressLine(5);
                    }
                }


            }

            catch (IOException exc)
            {
                Log.e("Google Map Manager", exc.toString());
            }


        }
        return address;
    }
}
