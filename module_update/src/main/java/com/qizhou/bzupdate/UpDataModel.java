package com.qizhou.bzupdate;

import android.os.Parcel;
import android.os.Parcelable;

public class UpDataModel implements Parcelable {
    public String version;
    public String msg;
    public String packagePage;
    public String packageUrl;

    public UpDataModel(){}

    public int getVersionCode(){
        String[] arry = version.split("\\.");
        int len=arry.length;
        int code =0;
        String versionStr ="";
        for (String s :arry){
            versionStr = versionStr+s;
        }
        try {
            code = Integer.parseInt(versionStr);
        }catch (Exception e){
            e.printStackTrace();
        }
        return code;
    }

    protected UpDataModel(Parcel in) {
        version = in.readString();
        msg = in.readString();
        packagePage = in.readString();
        packageUrl = in.readString();
    }

    public static final Creator<UpDataModel> CREATOR = new Creator<UpDataModel>() {
        @Override
        public UpDataModel createFromParcel(Parcel in) {
            return new UpDataModel(in);
        }

        @Override
        public UpDataModel[] newArray(int size) {
            return new UpDataModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(version);
        parcel.writeString(msg);
        parcel.writeString(packagePage);
        parcel.writeString(packageUrl);
    }
}
