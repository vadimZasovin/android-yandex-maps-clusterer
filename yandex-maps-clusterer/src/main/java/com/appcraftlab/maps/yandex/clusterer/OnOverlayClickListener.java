package com.appcraftlab.maps.yandex.clusterer;

import ru.yandex.yandexmapkit.utils.GeoPoint;

/**
 * Created by Admin on 27.03.2017.
 */

public interface OnOverlayClickListener {

    boolean onOverlayClick(GeoPoint geoPoint);

    boolean onOverlayItemClick(ClusteredOverlayItem item);
}