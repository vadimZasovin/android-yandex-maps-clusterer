package com.appcraftlab.maps.yandex.clusterer;

import android.graphics.drawable.Drawable;
import android.util.SparseIntArray;

import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;

/**
 * Created by Admin on 28.03.2017.
 */

public class TaggedOverlayItem extends OverlayItem{

    private SparseIntArray mTags;

    public TaggedOverlayItem(GeoPoint geoPoint, Drawable drawable) {
        super(geoPoint, drawable);
    }

    public void setTag(int id, int tag){
        ensureTags();
        mTags.put(id, tag);
    }

    private void ensureTags(){
        if(mTags == null){
            mTags = new SparseIntArray();
        }
    }

    public void setTag(int tag){
        setTag(R.id.default_tag_id, tag);
    }

    public int getTag(int id){
        if(mTags != null){
            return mTags.get(id);
        }
        return 0;
    }

    public int getTag(){
        return getTag(R.id.default_tag_id);
    }
}