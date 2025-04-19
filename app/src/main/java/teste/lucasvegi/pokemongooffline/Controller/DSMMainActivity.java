package teste.lucasvegi.pokemongooffline.Controller;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import teste.lucasvegi.pokemongooffline.Model.DSMControllerFacadeSingleton;
import teste.lucasvegi.pokemongooffline.Model.DSMClient;
import teste.lucasvegi.pokemongooffline.R;

/**
 * Main activity for the DSM-integrated app
 */
public class DSMMainActivity extends AppCompatActivity {
    private static final String TAG = "DSMMainActivity";
    
    private DSMClient dsmClient;
    private TextView textEvent;
    private ImageView imageLogo;
    private Button buttonTreasureHunt;
    private Button buttonTradeTreasures;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsm_main);
        
        // Initialize views
        textEvent = findViewById(R.id.textEvent);
        imageLogo = findViewById(R.id.imageLogo);
        TextView textWelcome = findViewById(R.id.textWelcome);
        buttonTreasureHunt = findViewById(R.id.buttonTreasureHunt);
        buttonTradeTreasures = findViewById(R.id.buttonTradeTreasures);
        
        // Set welcome message with user's name
        String welcomeMessage = "Welcome, " + DSMControllerFacadeSingleton.getInstance().getUsuario().getNome() + "!";
        textWelcome.setText(welcomeMessage);
        
        // Initialize DSM client
        dsmClient = DSMClient.getInstance();
        
        // Connect to DSM network
        if (!dsmClient.connect("bootstrap.dsm.net:4001")) {
            textEvent.setText("Failed to connect to DSM network");
        }
        
        // Load DSM logo from assets
        loadLogoFromAssets();
        
        // Update event information
        updateEventInfo();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Update event info when returning to this activity
        updateEventInfo();
    }
    
    private void loadLogoFromAssets() {
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("IMG_7010.PNG"); // Use middle-sized chest image as logo
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imageLogo.setImageBitmap(bitmap);
            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error loading logo: " + e.getMessage());
        }
    }
    
    private void updateEventInfo() {
        // Check if there's a current active event
        DSMClient.RegionEvent currentEvent = dsmClient.getCurrentActiveEvent();
        
        if (currentEvent != null) {
            // Format dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            String endDate = dateFormat.format(new Date(currentEvent.getEndTime()));
            
            // Update text
            textEvent.setText("Active Event: " + currentEvent.getRegionId() + 
                    "\nEnds: " + endDate);
            buttonTreasureHunt.setEnabled(true);
        } else {
            // Get the next scheduled event
            DSMClient.RegionEvent nextEvent = null;
            long currentTime = System.currentTimeMillis();
            
            // Find the next event that hasn't started yet
            for (String regionId : new String[]{"NA-East", "EU-West", "ASIA-Pacific", "SA-Central"}) {
                DSMClient.RegionEvent event = dsmClient.getRegionEvent(regionId);
                if (event != null && event.getStartTime() > currentTime) {
                    if (nextEvent == null || event.getStartTime() < nextEvent.getStartTime()) {
                        nextEvent = event;
                    }
                }
            }
            
            if (nextEvent != null) {
                // Format dates
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                String startDate = dateFormat.format(new Date(nextEvent.getStartTime()));
                
                textEvent.setText("Next Event: " + nextEvent.getRegionId() + 
                        "\nStarts: " + startDate);
            } else {
                textEvent.setText("No active or upcoming treasure hunt events");
            }
            
            buttonTreasureHunt.setEnabled(false);
        }
    }
    
    public void onTreasureHuntClick(View view) {
        Intent intent = new Intent(this, TreasureHuntActivity.class);
        startActivity(intent);
    }
    
    public void onTradeTreasuresClick(View view) {
        // This would link to the trading system
        // For now, just show that it's coming soon
        textEvent.setText("Trading feature coming soon!");
    }
    
    public void onMapClick(View view) {
        Intent intent = new Intent(this, DSMMapActivity.class);
        startActivity(intent);
    }
    
    public void onProfileClick(View view) {
        Intent intent = new Intent(this, PerfilActivity.class);
        startActivity(intent);
    }
}