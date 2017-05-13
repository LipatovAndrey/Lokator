package ru.sberbank.user7.lokator;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by user7 on 13.05.2017.
 */

public class FragmentLocator extends Fragment implements LocationListener {
    TextView position, address;
    LocationManager locationManager;
    private final static String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragmentlocator, container, false);
        position = (TextView) root.findViewById(R.id.position);
        address = (TextView) root.findViewById(R.id.address);
        return root;
    }
    private void setAddress(String s) {
        address.setText(s);
    }

    @Override
    public void onResume() {
        super.onResume();
     startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopListening();
    }
    private void startListening(){
       locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null){return;}
        boolean hasProviders = false;
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(getActivity(),PERMISSIONS,1);
        return;}
        for (int i = 0;i<locationManager.getProviders(true).size(); i++){
            locationManager.requestLocationUpdates(locationManager.getProviders(true).get(i),1000,10f,this);

        }



    }
    private void stopListening(){
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        position.setText(getString(R.string.locationformat, location.getLatitude(), location.getLongitude()));
        GeocodeTask geocodeTask = new GeocodeTask(this,location);
        geocodeTask.execute();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
   if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
       startListening();
   }else{position.setText(getString(R.string.errornotpermission));}
    }
    public static class GeocodeTask extends AsyncTask<Void,Void, String>{
        private WeakReference<FragmentLocator> fragment = new WeakReference<FragmentLocator>(null);
        Location location;
        private Context context;
        StringBuilder addres;
        public GeocodeTask(FragmentLocator fragmentLocator, Location location){
            fragment = new WeakReference<FragmentLocator>(fragmentLocator);
            this.location = location;
            this.context = fragmentLocator.getContext();
        }

        @Override
        protected String doInBackground(Void... params) {
            if(Geocoder.isPresent()){
                Geocoder geocoder = new Geocoder(context);
                List<Address> result = null;
                try {
                    result = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (result.size()>0){
                    addres = new StringBuilder();
                for(int i = 0 ; i< result.get(0).getMaxAddressLineIndex();i++){
                    String line = result.get(0).getAddressLine(i);
                    if (line!=null){
                        addres.append(line).append("\n");
                    }
                }

                }
                return String.valueOf(addres);}
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
           FragmentLocator fragmentLocator = fragment.get();
            if (fragmentLocator!=null){
                fragmentLocator.setAddress(s);
            }
        }
    }


}
