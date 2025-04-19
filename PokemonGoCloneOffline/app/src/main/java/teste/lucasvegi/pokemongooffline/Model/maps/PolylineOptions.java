package teste.lucasvegi.pokemongooffline.Model.maps;

import java.util.ArrayList;
import java.util.List;

import teste.lucasvegi.pokemongooffline.Model.LatLng;

/**
 * A class to configure options for Polylines in OpenStreetMap
 * Compatible with the original Google Maps API
 */
public class PolylineOptions {
    private List<LatLng> points;
    private int color;
    private float width;
    private boolean visible = true;
    
    public PolylineOptions() {
        this.points = new ArrayList<>();
    }
    
    public PolylineOptions add(LatLng point) {
        this.points.add(point);
        return this;
    }
    
    public PolylineOptions addAll(List<LatLng> points) {
        this.points.addAll(points);
        return this;
    }
    
    public PolylineOptions color(int color) {
        this.color = color;
        return this;
    }
    
    public PolylineOptions width(float width) {
        this.width = width;
        return this;
    }
    
    public PolylineOptions visible(boolean visible) {
        this.visible = visible;
        return this;
    }
    
    public List<LatLng> getPoints() {
        return points;
    }
    
    public int getColor() {
        return color;
    }
    
    public float getWidth() {
        return width;
    }
    
    public boolean isVisible() {
        return visible;
    }
}