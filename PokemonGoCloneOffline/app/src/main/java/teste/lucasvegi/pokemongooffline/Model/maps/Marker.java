package teste.lucasvegi.pokemongooffline.Model.maps;

import teste.lucasvegi.pokemongooffline.Model.LatLng;

/**
 * A simple Marker class for OpenStreetMap implementation
 * Compatible with the original Google Maps API
 */
public class Marker {
    private LatLng position;
    private String title;
    private Object icon;
    private boolean visible;
    
    public Marker() {
        this.visible = true;
    }
    
    public LatLng getPosition() {
        return position;
    }
    
    public void setPosition(LatLng position) {
        this.position = position;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public Object getIcon() {
        return icon;
    }
    
    public void setIcon(Object icon) {
        this.icon = icon;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    private String snippet;
    private Object tag;
    
    public String getSnippet() {
        return snippet;
    }
    
    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
    
    public Object getTag() {
        return tag;
    }
    
    public void setTag(Object tag) {
        this.tag = tag;
    }
    
    public void remove() {
        // Implementation for OSM would go here
    }
}