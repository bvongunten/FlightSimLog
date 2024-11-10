package ch.nostromo.flightsimlog.utils;

import ch.nostromo.flightsimlog.data.coordinates.Coordinates;

public class GeoTools {

    public static double calculateDistance(Coordinates from, Coordinates to, String unit) {
        return calculateDistance(from.getCoordLat(), from.getCoordLon(), to.getCoordLat(), to.getCoordLon(), unit);
    }


    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equalsIgnoreCase("K")) {
                dist = dist * 1.609344;
            } else if (unit.equalsIgnoreCase("N")) {
                dist = dist * 0.8684;
            } else if (unit.equalsIgnoreCase("M")) {
                dist = dist * 1.609344 * 1000;
            }
            return (dist);
        }
    }





}
