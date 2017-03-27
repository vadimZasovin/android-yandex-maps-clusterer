package com.appcraftlab.maps.yandex.clusterer;

import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import ru.yandex.yandexmapkit.utils.ScreenPoint;

/**
 * Created by Admin on 27.03.2017.
 */

public class ClustererOverlay extends Overlay {

    private static final float DEFAULT_REORGANIZATION_FREQUENCY = 1f;
    private static final float DEFAULT_MAX_ZOOM_LEVEL = 17;

    private OnOverlayClickListener mOnOverlayClickListener;
    private OnClusterClickListener mOnClusterClickListener;
    private ScreenPoint mScreenPoint;

    private float mReorganizationFrequency;
    private float mMaxZoomLevel;
    private float[] mZoomLevels;
    private float mZoomLevel;
    private float mZoom;

    public ClustererOverlay(MapController mapController,
                            OnOverlayClickListener onOverlayClickListener,
                            OnClusterClickListener onClusterClickListener) {
        super(mapController);
        mOnOverlayClickListener = onOverlayClickListener;
        mOnClusterClickListener = onClusterClickListener;

        mReorganizationFrequency = DEFAULT_REORGANIZATION_FREQUENCY;
        mMaxZoomLevel = DEFAULT_MAX_ZOOM_LEVEL;
        prepareZoomLevels();
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
                consumed = onOverlayItemClick(item);
            }else if(hasOnOverlayClickListener) {
                consumed = onOverlayClick(x, y);
            }
        }

        return consumed || super.onSingleTapUp(x, y);
    }

    private boolean onOverlayItemClick(OverlayItem overlayItem){
        if(overlayItem instanceof ClusteredOverlayItem){
            ClusteredOverlayItem item = (ClusteredOverlayItem) overlayItem;
            return onClusteredOverlayItemClick(item);
        }else {
            return onOtherOverlayItemClick(overlayItem);
        }
    }

    private boolean onClusteredOverlayItemClick(ClusteredOverlayItem item){
        boolean consumed = false;
        if(item.isCluster()){
            if(mOnClusterClickListener != null){
                float zoom = getZoomToOpenCluster();
                consumed = mOnClusterClickListener.onClusterClick(item, zoom);
            }else {
                consumed = mOnOverlayClickListener.onOverlayItemClick(item);
            }
        }
        return consumed;
    }

    private float getZoomToOpenCluster(){
        return 0;
    }

    private boolean onOtherOverlayItemClick(OverlayItem item) {
        return mOnOverlayClickListener != null && mOnOverlayClickListener.onOverlayItemClick(item);
    }

    private boolean onOverlayClick(float x, float y){
        ensureScreenPoint();
        mScreenPoint.setX(x);
        mScreenPoint.setY(y);
        MapController controller = getMapController();
        GeoPoint geoPoint = controller.getGeoPoint(mScreenPoint);
        return mOnOverlayClickListener.onOverlayClick(geoPoint);
    }

    private void ensureScreenPoint(){
        if(mScreenPoint == null){
            mScreenPoint = new ScreenPoint();
        }
    }

    public void setReorganizationFrequency(float frequency){
        if(mReorganizationFrequency != frequency){
            mReorganizationFrequency = frequency;
            prepareZoomLevels();
        }
    }

    public void setMaxReorganizationZoomLevel(float zoomLevel){
        if(mMaxZoomLevel != zoomLevel){
            mMaxZoomLevel = zoomLevel;
            prepareZoomLevels();
        }
    }

    private void prepareZoomLevels(){
        int size = (int) (mMaxZoomLevel * mReorganizationFrequency + 1);
        mZoomLevels = new float[size];
        float zoomLevel = 0;
        for(int i = 0; i < size; i ++){
            mZoomLevels[i] = zoomLevel += mReorganizationFrequency;
        }
    }
}
