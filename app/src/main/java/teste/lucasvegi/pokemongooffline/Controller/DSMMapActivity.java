package teste.lucasvegi.pokemongooffline.Controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import teste.lucasvegi.pokemongooffline.Model.DSMControllerFacadeSingleton;
import teste.lucasvegi.pokemongooffline.Model.DSMClient;
import teste.lucasvegi.pokemongooffline.Model.DSMLimboVault;
import teste.lucasvegi.pokemongooffline.R;

/**
 * OpenStreetMap implementation for DSM Treasure Hunt
 */
public class DSMMapActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "DSMMapActivity";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    
    // Map and location variables
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay locationOverlay;
    private LocationManager locationManager;
    private String provider;
    private Location currentLocation;
    
    // DSM related variables
    private DSMClient dsmClient;
    private Map<Marker, DSMLimboVault> vaultMarkers = new HashMap<>();
    private boolean continueScanningForVaults = true;
    private static final double SCAN_RADIUS_KM = 0.3;
    private static final float VAULT_INTERACTION_DISTANCE_METERS = 100f;
    private static final int SCAN_INTERVAL_MINUTES = 2;
    
    /**
     * Helper method to get the appropriate chest drawable based on the vault variant
     */
    private int getChestDrawableForVariant(DSMLimboVault vault) {
        switch (vault.getVariant()) {
            case GOLD:
                return R.drawable.dsm_gold_chest;
            case SILVER:
                return R.drawable.dsm_silver_chest;
            case BRONZE:
            default:
                return R.drawable.dsm_bronze_chest;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // OSMDroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        
        setContentView(R.layout.activity_dsm_map);
        
        // Initialize the DSM client
        dsmClient = DSMClient.getInstance();
        
        // Initialize map
        map = findViewById(R.id.osmmap);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        
        // Set initial zoom level and center
        mapController = map.getController();
        mapController.setZoom(18.0);
        
        // Initialize location overlay
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        map.getOverlays().add(locationOverlay);
        
        // Configure user name display
        ImageButton imgPerfil = findViewById(R.id.botaoPerfil);
        if (DSMControllerFacadeSingleton.getInstance().getUsuario().getSexo().equals("M"))
            imgPerfil.setImageResource(R.drawable.male_profile);
        else
            imgPerfil.setImageResource(R.drawable.female_profile);
        
        TextView txtNomeUser = findViewById(R.id.txtNomeUser);
        txtNomeUser.setText(DSMControllerFacadeSingleton.getInstance().getUsuario().getLogin());
        
        // Check location permissions
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });
        
        // Set up location manager
        setupLocationManager();
        
        // Start background thread for scanning vaults
        startVaultScanningThread();
    }
    
    private void setupLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        // Define location criteria
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(false);
        
        // Get the best provider
        provider = locationManager.getBestProvider(criteria, true);
        
        if (provider != null) {
            Log.i(TAG, "Using location provider: " + provider);
            
            // Request location updates (if we have permission)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(provider, 5000, 10, this);
                
                // Try to get last known location
                Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
                if (lastKnownLocation != null) {
                    onLocationChanged(lastKnownLocation);
                }
            }
        } else {
            Log.e(TAG, "No location provider available");
        }
    }
    
    private void startVaultScanningThread() {
        new Thread(() -> {
            try {
                // Wait for location to be available first
                while (continueScanningForVaults && currentLocation == null) {
                    TimeUnit.SECONDS.sleep(1);
                }
                
                while (continueScanningForVaults) {
                    // Scan for nearby vaults if we have a location
                    if (currentLocation != null) {
                        // Scan and update on UI thread
                        runOnUiThread(() -> scanForVaults(currentLocation));
                    }
                    
                    // Wait for next scan
                    TimeUnit.MINUTES.sleep(SCAN_INTERVAL_MINUTES);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Vault scanning thread interrupted", e);
            }
        }).start();
    }
    
    private void scanForVaults(Location location) {
        // Clear old markers
        for (Marker marker : vaultMarkers.keySet()) {
            marker.remove(map);
        }
        vaultMarkers.clear();
        
        // Get nearby vaults
        List<DSMLimboVault> nearbyVaults = new ArrayList<>();
        
        // Get current active event
        DSMClient.RegionEvent currentEvent = dsmClient.getCurrentActiveEvent();
        
        // Check if there's an active event
        if (currentEvent == null) {
            // No active event, show message and return
            runOnUiThread(() -> Toast.makeText(this, "No active treasure hunt event", Toast.LENGTH_SHORT).show());
            return;
        }
        
        String regionId = currentEvent.getRegionId();
        
        // Simulate finding 3-5 random vaults in the area
        int numVaults = 3 + (int)(Math.random() * 3);
        double baseLat = location.getLatitude();
        double baseLng = location.getLongitude();
        
        // Calculate radius in degrees (approximate)
        double latDelta = SCAN_RADIUS_KM / 111.0; // 1 degree ~ 111km
        double lngDelta = SCAN_RADIUS_KM / (111.0 * Math.cos(Math.toRadians(baseLat)));
        
        for (int i = 0; i < numVaults; i++) {
            // Random position within radius
            double randLat = baseLat + (Math.random() * 2 - 1) * latDelta;
            double randLng = baseLng + (Math.random() * 2 - 1) * lngDelta;
            
            // Create vault
            DSMLimboVault vault = DSMLimboVault.createRandomChest(regionId, randLat, randLng);
            nearbyVaults.add(vault);
        }
        
        // Add markers for each vault
        for (DSMLimboVault vault : nearbyVaults) {
            addVaultMarker(vault);
        }
    }
    
    private void addVaultMarker(DSMLimboVault vault) {
        Marker marker = new Marker(map);
        
        // Set position
        GeoPoint position = new GeoPoint(vault.getLatitude(), vault.getLongitude());
        marker.setPosition(position);
        
        // Set title and snippet
        marker.setTitle(vault.getChestId());
        marker.setSnippet("Type: " + vault.getVariant() + "\nTokens: " + vault.getTokenAmount());
        
        // Set icon based on variant
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open(vault.getChestImageAsset());
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // Scale down the bitmap to a reasonable size for a marker
            bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
            
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            marker.setIcon(getResources().getDrawable(getChestDrawableForVariant(vault)));
        } catch (Exception e) {
            Log.e(TAG, "Error loading vault marker icon", e);
            marker.setIcon(getResources().getDrawable(R.drawable.dsm_bronze_chest));
        }
        
        // Set up click listener
        marker.setOnMarkerClickListener((marker1, mapView) -> {
            DSMLimboVault clickedVault = vaultMarkers.get(marker1);
            handleVaultClick(clickedVault);
            return true;
        });
        
        // Add to map and track
        map.getOverlays().add(marker);
        vaultMarkers.put(marker, vault);
    }
    
    private void handleVaultClick(DSMLimboVault vault) {
        if (vault == null || currentLocation == null) {
            return;
        }
        
        // Check if user is close enough to interact
        boolean withinRange = vault.isWithinClaimRange(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                VAULT_INTERACTION_DISTANCE_METERS);
        
        if (withinRange) {
            // Launch treasure hunt activity with this vault
            Toast.makeText(this, "Opening vault: " + vault.getChestId(), Toast.LENGTH_SHORT).show();
            
            // In a real implementation, we would pass the vault ID to TreasureHuntActivity
            
        } else {
            // Show distance message
            float[] results = new float[1];
            Location.distanceBetween(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    vault.getLatitude(), vault.getLongitude(),
                    results);
            
            float distance = results[0];
            Toast.makeText(this, 
                    "You need to get closer to this vault!\n" +
                    "Currently " + Math.round(distance) + "m away.",
                    Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        
        // Center map on current location
        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapController.setCenter(startPoint);
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Required for LocationListener interface but not used
    }
    
    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Location provider enabled: " + provider, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Location provider disabled: " + provider, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        
        // Request location updates
        if (locationManager != null && provider != null && 
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, 5000, 10, this);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        
        // Remove location updates
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        continueScanningForVaults = false;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                permissionsToRequest.add(permissions[i]);
            }
        }
        
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        } else {
            // All permissions granted, initialize location
            setupLocationManager();
        }
    }
    
    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
    
    // UI Event Handlers
    
    public void clickPokedex(View v) {
        Toast.makeText(this, "Treasure Inventory feature coming soon", Toast.LENGTH_SHORT).show();
    }
    
    public void clickPerfil(View v) {
        Toast.makeText(this, "Profile feature coming soon", Toast.LENGTH_SHORT).show();
    }
    
    public void clickMapaCaptura(View v) {
        Toast.makeText(this, "Treasure Maps feature coming soon", Toast.LENGTH_SHORT).show();
    }
    
    public void clickOvo(View v) {
        Toast.makeText(this, "Token Staking feature coming soon", Toast.LENGTH_SHORT).show();
    }
}