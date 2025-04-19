package teste.lucasvegi.pokemongooffline.Controller;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import teste.lucasvegi.pokemongooffline.Model.DSMControllerFacadeSingleton;
import teste.lucasvegi.pokemongooffline.Model.DSMClient;
import teste.lucasvegi.pokemongooffline.Model.DSMLimboVault;
import teste.lucasvegi.pokemongooffline.R;

/**
 * Activity for the DSM Treasure Hunt
 */
public class TreasureHuntActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "TreasureHuntActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final float CLAIM_RADIUS_METERS = 100.0f;
    
    private LocationManager locationManager;
    private String provider;
    private Location currentLocation;
    
    private DSMClient dsmClient;
    private DSMLimboVault nearbyChest;
    
    private TextView textStatus;
    private TextView textChestInfo;
    private ImageView imageChest;
    private Button buttonClaim;
    private Button buttonWithdraw;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasure_hunt);
        
        // Initialize UI elements
        textStatus = findViewById(R.id.textStatus);
        textChestInfo = findViewById(R.id.textChestInfo);
        imageChest = findViewById(R.id.imageChest);
        buttonClaim = findViewById(R.id.buttonClaim);
        buttonWithdraw = findViewById(R.id.buttonWithdraw);
        
        // Initialize DSM client
        dsmClient = DSMClient.getInstance();
        
        // Check if already connected, if not connect to bootstrap node
        if (!dsmClient.connect("bootstrap.dsm.net:4001")) {
            showToast("Failed to connect to DSM network");
        }
        
        // Subscribe to current region (default to NA-East)
        dsmClient.subscribeRegion("NA-East");
        
        // Sync with network
        dsmClient.sync();
        
        // Setup location services
        setupLocation();
        
        // Update UI
        updateUI();
    }
    
    private void setupLocation() {
        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }
        
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        // Define the criteria how to select the location provider
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(false);
        
        // Get the best provider
        provider = locationManager.getBestProvider(criteria, true);
        
        if (provider != null) {
            // Get the last known location
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                onLocationChanged(location);
            }
            
            // Register for location updates
            locationManager.requestLocationUpdates(provider, 5000, 10, this);
        } else {
            showToast("No location provider available");
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupLocation();
            } else {
                showToast("Location permission is required for Treasure Hunt");
                finish();
            }
        }
    }
    
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        
        // Check for nearby chests
        checkForNearbyChests();
        
        // Update UI
        updateUI();
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Implementation required for older Android versions
    }
    
    @Override
    public void onProviderEnabled(String provider) {
        // Implementation required for older Android versions
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        // Implementation required for older Android versions
    }
    
    private void checkForNearbyChests() {
        if (currentLocation == null) {
            return;
        }
        
        // Check if there's a current active event
        DSMClient.RegionEvent currentEvent = dsmClient.getCurrentActiveEvent();
        if (currentEvent == null) {
            textStatus.setText("No active treasure hunt event");
            return;
        }
        
        // Look for nearby chests
        nearbyChest = dsmClient.getNearbyChest(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                CLAIM_RADIUS_METERS
        );
        
        if (nearbyChest != null) {
            Log.d(TAG, "Found nearby chest: " + nearbyChest.getChestId());
        }
    }
    
    private void updateUI() {
        // Check if there's a current active event
        DSMClient.RegionEvent currentEvent = dsmClient.getCurrentActiveEvent();
        if (currentEvent == null) {
            textStatus.setText("No active treasure hunt event");
            buttonClaim.setEnabled(false);
            buttonWithdraw.setEnabled(false);
            return;
        }
        
        textStatus.setText("Active Event: " + currentEvent.getRegionId());
        
        if (nearbyChest == null) {
            textChestInfo.setText("No treasure chests nearby");
            // Use a placeholder image for now - we'll replace with DSM-themed graphics
            imageChest.setImageResource(R.drawable.pokemon_go_logo);
            buttonClaim.setEnabled(false);
            buttonWithdraw.setEnabled(false);
            return;
        }
        
        // Update chest info
        String chestInfo = "Chest ID: " + nearbyChest.getChestId() + "\n" +
                "Type: " + nearbyChest.getVariant() + "\n" +
                "Tokens: " + nearbyChest.getTokenAmount() + "\n" +
                "Status: " + nearbyChest.getStatus();
        
        textChestInfo.setText(chestInfo);
        
        // Load chest image from assets based on variant
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open(nearbyChest.getChestImageAsset());
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imageChest.setImageBitmap(bitmap);
            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error loading chest image: " + e.getMessage());
            // Fallback to system resources if asset loading fails
            switch (nearbyChest.getVariant()) {
                case BRONZE:
                    imageChest.setImageResource(android.R.drawable.ic_menu_compass);
                    break;
                case SILVER:
                    imageChest.setImageResource(android.R.drawable.ic_menu_my_calendar);
                    break;
                case GOLD:
                    imageChest.setImageResource(android.R.drawable.ic_menu_myplaces);
                    break;
            }
        }
        
        // Enable/disable buttons based on chest status
        switch (nearbyChest.getStatus()) {
            case MINTED:
                buttonClaim.setEnabled(true);
                buttonWithdraw.setEnabled(false);
                buttonClaim.setText("Unlock Chest");
                break;
            case CLAIMED:
                buttonClaim.setEnabled(false);
                
                // Only enable withdraw if the event has ended
                if (currentEvent.hasEnded()) {
                    buttonWithdraw.setEnabled(true);
                } else {
                    buttonWithdraw.setEnabled(false);
                }
                break;
            case WITHDRAWN:
                buttonClaim.setEnabled(false);
                buttonWithdraw.setEnabled(false);
                break;
            default:
                buttonClaim.setEnabled(false);
                buttonWithdraw.setEnabled(false);
                break;
        }
    }
    
    public void onClaimClick(View view) {
        if (nearbyChest == null || currentLocation == null) {
            showToast("No chest to unlock");
            return;
        }
        
        // Get current user ID
        String userId = DSMControllerFacadeSingleton.getInstance().getUsuario().getLogin();
        
        // Claim the chest (unlock vault)
        boolean success = dsmClient.claimChest(
                nearbyChest.getChestId(),
                userId,
                currentLocation.getLatitude(),
                currentLocation.getLongitude()
        );
        
        if (success) {
            showToast("Chest unlocked successfully!");
            
            // Update UI
            updateUI();
        } else {
            showToast("Failed to unlock chest");
        }
    }
    
    public void onWithdrawClick(View view) {
        if (nearbyChest == null) {
            showToast("No chest to withdraw from");
            return;
        }
        
        // Get current user ID
        String userId = DSMControllerFacadeSingleton.getInstance().getUsuario().getLogin();
        
        // Withdraw tokens
        long tokens = dsmClient.withdrawTokens(nearbyChest.getChestId(), userId);
        
        if (tokens > 0) {
            showToast("Withdrew " + tokens + " ROOT tokens!");
            
            // Update UI
            updateUI();
        } else {
            showToast("Failed to withdraw tokens");
        }
    }
    
    public void onBackClick(View view) {
        finish();
    }
    
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, message);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Register for location updates
        if (provider != null && locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(provider, 5000, 10, this);
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Remove location updates
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}
