package com.amsy.mobileoffloading.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.amsy.mobileoffloading.helper.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService {
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static LocationService locationService;
    private Location lastAvailableLocation;
    private LocationCallback locationCallback;


    public LocationService(Context context) {
        this.context = context;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                lastAvailableLocation = locationResult.getLastLocation();
            }
        };
    }

    public static LocationService getInstance(Context context) {
        if (locationService == null) {
            locationService = new LocationService(context);
        }
        return locationService;
    }

    public Location getLastAvailableLocation() {
        return this.lastAvailableLocation;
    }

    @SuppressLint("MissingPermission")
    public void start(int interval) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(interval);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, context.getMainLooper());
    }

    public void stop() {
        if (this.locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(this.locationCallback);
        }
    }


}