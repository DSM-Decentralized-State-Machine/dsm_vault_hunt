package teste.lucasvegi.pokemongooffline.Util;

/**
 * Legacy NearbySearch class - now redirects to OSMNearbySearch
 * This class is kept for backward compatibility with existing code.
 */
public class NearbySearch {

    /**
     * Compatibility wrapper that redirects to OSMNearbySearch
     * @param latlng A coordinate object with lat and lng properties
     * @return A response object that mimics the PlacesSearchResponse structure
     */
    public static OSMNearbySearch.OSMSearchResponse run(Object latlng) {
        // Extract latitude and longitude from the passed object
        double lat = 0.0;
        double lng = 0.0;
        
        try {
            // Try to use reflection to access the latitude and longitude values
            // from whatever object is passed (assuming it has lat/lng or latitude/longitude properties)
            java.lang.reflect.Field latField = null;
            java.lang.reflect.Field lngField = null;
            
            try {
                latField = latlng.getClass().getField("lat");
                lngField = latlng.getClass().getField("lng");
            } catch (NoSuchFieldException e) {
                try {
                    latField = latlng.getClass().getField("latitude");
                    lngField = latlng.getClass().getField("longitude");
                } catch (NoSuchFieldException e2) {
                    // If we can't find the fields directly, try to look for a "location" field
                    try {
                        Object location = latlng.getClass().getField("location").get(latlng);
                        latField = location.getClass().getField("lat");
                        lngField = location.getClass().getField("lng");
                        lat = latField.getDouble(location);
                        lng = lngField.getDouble(location);
                        return OSMNearbySearch.run(lat, lng);
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
            }
            
            if (latField != null && lngField != null) {
                lat = latField.getDouble(latlng);
                lng = lngField.getDouble(latlng);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Call the OSM implementation
        return OSMNearbySearch.run(lat, lng);
    }
}