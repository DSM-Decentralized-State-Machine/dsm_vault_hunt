package teste.lucasvegi.pokemongooffline.Model.maps;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

/**
 * A fragment for displaying maps in OpenStreetMap
 * Compatible with the original Google Maps API
 */
public class SupportMapFragment extends Fragment {
    private GoogleMap map;
    
    public static SupportMapFragment newInstance() {
        return new SupportMapFragment();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Implementation for OSM would go here
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    public void getMapAsync(OnMapReadyCallback callback) {
        this.map = new GoogleMap();
        callback.onMapReady(map);
    }
}