package teste.lucasvegi.pokemongooffline.Model.maps;

import android.graphics.Bitmap;

/**
 * A factory class for creating BitmapDescriptors for OpenStreetMap
 * Compatible with the original Google Maps API
 */
public class BitmapDescriptorFactory {
    public static final float HUE_RED = 0.0F;
    public static final float HUE_ORANGE = 30.0F;
    public static final float HUE_YELLOW = 60.0F;
    public static final float HUE_GREEN = 120.0F;
    public static final float HUE_CYAN = 180.0F;
    public static final float HUE_AZURE = 210.0F;
    public static final float HUE_BLUE = 240.0F;
    public static final float HUE_VIOLET = 270.0F;
    public static final float HUE_MAGENTA = 300.0F;
    public static final float HUE_ROSE = 330.0F;
    
    public static BitmapDescriptor fromResource(int resourceId) {
        return new BitmapDescriptor(resourceId);
    }
    
    public static BitmapDescriptor fromBitmap(Bitmap bitmap) {
        return new BitmapDescriptor(bitmap);
    }
    
    public static BitmapDescriptor defaultMarker() {
        // Default implementation
        return new BitmapDescriptor(-1);
    }
    
    public static BitmapDescriptor defaultMarker(float hue) {
        // Implementation with color hue
        return new BitmapDescriptor(-1);
    }
}