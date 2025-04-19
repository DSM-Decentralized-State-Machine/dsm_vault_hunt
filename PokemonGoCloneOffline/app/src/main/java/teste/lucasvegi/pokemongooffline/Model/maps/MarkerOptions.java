package teste.lucasvegi.pokemongooffline.Model.maps;

import teste.lucasvegi.pokemongooffline.Model.LatLng;

/**
 * A class to configure options for Markers in OpenStreetMap
 * Compatible with the original Google Maps API
 */
public class MarkerOptions {
    private LatLng position;
    private String title;
    private Object icon;
    private boolean visible = true;
    
    public MarkerOptions() {
    }
    
    public MarkerOptions position(LatLng position) {
        this.position = position;
        return this;
    }
    
    public MarkerOptions title(String title) {
        this.title = title;
        return this;
    }
    
    public MarkerOptions icon(Object icon) {
        this.icon = icon;
        return this;
    }
    
    public MarkerOptions visible(boolean visible) {
        this.visible = visible;
        return this;
    }
    
    public LatLng getPosition() {
        return position;
    }
    
    public String getTitle() {
        return title;
    }
    
    public Object getIcon() {
        return icon;
    }
    
    private String snippet;
    private float alpha = 1.0f;
    
    public MarkerOptions snippet(String snippet) {
        this.snippet = snippet;
        return this;
    }
    
    public MarkerOptions alpha(float alpha) {
        this.alpha = alpha;
        return this;
    }
    
    public String getSnippet() {
        return snippet;
    }
    
    public float getAlpha() {
        return alpha;
    }
    
    public boolean isVisible() {
        return visible;
    }
}