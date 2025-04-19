package teste.lucasvegi.pokemongooffline.Controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import teste.lucasvegi.pokemongooffline.Model.ControladoraFachadaSingleton;
import teste.lucasvegi.pokemongooffline.Model.DSMControllerFacadeSingleton;
import teste.lucasvegi.pokemongooffline.R;

/**
 * Splash screen with DSM logo
 */
public class SplashActivity extends AppCompatActivity {
    
    // Splash screen duration in milliseconds
    private static final int SPLASH_DURATION = 2000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Handler to delay transition to next screen
        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginStatus, SPLASH_DURATION);
    }
    
    /**
     * Check if user is already logged in and navigate accordingly
     */
    private void checkLoginStatus() {
        // Check if user has an active session
        if (DSMControllerFacadeSingleton.getInstance().temSessao() || 
                ControladoraFachadaSingleton.getInstance().temSessao()) {
            // User already logged in, go to map
            startActivity(new Intent(this, DSMMapActivity.class));
        } else {
            // User needs to log in
            startActivity(new Intent(this, LoginActivity.class));
        }
        
        // Close this activity
        finish();
    }
}