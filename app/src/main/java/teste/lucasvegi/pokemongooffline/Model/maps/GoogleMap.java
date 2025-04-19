package teste.lucasvegi.pokemongooffline.Model.maps;

import java.util.HashMap;
import java.util.Map;

import teste.lucasvegi.pokemongooffline.Model.LatLng;

/**
 * A class to represent GoogleMap compatibility for OpenStreetMap
 */
public class GoogleMap {
    private Map<String, Object> properties = new HashMap<>();
    private OnMarkerClickListener markerClickListener;
    
    public interface OnMarkerClickListener {
        boolean onMarkerClick(Marker marker);
    }
    
    public Marker addMarker(MarkerOptions markerOptions) {
        Marker marker = new Marker();
        marker.setPosition(markerOptions.getPosition());
        marker.setTitle(markerOptions.getTitle());
        marker.setIcon(markerOptions.getIcon());
        marker.setVisible(markerOptions.isVisible());
        return marker;
    }
    
    public Polyline addPolyline(PolylineOptions polylineOptions) {
        Polyline polyline = new Polyline();
        polyline.setPoints(polylineOptions.getPoints());
        polyline.setColor(polylineOptions.getColor());
        polyline.setWidth(polylineOptions.getWidth());
        polyline.setVisible(polylineOptions.isVisible());
        return polyline;
    }
    
    public void moveCamera(CameraUpdate update) {
        // Implementation for OSM would go here
    }
    
    public void animateCamera(CameraUpdate update) {
        // Implementation for OSM would go here
    }
    
    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        this.markerClickListener = listener;
    }
    
    public OnMarkerClickListener getOnMarkerClickListener() {
        return markerClickListener;
    }
    
    public void setMyLocationEnabled(boolean enabled) {
        properties.put("myLocationEnabled", enabled);
    }
    
    public void setMapType(int mapType) {
        properties.put("mapType", mapType);
    }
    
    public void clear() {
        // Implementation for OSM would go here
    }
    
    public void setBuildingsEnabled(boolean enabled) {
        properties.put("buildingsEnabled", enabled);
    }
    
    public boolean isBuildingsEnabled() {
        Object value = properties.get("buildingsEnabled");
        return value != null && (Boolean) value;
    }
    
    public static final int MAP_TYPE_NORMAL = 1;
    public static final int MAP_TYPE_SATELLITE = 2;
    public static final int MAP_TYPE_TERRAIN = 3;
    public static final int MAP_TYPE_HYBRID = 4;
}