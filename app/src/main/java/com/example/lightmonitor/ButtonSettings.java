package com.example.lightmonitor;

import android.os.Parcel;
import android.os.Parcelable;

public class ButtonSettings implements Parcelable {
    boolean twoColors;
    String type;
    ButtonSettings(String t, boolean c){
        twoColors = c;
        type = t;
    }

    private ButtonSettings(Parcel in) {
        twoColors = in.readByte() != 0;
        type = in.readString();
    }

    public static final Creator<ButtonSettings> CREATOR = new Creator<ButtonSettings>() {
        @Override
        public ButtonSettings createFromParcel(Parcel in) {
            return new ButtonSettings(in);
        }

        @Override
        public ButtonSettings[] newArray(int size) {
            return new ButtonSettings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (twoColors ? 1 : 0));
        parcel.writeString(type);
    }
}
