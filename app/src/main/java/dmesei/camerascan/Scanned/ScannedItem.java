package dmesei.camerascan.Scanned;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import dmesei.camerascan.Concept.Concept;

public class ScannedItem implements Parcelable {

    public static Bitmap fallbackBitmap;

    public Bitmap bitmap;
    public String path;
    public Concept[] concepts;

    public ScannedItem(String p, Concept[] c) {
        path = p;
        concepts = c;
        bitmap = BitmapFactory.decodeFile(path);
        if(bitmap == null) {bitmap = fallbackBitmap;} //Fallback
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
