package com.appcraftlab.maps.yandex.clusterer;

import ru.yandex.yandexmapkit.utils.GeoPoint;

/**
 * Created by Admin on 28.03.2017.
 */

public final class YandexMapsUtils {

    public static GeoPoint getMidPoint(GeoPoint... points){
        int length = points.length;
        double x = 0, y = 0, z = 0;
        for (GeoPoint point : points) {
            double lat = point.getLat();
            double lng = point.getLon();
            lat = Math.toRadians(lat);
            lng = Math.toRadians(lng);
            x += Math.cos(lat) * Math.cos(lng);
            y += Math.cos(lat) * Math.sin(lng);
            z += Math.sin(lat);
        }

        x /= length;
        y /= length;
        z /= length;

        double longitude = Math.atan2(y, x);
        double hypot = Math.hypot(x, y);
        double latitude = Math.atan2(z, hypot);

        latitude = Math.toDegrees(latitude);
        longitude = Math.toDegrees(longitude);
        return new GeoPoint(latitude, longitude);
    }
}