package teste.lucasvegi.pokemongooffline.Model.maps;

import teste.lucasvegi.pokemongooffline.Model.LatLng;

/**
 * A factory class for creating CameraUpdates for OpenStreetMap
 * Compatible with the original Google Maps API
 */
public final class CameraUpdateFactory {
    
    public static CameraUpdate zoomIn() {
        return new CameraUpdate("zoomIn");
    }
    
    public static CameraUpdate zoomOut() {
        return new CameraUpdate("zoomOut");
    }
    
    public static CameraUpdate zoomTo(float zoom) {
        return new CameraUpdate(zoom);
    }
    
    public static CameraUpdate newLatLng(LatLng latLng) {
        return new CameraUpdate(latLng);
    }
    
    public static CameraUpdate newLatLngZoom(LatLng latLng, float zoom) {
        return new CameraUpdate(new Object[]{latLng, zoom});
    }
    
    public static CameraUpdate newCameraPosition(CameraPosition cameraPosition) {
        return new CameraUpdate(cameraPosition);
    }
}