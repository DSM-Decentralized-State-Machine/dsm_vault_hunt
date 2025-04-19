package teste.lucasvegi.pokemongooffline.Model.maps;

import android.graphics.Bitmap;

/**
 * A class to handle bitmap descriptors for OpenStreetMap
 * Compatible with the original Google Maps API
 */
public class BitmapDescriptor {
    private Bitmap bitmap;
    private int resourceId;
    
    public BitmapDescriptor(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
    
    public BitmapDescriptor(int resourceId) {
        this.resourceId = resourceId;
    }
    
    public Bitmap getBitmap() {
        return bitmap;
    }
    
    public int getResourceId() {
        return resourceId;
    }
}