package com.appcraftlab.maps.yandex.clusterer;

import android.support.annotation.NonNull;

/**
 * Created by Admin on 27.03.2017.
 */

public interface OnClusterClickListener {

    boolean onClusterClick(@NonNull ClusteredOverlayItem item, float requestedZoom);
}
