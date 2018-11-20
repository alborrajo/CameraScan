package dmesei.camerascan.Concept;

import android.os.Parcel;
import android.os.Parcelable;

public class Concept implements Parcelable {
    public String name;
    public double percentage;

    public Concept(String n, double p) {
        name = n;
        percentage = p;
    }

    protected Concept(Parcel in) {
        name = in.readString();
        percentage = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(percentage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Concept> CREATOR = new Creator<Concept>() {
        @Override
        public Concept createFromParcel(Parcel in) {
            return new Concept(in);
        }

        @Override
        public Concept[] newArray(int size) {
            return new Concept[size];
        }
    };
}
