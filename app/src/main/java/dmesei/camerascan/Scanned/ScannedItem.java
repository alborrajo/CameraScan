package dmesei.camerascan.Scanned;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import dmesei.camerascan.Concept.Concept;

public class ScannedItem implements Parcelable {

    public Bitmap bitmap;
    public Concept[] concepts;

    public ScannedItem(Bitmap b, Concept[] c) {
        bitmap = b;
        concepts = c;
    }

    protected ScannedItem(Parcel in) {
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
        concepts = in.createTypedArray(Concept.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bitmap, flags);
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
