package com.appcraftlab.maps.yandex.clusterer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private Rect mCursor;
    private final int mCursorSize;
    private Rect mScanArea;
    private List<ClusteredOverlayItem> mHandled;

    private ClusterDrawableFactory mClusterDrawableFactory;

    public ClustererOverlay(MapController mapController,
                            OnOverlayClickListener onOverlayClickListener,
                            OnClusterClickListener onClusterClickListener,
                            ClusterDrawableFactory factory) {
        super(mapController);
        mOnOverlayClickListener = onOverlayClickListener;
        mOnClusterClickListener = onClusterClickListener;
        mClusterDrawableFactory = factory;

        mReorganizationFrequency = DEFAULT_REORGANIZATION_FREQUENCY;
        mMaxZoomLevel = DEFAULT_MAX_ZOOM_LEVEL;
        prepareZoomLevels();

        Context context = mapController.getContext();
        Resources resources = context.getResources();
        mCursorSize = resources.getDimensionPixelSize(R.dimen.cursor_size);
    }

    public ClustererOverlay(MapController mapController,
                            OnOverlayClickListener onOverlayClickListener,
                            OnClusterClickListener onClusterClickListener){
        this(mapController, onOverlayClickListener, onClusterClickListener, null);
    }

    public ClustererOverlay(MapController mapController,
                            OnOverlayClickListener listener){
        this(mapController, listener, null);
    }

    public ClustererOverlay(MapController mapController){
        this(mapController, null);
    }

    public void setOnOverlayClickListener(OnOverlayClickListener listener){
        mOnOverlayClickListener = listener;
    }

    public void setOnClusterClickListener(OnClusterClickListener listener){
        mOnClusterClickListener = listener;
    }

    public void setClusterDrawableFactory(ClusterDrawableFactory factory){
        mClusterDrawableFactory = factory;
    }

    @Override
    public void addOverlayItem(OverlayItem overlayItem) {
        if(!(overlayItem instanceof ClusteredOverlayItem)){
            throw new IllegalArgumentException("This Overlay works only with ClusteredOverlayItem");
        }
        ClusteredOverlayItem item = (ClusteredOverlayItem) overlayItem;
        item.setClusterDrawableFactory(mClusterDrawableFactory);
        super.addOverlayItem(overlayItem);
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
        ClusteredOverlayItem item = (ClusteredOverlayItem) overlayItem;
        boolean consumed = false;
        if(item.isCluster()){
            if(mOnClusterClickListener != null){
                float zoom = getZoomToOpenCluster();
                boolean zoomIn = !mOnClusterClickListener.onClusterClick(item, zoom);
                if(zoomIn){
                    GeoPoint point = item.getGeoPoint();
                    MapController controller = getMapController();
                    controller.setPositionAnimationTo(point, zoom);
                }
                consumed = true;
            }else {
                consumed = mOnOverlayClickListener.onOverlayItemClick(item);
            }
        }
        return consumed;
    }

    private float getZoomToOpenCluster(){
        int step = 2;
        int currentIndex = getCurrentZoomLevelIndex();
        int newIndex = currentIndex + step;
        int length = mZoomLevels.length;
        if(newIndex >= length){
            newIndex = length - 1;
        }
        return mZoomLevels[newIndex];
    }

    private int getCurrentZoomLevelIndex(){
        int length = mZoomLevels.length;
        for(int i = 0; i < length; i++){
            float zoomLevel = mZoomLevels[i];
            if(zoomLevel == mZoomLevel){
                return i;
            }
        }
        return length - 1;
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
        if(frequency <= 0){
            throw new IllegalArgumentException("frequency must be > 0");
        }
        if(mReorganizationFrequency != frequency){
            mReorganizationFrequency = frequency;
            prepareZoomLevels();
        }
    }

    public void setMaxReorganizationZoomLevel(float zoomLevel){
        if(zoomLevel <= 0){
            throw new IllegalArgumentException("zoom level must be > 0");
        }
        if(mMaxZoomLevel != zoomLevel){
            mMaxZoomLevel = zoomLevel;
            prepareZoomLevels();
        }
    }

    private void prepareZoomLevels(){
        int size = (int) (mMaxZoomLevel / mReorganizationFrequency + 1);
        mZoomLevels = new float[size];
        float zoomLevel = 1f;
        for(int i = 0; i < size; i++){
            mZoomLevels[i] = zoomLevel;
            zoomLevel += mReorganizationFrequency;
        }
    }

    public void setZoomLevels(float[] zoomLevels){
        if(zoomLevels == null || zoomLevels.length == 0){
            throw new IllegalArgumentException("zoom levels must not be null or empty");
        }
        mZoomLevels = zoomLevels;
        Arrays.sort(mZoomLevels);
    }

    @Override
    public List prepareDraw() {
        List retval = super.prepareDraw();
        if(hasItems() && zoomLevelChanged()){
            reorganize();
        }
        return retval;
    }

    private boolean hasItems(){
        List<ClusteredOverlayItem> items = getItems();
        return items != null && !items.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private List<ClusteredOverlayItem> getItems(){
        return (List<ClusteredOverlayItem>) c;
    }

    private boolean zoomLevelChanged(){
        MapController controller = getMapController();
        float zoom = controller.getZoomCurrent();

        if(zoom == mZoom){
            return false;
        }

        boolean grow = zoom > mZoom;
        mZoom = zoom;
        float zoomLevel = determineZoomLevel(zoom, grow);

        if(zoomLevel != mZoomLevel){
            mZoomLevel = zoomLevel;
            return true;
        }

        return false;
    }

    private float determineZoomLevel(float zoom, boolean grow){
        if(grow){
            return determineZoomLevelFromStart(zoom);
        }else{
            return determineZoomLevelFromEnd(zoom);
        }
    }

    private float determineZoomLevelFromStart(float zoom){
        int length = mZoomLevels.length;
        for (float zoomLevel : mZoomLevels) {
            if (zoom < zoomLevel) {
                return zoomLevel;
            }
        }
        return mZoomLevels[length - 1];
    }

    private float determineZoomLevelFromEnd(float zoom){
        int length = mZoomLevels.length;
        for(int i = length - 1; i >= 0; i--){
            float zoomLevel = mZoomLevels[i];
            if(zoom > zoomLevel){
                return zoomLevel;
            }
        }
        return mZoomLevels[0];
    }

    private void reorganize(){
        resetAllItems();
        updateScanArea();
        resetCursor();
        resetHandled();
        while (!scanAreaCompleted()){
            moveCursor();
            ClusteredOverlayItem item = performScan();
            if(item != null){
                addItemToHandled(item);
            }
        }
    }

    private void resetAllItems(){
        for(ClusteredOverlayItem item : getItems()){
            item.reset();
        }
    }

    private void updateScanArea(){
        ensureScanArea();
        initScanArea();
    }

    private void ensureScanArea(){
        if(mScanArea == null){
            mScanArea = new Rect();
        }
    }

    private void initScanArea(){
        float leftmostX = Float.MAX_VALUE,
                topmostY = Float.MAX_VALUE,
                rightmostX = Float.MIN_VALUE,
                bottommostY = Float.MIN_VALUE;

        for(ClusteredOverlayItem item : getItems()){
            ScreenPoint point = item.getScreenPoint();
            float x = point.getX(), y = point.getY();
            if(x < leftmostX){
                leftmostX = x;
            }else if(x > rightmostX){
                rightmostX = x;
            }
            if(y < topmostY){
                topmostY = y;
            }else if(y > bottommostY){
                bottommostY = y;
            }
        }

        int halfSize = mCursorSize / 2;
        int left = (int) (leftmostX - halfSize);
        int top = (int) (topmostY - halfSize);
        int right = (int) (rightmostX + halfSize);
        int bottom = (int) (bottommostY + halfSize);

        mScanArea.left = left;
        mScanArea.top = top;
        mScanArea.right = right;
        mScanArea.bottom = bottom;
    }

    private void resetCursor(){
        if(cursorInitialized()){
            mCursor.set(0, 0, 0, 0);
        }
    }

    private void resetHandled(){
        if(hasHandledItems()){
            mHandled.clear();
        }
    }

    private boolean hasHandledItems(){
        return mHandled != null && !mHandled.isEmpty();
    }

    private boolean scanAreaCompleted() {
        return !(!scanAreaInitialized() || !cursorInitialized())
                && mCursor.right >= mScanArea.right
                && mCursor.bottom >= mScanArea.bottom;
    }

    private boolean scanAreaInitialized(){
        return mScanArea != null && !mScanArea.isEmpty();
    }

    private boolean cursorInitialized(){
        return mCursor != null && !mCursor.isEmpty();
    }

    private void moveCursor(){
        if(cursorInitialized()){
            moveCursorToNext();
        }else {
            moveCursorToFirst();
        }
    }

    private void moveCursorToNext(){
        int newLeft, newTop;
        if(mCursor.right >= mScanArea.right){
            newLeft = mScanArea.left;
            newTop = mCursor.bottom;
        }else {
            newLeft = mCursor.right;
            newTop = mCursor.top;
        }
        mCursor.offsetTo(newLeft, newTop);
    }

    private void moveCursorToFirst(){
        ensureCursor();
        initCursorSize();
        int left = mScanArea.left;
        int top = mScanArea.top;
        mCursor.offsetTo(left, top);
    }

    private void ensureCursor(){
        if(mCursor == null){
            mCursor = new Rect();
        }
    }

    private void initCursorSize(){
        int size = calculateCursorSize();
        mCursor.right = size;
        mCursor.bottom = size;
    }

    private int calculateCursorSize(){
        return (int) (mCursorSize * normalizeCurrentZoom());
    }

    private float normalizeCurrentZoom(){
        float min = mZoomLevels[0];
        int length = mZoomLevels.length;
        float max = mZoomLevels[length - 1];
        return (mZoom - min) / (max - min);
    }

    private ClusteredOverlayItem performScan(){
        ClusteredOverlayItem item = scanAllItems();
        if(item != null && hasHandledItems()){
            for(ClusteredOverlayItem handledItem : mHandled){
                if(handledItem.formsCluster(item)){
                    handledItem.addClusteredItem(item);
                    return null;
                }
            }
        }
        return item;
    }

    private ClusteredOverlayItem scanAllItems(){
        ClusteredOverlayItem retval = null;
        for(ClusteredOverlayItem item : getItems()){
            if(item.isClustered() || !item.isClusterizable()){
                continue;
            }

            ScreenPoint point = item.getScreenPoint();
            int x = (int) point.getX();
            int y = (int) point.getY();

            if(mCursor.contains(x, y)){
                if(retval == null){
                    retval = item;
                }else {
                    retval.addClusteredItem(item);
                }
            }
        }
        return retval;
    }

    private void addItemToHandled(ClusteredOverlayItem item){
        ensureHandled();
        mHandled.add(item);
    }

    private void ensureHandled(){
        if(mHandled == null){
            mHandled = new ArrayList<>();
        }
    }
}