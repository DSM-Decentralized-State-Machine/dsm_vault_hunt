package teste.lucasvegi.pokemongooffline.Model.maps;

import teste.lucasvegi.pokemongooffline.Model.LatLng;

/**
 * A class to represent camera position for OpenStreetMap
 * Compatible with the original Google Maps API
 */
public final class CameraPosition {
    public final LatLng target;
    public final float zoom;
    public final float tilt;
    public final float bearing;
    
    private CameraPosition(LatLng target, float zoom, float tilt, float bearing) {
        this.target = target;
        this.zoom = zoom;
        this.tilt = tilt;
        this.bearing = bearing;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static Builder builder(CameraPosition camera) {
        return new Builder(camera);
    }
    
    public static final class Builder {
        private LatLng target;
        private float zoom;
        private float tilt;
        private float bearing;
        
        public Builder() {
        }
        
        public Builder(CameraPosition camera) {
            this.target = camera.target;
            this.zoom = camera.zoom;
            this.tilt = camera.tilt;
            this.bearing = camera.bearing;
        }
        
        public Builder target(LatLng target) {
            this.target = target;
            return this;
        }
        
        public Builder zoom(float zoom) {
            this.zoom = zoom;
            return this;
        }
        
        public Builder tilt(float tilt) {
            this.tilt = tilt;
            return this;
        }
        
        public Builder bearing(float bearing) {
            this.bearing = bearing;
            return this;
        }
        
        public CameraPosition build() {
            return new CameraPosition(target, zoom, tilt, bearing);
        }
    }
}