package teste.lucasvegi.pokemongooffline.Model;

/**
 * A simple class to represent a geographical point with latitude and longitude.
 * Compatible with the OpenStreetMap implementation.
 */
public class LatLng {
    public final double latitude;
    public final double longitude;

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "LatLng{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        LatLng latLng = (LatLng) obj;
        return Double.compare(latLng.latitude, latitude) == 0 &&
               Double.compare(latLng.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        int result = 17;
        long latBits = Double.doubleToLongBits(latitude);
        long lngBits = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (latBits ^ (latBits >>> 32));
        result = 31 * result + (int) (lngBits ^ (lngBits >>> 32));
        return result;
    }
}