package com.appcraftlab.maps.yandex.clusterer;

import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.ScreenPoint;

/**
 * Created by Admin on 27.03.2017.
 */

public class ClustererOverlay extends Overlay {

    private OnOverlayClickListener mOnOverlayClickListener;
    private OnClusterClickListener mOnClusterClickListener;
    private ScreenPoint mScreenPoint;

    public ClustererOverlay(MapController mapController,
                            OnOverlayClickListener onOverlayClickListener,
                            OnClusterClickListener onClusterClickListener) {
        super(mapController);
        mOnOverlayClickListener = onOverlayClickListener;
        mOnClusterClickListener = onClusterClickListener;
    }

    public ClustererOverlay(MapController mapController, OnOverlayClickListener listener){
        this(mapController, listener, null);
    }

    public ClustererOverlay(MapController mapController){
        this(mapController, null);
    }

    @Override
    public boolean onSingleTapUp(float x, float y) {
        boolean consumed = false;

        boolean hasOnOverlayClickListener = mOnOverlayClickListener != null;
        boolean hasOnClusterClickListener = mOnClusterClickListener != null;
        boolean hasListener = hasOnOverlayClickListener || hasOnClusterClickListener;

        if(hasListener){
            OverlayItem item = a(x, y);
            if(item != null){

            }
        }

        return super.onSingleTapUp(x, y);
    }
}
