package com.appcraftlab.maps.yandex.clusterer;

import android.support.annotation.NonNull;

import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;

/**
 * Created by Admin on 27.03.2017.
 */

public interface OnOverlayClickListener {

    boolean onOverlayClick(@NonNull GeoPoint geoPoint);

    boolean onOverlayItemClick(@NonNull OverlayItem item);
}