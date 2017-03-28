package com.appcraftlab.maps.yandex.clusterer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import ru.yandex.yandexmapkit.utils.ScreenPoint;

/**
 * Created by Admin on 27.03.2017.
 */

public class ClusteredOverlayItem extends TaggedOverlayItem {

    private boolean mClusterizable = true;
    private ClusterDrawableFactory mDrawableFactory;
    private final int mHalfScopeSize;

    private Rect mBounds;
    private int mBackedItemsCount;
    private boolean mClustered;

    private Drawable mInitialDrawable;
    private GeoPoint mInitialGeoPoint;

    public ClusteredOverlayItem(Context context, GeoPoint geoPoint, Drawable drawable) {
        super(geoPoint, drawable);
        Resources resources = context.getResources();
        int scopeSize = resources.getDimensionPixelSize(R.dimen.scope_size);
        mHalfScopeSize = scopeSize / 2;
    }

    public void setClusterizable(boolean clusterizable){
        if(!clusterizable && isCluster()){
            reset();
        }
        mClusterizable = clusterizable;
    }

    public boolean isClusterizable(){
        return mClusterizable;
    }

    void setClusterDrawableFactory(ClusterDrawableFactory factory){
        mDrawableFactory = factory;
    }

    public boolean isCluster(){
        return mBackedItemsCount > 0;
    }

    public boolean isClustered(){
        return mClustered;
    }

    void reset(){
        if(mInitialDrawable != null){
            setDrawable(mInitialDrawable);
        }
        if(mInitialGeoPoint != null){
            setGeoPoint(mInitialGeoPoint);
        }
        mClustered = false;
        setVisible(true);
        mBackedItemsCount = 0;
        updateBounds();
    }

    private void updateBounds(){
        ensureBounds();
        ScreenPoint point = getScreenPoint();
        final float x = point.getX(), y = point.getY();
        final int xOffset;
        final int yOffset;
        if(isCluster()){
            xOffset = mBounds.width() / 2;
            yOffset = mBounds.height() / 2;
        }else {
            xOffset = mHalfScopeSize;
            yOffset = mHalfScopeSize;
        }
        mBounds.left = (int) (x - xOffset);
        mBounds.top = (int) (y - yOffset);
        mBounds.right = (int) (x + xOffset);
        mBounds.bottom = (int) (y + yOffset);
    }

    private void ensureBounds(){
        if(mBounds == null){
            mBounds = new Rect();
        }
    }

    boolean formsCluster(ClusteredOverlayItem item){
        return formsCluster(this, item);
    }

    static boolean formsCluster(ClusteredOverlayItem item1, ClusteredOverlayItem item2){
        Rect bounds1 = item1.getBounds();
        Rect bounds2 = item2.getBounds();
        return Rect.intersects(bounds1, bounds2)
                || bounds1.contains(bounds2)
                || bounds2.contains(bounds1);
    }

    private Rect getBounds(){
        updateBounds();
        return mBounds;
    }

    void addClusteredItem(ClusteredOverlayItem item){
        boolean isCluster = isCluster();
        updateBackedItemsCount(item);
        updateDrawable(isCluster);
        updateGeoPoint(item);

        Rect bounds = item.getBounds();
        mBounds.union(bounds);

        item.setVisible(false);
        item.mClustered = true;
    }

    private void updateBackedItemsCount(ClusteredOverlayItem item){
        if(item.isCluster()){
            mBackedItemsCount += item.mBackedItemsCount;
        }else {
            mBackedItemsCount++;
        }
    }

    private void updateDrawable(boolean isCluster){
        Drawable drawable;
        if(!isCluster){
            mBackedItemsCount++;
            updateBounds();
            mInitialDrawable = getDrawable();
            drawable = createClusterDrawable();
            setDrawable(drawable);
        }else {
            drawable = getDrawable();
            ClusterDrawable clusterDrawable = (ClusterDrawable) drawable;
            clusterDrawable.setClusteredItemsCount(mBackedItemsCount);
        }
    }

    private ClusterDrawable createClusterDrawable(){
        if(mDrawableFactory == null){
            throw new NullPointerException("ClusterDrawableFactory must be set");
        }
        return mDrawableFactory.create(mBackedItemsCount);
    }

    private void updateGeoPoint(ClusteredOverlayItem item){
        GeoPoint currentGeoPoint = getGeoPoint();
        if(mInitialGeoPoint == null){
            mInitialGeoPoint = currentGeoPoint;
        }
        GeoPoint[] geoPoints = new GeoPoint[]{currentGeoPoint, item.getGeoPoint()};
        GeoPoint newGeoPoint = YandexMapsUtils.getMidPoint(geoPoints);
        setGeoPoint(newGeoPoint);
    }

    @Override
    public int a(OverlayItem overlayItem) {
        if(overlayItem instanceof ClusteredOverlayItem){
            ClusteredOverlayItem other = (ClusteredOverlayItem) overlayItem;

            boolean thisCluster = isCluster();
            boolean otherCluster = other.isCluster();
            if(thisCluster != otherCluster){
                return thisCluster ? 1 : -1;
            }else if(thisCluster) {
                int thisBacked = mBackedItemsCount;
                int otherBacked = other.mBackedItemsCount;
                return thisBacked > otherBacked ? 1 :
                        thisBacked < otherBacked ? -1 : 0;
            }
        }
        return super.a(overlayItem);
    }
}