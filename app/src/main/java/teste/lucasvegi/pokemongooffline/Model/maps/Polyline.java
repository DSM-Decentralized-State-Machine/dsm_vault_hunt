package teste.lucasvegi.pokemongooffline.Model.maps;

import java.util.List;

import teste.lucasvegi.pokemongooffline.Model.LatLng;

/**
 * A class to represent a polyline for OpenStreetMap
 * Compatible with the original Google Maps API
 */
public class Polyline {
    private List<LatLng> points;
    private int color;
    private float width;
    private boolean visible;
    
    public Polyline() {
        this.visible = true;
    }
    
    public List<LatLng> getPoints() {
        return points;
    }
    
    public void setPoints(List<LatLng> points) {
        this.points = points;
    }
    
    public int getColor() {
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
    
    public float getWidth() {
        return width;
    }
    
    public void setWidth(float width) {
        this.width = width;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public void remove() {
        // Implementation for OSM would go here
    }
}