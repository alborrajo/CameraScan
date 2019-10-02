package dmesei.camerascan.Scanned;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import dmesei.camerascan.Concept.Concept;

public class ScannedItem implements Parcelable {

    public static Bitmap fallbackBitmap;

    public String path;
    public Concept[] concepts;

    public ScannedItem(String p, Concept[] c) {
        path = p;
        concepts = c;
    }

    private Bitmap bitmap;
    public Bitmap getBitmap() {
        if(bitmap != null) {return bitmap;}

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if(bitmap == null) {
            return fallbackBitmap;
        }

        return bitmap;
    }

    private Bitmap bitmapThumbnail;
    public Bitmap getBitmapThumbnail() {
        if(bitmapThumbnail != null) {return bitmapThumbnail;}

        Bitmap tmpBitmap = BitmapFactory.decodeFile(path);
        if(tmpBitmap == null) {
            tmpBitmap = fallbackBitmap;
        }

        //Generate thumbnail from bitmap
        return bitmapThumbnail = Bitmap.createScaledBitmap(tmpBitmap, 64, 64, true);
    }

    // PARCELABLE
    protected ScannedItem(Parcel in) {
        this(
            in.readString(), //Path
            in.createTypedArray(Concept.CREATOR) //Concepts
        );
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeTypedArray(concepts, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ScannedItem> CREATOR = new Creator<ScannedItem>() {
        @Override
        public ScannedItem createFromParcel(Parcel in) {
            return new ScannedItem(in);
        }

        @Override
        public ScannedItem[] newArray(int size) {
            return new ScannedItem[size];
        }
    };

}
