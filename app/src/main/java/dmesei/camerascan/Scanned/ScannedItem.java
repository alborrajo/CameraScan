package dmesei.camerascan.Scanned;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import dmesei.camerascan.Concept.Concept;

public class ScannedItem implements Parcelable {

    public String path;
    public Concept[] concepts;

    public ScannedItem(String p, Concept[] c) {
        path = p;
        concepts = c;
    }

    public Bitmap getFullBitmap() {
        return BitmapFactory.decodeFile(path);
    }

    public Bitmap getBitmapWithSize(int width, int height) {
        BitmapFactory.Options bfOptions = new BitmapFactory.Options();

        // Get image size
        bfOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bfOptions);

        // Get appropriate sample size
        bfOptions.inSampleSize = calculateInSampleSize(bfOptions, width, height);

        // Get image in sample size
        bfOptions.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, bfOptions);
    }

    // Image utils
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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
