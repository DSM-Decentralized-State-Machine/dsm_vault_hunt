package teste.lucasvegi.pokemongooffline.Model.maps;

/**
 * Interface definition for a callback to be invoked when the map is ready to be used.
 * Compatible with the original Google Maps API
 */
public interface OnMapReadyCallback {
    /**
     * Called when the map is ready to be used.
     * @param map A non-null instance of a GoogleMap associated with the MapFragment.
     */
    void onMapReady(GoogleMap map);
}