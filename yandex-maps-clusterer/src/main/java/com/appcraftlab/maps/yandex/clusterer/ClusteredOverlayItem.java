package com.appcraftlab.maps.yandex.clusterer;

import android.graphics.drawable.Drawable;

import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;

/**
 * Created by Admin on 27.03.2017.
 */

public class ClusteredOverlayItem extends OverlayItem {

    public ClusteredOverlayItem(GeoPoint geoPoint, Drawable drawable) {
        super(geoPoint, drawable);
    }

    public boolean isCluster(){
        return true;
    }
}
