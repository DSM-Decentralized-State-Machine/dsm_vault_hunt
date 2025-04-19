package teste.lucasvegi.pokemongooffline.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * OSMNearbySearch - OpenStreetMap replacement for Google's Places API
 * Uses Overpass API to find points of interest near a location
 */
public class OSMNearbySearch {

    // OSM result class to store place details
    public static class OSMPlace {
        public long id;
        public String name;
        public String type;
        public double lat;
        public double lon;
        public String vicinity;
        
        @Override
        public String toString() {
            return name + " (" + type + ")";
        }
    }
    
    // Results container similar to Google's PlacesSearchResponse
    public static class OSMSearchResponse {
        public OSMPlace[] results;
        public String status;
        
        public OSMSearchResponse() {
            this.status = "UNKNOWN";
            this.results = new OSMPlace[0];
        }
    }
    
    /**
     * Run a nearby search query using OpenStreetMap's Overpass API
     * 
     * @param lat Latitude of the center point
     * @param lon Longitude of the center point
     * @param radius Search radius in meters (default: 300)
     * @return OSMSearchResponse object containing results
     */
    public static OSMSearchResponse run(double lat, double lon, int radius) {
        OSMSearchResponse response = new OSMSearchResponse();
        List<OSMPlace> places = new ArrayList<>();
        
        try {
            // Create Overpass API query
            String overpassQuery = buildOverpassQuery(lat, lon, radius);
            
            // Execute the query
            String jsonResult = executeOverpassQuery(overpassQuery);
            
            // Parse the results
            if (jsonResult != null) {
                places = parseOverpassResults(jsonResult);
                response.status = "OK";
            } else {
                response.status = "ZERO_RESULTS";
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.status = "ERROR";
        }
        
        // Convert list to array for compatibility with the original API
        response.results = places.toArray(new OSMPlace[0]);
        return response;
    }
    
    /**
     * Simplified version that uses default radius
     */
    public static OSMSearchResponse run(double lat, double lon) {
        return run(lat, lon, 300); // 300 meters default radius
    }
    
    /**
     * Build an Overpass API query to find POIs around a location
     */
    private static String buildOverpassQuery(double lat, double lon, int radiusMeters) {
        StringBuilder query = new StringBuilder();
        query.append("[out:json];");
        query.append("(");
        
        // Look for various useful POIs
        // Adjust these for more or different types of places
        query.append("node[\"amenity\"](around:").append(radiusMeters).append(",").append(lat).append(",").append(lon).append(");");
        query.append("node[\"shop\"](around:").append(radiusMeters).append(",").append(lat).append(",").append(lon).append(");");
        query.append("node[\"tourism\"](around:").append(radiusMeters).append(",").append(lat).append(",").append(lon).append(");");
        query.append("node[\"leisure\"](around:").append(radiusMeters).append(",").append(lat).append(",").append(lon).append(");");
        query.append("node[\"natural\"](around:").append(radiusMeters).append(",").append(lat).append(",").append(lon).append(");");
        
        query.append(");");
        query.append("out body;");
        
        return query.toString();
    }
    
    /**
     * Execute the Overpass API query and get the JSON response
     */
    private static String executeOverpassQuery(String query) throws IOException {
        URL url = new URL("https://overpass-api.de/api/interpreter");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        // Send the query
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = ("data=" + query).getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        // Read the response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        return response.toString();
    }
    
    /**
     * Parse the JSON response from Overpass API into OSMPlace objects
     */
    private static List<OSMPlace> parseOverpassResults(String jsonData) throws JSONException {
        List<OSMPlace> places = new ArrayList<>();
        
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray elements = jsonObject.getJSONArray("elements");
        
        for (int i = 0; i < elements.length(); i++) {
            JSONObject element = elements.getJSONObject(i);
            
            // Skip if it's not a node
            if (!element.getString("type").equals("node")) {
                continue;
            }
            
            OSMPlace place = new OSMPlace();
            place.id = element.getLong("id");
            place.lat = element.getDouble("lat");
            place.lon = element.getDouble("lon");
            
            // Parse tags if available
            if (element.has("tags")) {
                JSONObject tags = element.getJSONObject("tags");
                
                // Get name
                if (tags.has("name")) {
                    place.name = tags.getString("name");
                } else {
                    // Use a default name if none provided
                    place.name = "Place " + place.id;
                }
                
                // Determine type
                if (tags.has("amenity")) {
                    place.type = tags.getString("amenity");
                } else if (tags.has("shop")) {
                    place.type = tags.getString("shop");
                } else if (tags.has("tourism")) {
                    place.type = tags.getString("tourism");
                } else if (tags.has("leisure")) {
                    place.type = tags.getString("leisure");
                } else if (tags.has("natural")) {
                    place.type = tags.getString("natural");
                } else {
                    place.type = "point_of_interest";
                }
                
                // Create vicinity text from address data if available
                StringBuilder vicinity = new StringBuilder();
                if (tags.has("addr:street")) {
                    vicinity.append(tags.getString("addr:street"));
                    if (tags.has("addr:housenumber")) {
                        vicinity.append(" ").append(tags.getString("addr:housenumber"));
                    }
                }
                if (tags.has("addr:city")) {
                    if (vicinity.length() > 0) vicinity.append(", ");
                    vicinity.append(tags.getString("addr:city"));
                }
                
                place.vicinity = vicinity.length() > 0 ? vicinity.toString() : null;
            }
            
            // Only add places with names
            if (place.name != null && !place.name.isEmpty()) {
                places.add(place);
            }
        }
        
        return places;
    }
}
