package com.gark.vk.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Gark on 21.07.13.
 */
public class VideoTypes implements Parcelable {




    private String vkVideo240;
    private String vkVideo360;
    private String vkVideo480;
    private String vkVideo720;
    private String yTubeVideo;
    private String vimeoVideo;

    public VideoTypes() {

    }

    protected VideoTypes(Parcel in) {
        vkVideo240 = in.readString();
        vkVideo360 = in.readString();
        vkVideo480 = in.readString();
        vkVideo720 = in.readString();
        yTubeVideo = in.readString();
        vimeoVideo = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vkVideo240);
        dest.writeString(vkVideo360);
        dest.writeString(vkVideo480);
        dest.writeString(vkVideo720);
        dest.writeString(yTubeVideo);
        dest.writeString(vimeoVideo);
    }

    public static final Parcelable.Creator<VideoTypes> CREATOR = new Parcelable.Creator<VideoTypes>() {
        public VideoTypes createFromParcel(Parcel in) {
            return new VideoTypes(in);
        }

        public VideoTypes[] newArray(int size) {
            return new VideoTypes[size];
        }
    };

    public void setVkVideo240(String vkVideo240) {
        this.vkVideo240 = vkVideo240;
    }

    public void setVkVideo360(String vkVideo360) {
        this.vkVideo360 = vkVideo360;
    }

    public void setVkVideo480(String vkVideo480) {
        this.vkVideo480 = vkVideo480;
    }

    public void setVkVideo720(String vkVideo720) {
        this.vkVideo720 = vkVideo720;
    }

    public void setyTubeVideo(String yTubeVideo) {
        this.yTubeVideo = yTubeVideo;
    }

    public String getVkVideo240() {
        return vkVideo240;
    }

    public String getVkVideo360() {
        return vkVideo360;
    }

    public String getVkVideo480() {
        return vkVideo480;
    }

    public String getVkVideo720() {
        return vkVideo720;
    }

    public String getyTubeVideo() {
        return yTubeVideo;
    }

    public String getVimeoVideo() {
        return vimeoVideo;
    }
}
