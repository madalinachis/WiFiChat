package com.example.madalina.wifigroupchat.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.madalina.wifigroupchat.InitThreads.ClientInit;
import com.example.madalina.wifigroupchat.InitThreads.ServerInit;
import com.example.madalina.wifigroupchat.Receivers.WifiDirectBroadcastReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.example.madalina.wifigroupchat.Application;
import com.example.madalina.wifigroupchat.R;
import com.example.madalina.wifigroupchat.adapters.UsersAdapter;
import com.example.madalina.wifigroupchat.dependencies.Injector;
import com.example.madalina.wifigroupchat.model.PeersUser;
import com.example.madalina.wifigroupchat.model.User;
import com.example.madalina.wifigroupchat.network.ErrorHandler;
import com.example.madalina.wifigroupchat.network.UserApis;
import com.example.madalina.wifigroupchat.wifiDirect.DeviceDetailFragment;
import com.example.madalina.wifigroupchat.wifiDirect.DeviceListFragment;
import com.example.madalina.wifigroupchat.wifiDirect.WiFiDirectBroadcastReceiver;
import com.example.madalina.wifigroupchat.util.GetGpsLocation;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends BaseActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        WifiP2pManager.ChannelListener, DeviceListFragment.DeviceActionListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 50;
    private static final int FAST_CEILING_IN_SECONDS = 10;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;
    private static final float METERS_PER_FEET = 0.3048f;
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;

    private SupportMapFragment mapFragment;
    private final Map<Integer, Marker> mapMarkers = new HashMap<>();
    private int mostRecentMapUpdate;
    private int selectedPostObjectId;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private Location lastLocation;
    private Location currentLocation;
    User currentUser;
    UserApis userApis;
    UsersAdapter usersAdapter;
    List<PeersUser> peersUsersList = new ArrayList<>();
    List<User> users;
    GetGpsLocation gpsLocation;
    double latitude;
    double longitude;

    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;

    public static final String TAG = "MapActivity";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;

    public static final String DEFAULT_CHAT_NAME = "";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectBroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    public static String chatName;
    public static ServerInit server;

    //Getters and Setters
    public WifiP2pManager getmManager() {
        return mManager;
    }

    public WifiP2pManager.Channel getmChannel() {
        return mChannel;
    }

    public WifiDirectBroadcastReceiver getmReceiver() {
        return mReceiver;
    }

    public IntentFilter getmIntentFilter() {
        return mIntentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.init();
        userApis = Injector.getApi(UserApis.class);
        currentUser = getIntent().getParcelableExtra("currentUser");
        setContentView(R.layout.activity_map);

        init();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        getAllUsers();
        getNearbyFriends();

        Button startChatButton = (Button) findViewById(R.id.start_chat);
        if (startChatButton != null) {
            startChatButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    //Set the chat name
                    saveChatName(MapActivity.this, currentUser.getUsername());
                    chatName = loadChatName(MapActivity.this);

                    //Start the init process
                    if (mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_OWNER) {
                        Toast.makeText(MapActivity.this, "I'm the group owner  " + mReceiver.getOwnerAddr().getHostAddress(), Toast.LENGTH_SHORT).show();
                        server = new ServerInit();
                        server.start();
                    } else if (mReceiver.isGroupeOwner() == WifiDirectBroadcastReceiver.IS_CLIENT) {
                        Toast.makeText(MapActivity.this, "I'm the client", Toast.LENGTH_SHORT).show();
                        ClientInit client = new ClientInit(mReceiver.getOwnerAddr());
                        client.start();
                    }
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_location:
                updateLocation();
                return true;
            case R.id.all_friends:
                getAllUsers();
                getNearbyFriends();
                return true;
            case R.id.hobby_friends:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String selectedHobby = prefs.getString("PREF_LIST", "Running");
                getNearbyFriendsByHobby(selectedHobby);
                getNearbyFriends();
                return true;
            case R.id.settings:
                startActivity(new Intent(MapActivity.this, SettingsActivity.class));
                return true;
            case R.id.log_out:
                Intent logoutIntent = new Intent(MapActivity.this, WelcomeActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logoutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        locationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        locationClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, MapActivity.this);
        registerReceiver(receiver, intentFilter);
        if (lastLocation != null) {
            LatLng myLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            updateZoom(myLatLng);
        }
        doMapQuery();
        doListQuery();

        registerReceiver(mReceiver, mIntentFilter);

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.v(TAG, "Discovery process succeeded");
            }

            @Override
            public void onFailure(int reason) {
                Log.v(TAG, "Discovery process failed");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (Application.APPDEBUG) {
                            Log.d(Application.APPTAG, "Connected to Google Play services");
                        }
                        break;
                    default:
                        if (Application.APPDEBUG) {
                            Log.d(Application.APPTAG, "Could not connect to Google Play services");
                        }
                        break;
                }
            default:
                if (Application.APPDEBUG) {
                    Log.d(Application.APPTAG, "Unknown request code received for the activity");
                }
                break;
        }
    }

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Application.APPDEBUG) {
                Log.d(Application.APPTAG, "Google play services available");
            }
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), Application.APPTAG);
            }
            return false;
        }
    }

    public void onConnected(Bundle bundle) {
        if (Application.APPDEBUG) {
            Log.d("Connected to location services", Application.APPTAG);
        }
        currentLocation = getLocation();
        startPeriodicUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(Application.APPTAG, "GoogleApiClient connection has been suspend");
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                if (Application.APPDEBUG) {
                    Log.d(Application.APPTAG, "An error occurred when connecting to location services.", e);
                }
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /*
     * Report location updates to the UI.
     */
    public void onLocationChanged(Location location) {

    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);
        String mac = device.deviceAddress;
        Log.d("MACCCCCCCCCCCC", mac);
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MapActivity.this, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
        usersAdapter.notifyDataSetChanged();
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
        usersAdapter.notifyDataSetChanged();
    }

    @Override
    public void getPeersList(WifiP2pDeviceList peerList) {
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        for (WifiP2pDevice device : peers)
            Log.d("NUMAAAAAAAAR", device.deviceAddress);
        peersUsersList.clear();
        for (User user : users) {
            for (WifiP2pDevice device : peers) {
                Log.d("DEVICE ADDRESSSSSSSSSSS", macBuilder(device.deviceAddress));
                Log.d("USER MACCCCCCCC", user.getMac());
                if (macBuilder(user.getMac()).equals(macBuilder(device.deviceAddress)) || user.getMac().equals("02:00:00:00:00:00")) {
                    peersUsersList.add(new PeersUser(user, device));
                }
            }
        }
        displayUsers(peersUsersList);
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this, "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE || fragment.getDevice().status == WifiP2pDevice.INVITED) {
                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(MapActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MapActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        unregisterReceiver(mReceiver);
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    public String macBuilder(String mac) {
        return mac.substring(2).toUpperCase();
    }

    /*
    * In response to a request to start updates, send a request to Location Services
    */
    private void startPeriodicUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);
    }

    /*
     * In response to a request to stop updates, send a request to Location Services
     */
    private void stopPeriodicUpdates() {
        locationClient.disconnect();
    }

    /*
     * Get the current location
     */
    private Location getLocation() {
        if (servicesConnected()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }
    }

    /*
     * Set up a query to update the list view
     */
    private void doListQuery() {
        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        if (myLoc != null) {
            usersAdapter.notifyDataSetChanged();
        }
    }

    /*
     * Set up the query to update the map view
     */
    private void doMapQuery() {
        final int myUpdateNumber = ++mostRecentMapUpdate;
        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        if (myLoc == null) {
            cleanUpMarkers(new HashSet<Integer>());
            return;
        }

        if (myUpdateNumber != mostRecentMapUpdate) {
            return;
        }
        Set<Integer> toKeep = new HashSet<>();
        for (User user : users) {
            toKeep.add(user.getUserId());
            Marker oldMarker = mapMarkers.get(user.getUserId());
            MarkerOptions markerOpts = new MarkerOptions().position(new LatLng(user.getLatitude(), user.getLongitude()));
            if (oldMarker != null) {
                if (oldMarker.getSnippet() != null) {
                    continue;
                } else {
                    oldMarker.remove();
                }
            }
            markerOpts = markerOpts.title(user.getUsername()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            Marker marker = mapFragment.getMap().addMarker(markerOpts);
            mapMarkers.put(user.getUserId(), marker);
            if (user.getUserId() == selectedPostObjectId) {
                marker.showInfoWindow();
                selectedPostObjectId = 0;
            }
        }
        cleanUpMarkers(toKeep);
    }

    /*
     * Helper method to clean up old markers
     */
    private void cleanUpMarkers(Set<Integer> markersToKeep) {
        for (int objId : new HashSet<>(mapMarkers.keySet())) {
            if (!markersToKeep.contains(objId)) {
                Marker marker = mapMarkers.get(objId);
                marker.remove();
                mapMarkers.get(objId).remove();
                mapMarkers.remove(objId);
            }
        }
    }


    /*
     * Zooms the map to show the area of interest based on the search radius
     */
    private void updateZoom(LatLng myLatLng) {
        LatLngBounds bounds = calculateBoundsWithCenter(myLatLng);
        mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 18));
    }

    /*
     * Helper method to calculate the offset for the bounds used in map zooming
     */
    private double calculateLatLngOffset(LatLng myLatLng, boolean bLatOffset) {
        double latLngOffset = OFFSET_CALCULATION_INIT_DIFF;
        float desiredOffsetInMeters = 100 * METERS_PER_FEET;
        float[] distance = new float[1];
        boolean foundMax = false;
        double foundMinDiff = 0;
        do {
            if (bLatOffset) {
                Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude
                        + latLngOffset, myLatLng.longitude, distance);
            } else {
                Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude,
                        myLatLng.longitude + latLngOffset, distance);
            }
            float distanceDiff = distance[0] - desiredOffsetInMeters;
            if (distanceDiff < 0) {
                if (!foundMax) {
                    foundMinDiff = latLngOffset;
                    latLngOffset *= 2;
                } else {
                    double tmp = latLngOffset;
                    latLngOffset += (latLngOffset - foundMinDiff) / 2;
                    foundMinDiff = tmp;
                }
            } else {
                latLngOffset -= (latLngOffset - foundMinDiff) / 2;
                foundMax = true;
            }
        } while (Math.abs(distance[0] - desiredOffsetInMeters) > OFFSET_CALCULATION_ACCURACY);
        return latLngOffset;
    }

    /*
     * Helper method to calculate the bounds for map zooming
     */
    LatLngBounds calculateBoundsWithCenter(LatLng myLatLng) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        double lngDifference = calculateLatLngOffset(myLatLng, false);
        LatLng east = new LatLng(myLatLng.latitude, myLatLng.longitude + lngDifference);
        builder.include(east);
        LatLng west = new LatLng(myLatLng.latitude, myLatLng.longitude - lngDifference);
        builder.include(west);
        double latDifference = calculateLatLngOffset(myLatLng, true);
        LatLng north = new LatLng(myLatLng.latitude + latDifference, myLatLng.longitude);
        builder.include(north);
        LatLng south = new LatLng(myLatLng.latitude - latDifference, myLatLng.longitude);
        builder.include(south);
        return builder.build();
    }

    /*
     * Show a dialog returned by Google Play services for the connection error code
     */
    private void showErrorDialog(int errorCode) {
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST);
        if (errorDialog != null) {
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(errorDialog);
            errorFragment.show(getSupportFragmentManager(), Application.APPTAG);
        }
    }

    /*
     * Define a DialogFragment to display the error dialog generated in showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    private void getAllUsers() {
        runCall(userApis.getAllUsers()).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    users = response.body();
                } else {
                    ErrorHandler.showError(MapActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                ErrorHandler.showError(MapActivity.this, t);
            }
        });
    }

    private void displayUsers(List<PeersUser> users) {
        usersAdapter = new UsersAdapter(MapActivity.this, users);
        ListView postsListView = (ListView) findViewById(R.id.users_listview);
        if (postsListView != null) {
            postsListView.setAdapter(usersAdapter);
            postsListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final View fragmentDetails = findViewById(R.id.frag_detail);
                    if (fragmentDetails != null) {
                        fragmentDetails.setVisibility(View.VISIBLE);
                    }
                    final User user = usersAdapter.getItem(position).getUser();
                    selectedPostObjectId = user.getUserId();
                    mapFragment.getMap().animateCamera(
                            CameraUpdateFactory.newLatLngZoom(new LatLng(user.getLatitude(), user.getLongitude()), 16.0f), new GoogleMap.CancelableCallback() {
                                public void onFinish() {
                                    Marker marker = mapMarkers.get(user.getUserId());
                                    if (marker != null) {
                                        marker.showInfoWindow();
                                    }
                                }

                                public void onCancel() {
                                }
                            });
                    Marker marker = mapMarkers.get(user.getUserId());
                    if (marker != null) {
                        marker.showInfoWindow();
                    }
                    WifiP2pDevice device = usersAdapter.getItem(position).getDevice();
                    showDetails(device);
                }
            });
        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mapFragment.getMap().setMyLocationEnabled(true);
        mapFragment.getMap().setOnCameraChangeListener(new OnCameraChangeListener() {
            public void onCameraChange(CameraPosition position) {
                doMapQuery();
            }
        });

    }

    private void getNearbyFriendsByHobby(String selectedHobby) {
        runCall(userApis.getUsersWithHobby(selectedHobby)).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    users = response.body();
                } else {
                    ErrorHandler.showError(MapActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                ErrorHandler.showError(MapActivity.this, t);
            }
        });
    }

    private void getNearbyFriends() {
        if (!isWifiP2pEnabled) {
            Toast.makeText(MapActivity.this, R.string.p2p_off_warning, Toast.LENGTH_SHORT).show();
        }
        final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.frag_list);
        fragment.onInitiateDiscovery();
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(MapActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(MapActivity.this, "Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLocation() {
        gpsLocation = new GetGpsLocation(MapActivity.this);
        if (gpsLocation.canGetLocation()) {
            latitude = gpsLocation.getLatitude();
            longitude = gpsLocation.getLongitude();
        } else {
            gpsLocation.showSettingsAlert();
        }

        doMapQuery();
        doListQuery();

        currentUser.setLatitude(latitude);
        currentUser.setLongitude(longitude);

        runCall(userApis.update(currentUser)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("update", "succes");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    //Save the chat name to SharedPreferences
    public void saveChatName(Context context, String chatName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("chatName", chatName);
        edit.commit();
    }

    //Retrieve the chat name from SharedPreferences
    public String loadChatName(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("chatName", DEFAULT_CHAT_NAME);
    }

    public void init() {
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = WifiDirectBroadcastReceiver.createInstance();
        mReceiver.setmManager(mManager);
        mReceiver.setmChannel(mChannel);
        mReceiver.setmActivity(this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }
}
