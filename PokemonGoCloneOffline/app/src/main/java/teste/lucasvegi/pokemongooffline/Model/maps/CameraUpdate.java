package teste.lucasvegi.pokemongooffline.Model.maps;

/**
 * A camera update for OpenStreetMap
 * Compatible with the original Google Maps API
 */
public final class CameraUpdate {
    private final Object update;
    
    CameraUpdate(Object update) {
        this.update = update;
    }
    
    public Object getUpdate() {
        return update;
    }
}