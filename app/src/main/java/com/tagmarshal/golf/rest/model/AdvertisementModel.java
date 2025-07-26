package com.tagmarshal.golf.rest.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.annotations.SerializedName;
import com.tagmarshal.golf.util.TMUtil;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class AdvertisementModel implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("sections")
    private List<Geometry> sections;

    @SerializedName("mediaNames")
    private List<MediaName> mediaNames;

    @SerializedName("activeTimeDays")
    private List<ActiveTimeDaysItemModel> activeTimeDays;

    @SerializedName("displayTime")
    private int displayTime;

    @SerializedName("bannerInterval")
    private int bannerInterval;

    @SerializedName("type")
    private String type;

    @SerializedName("text")
    private String text;

    @SerializedName("title")
    private String title;

    @SerializedName("sound")
    private boolean sound;

    @SerializedName("canExit")
    private boolean canExit;

    @SerializedName("linkToFoodAndBev")
    private boolean linkToFoodAndBev;

    @SerializedName("from")
    private String from;

    @SerializedName("to")
    private String to;

    private boolean isEnabled;
    private boolean isDownloaded;
    private List<String> downloadsUrls;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdvertisementModel that = (AdvertisementModel) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isValid() {
        if (type == null) return false;

        if (type.equals("text") && (title == null || text == null)) return false;
        if (type.equals("image") && (mediaNames == null || !isDownloaded)) return false;
        if (type.equals("video") && (mediaNames == null || !isDownloaded)) return false;

        return true;
    }

    public void update(AdvertisementModel updatedModel) {
        if (updatedModel.sections != null && (updatedModel.sections.size() != this.sections.size())) {
            this.sections = updatedModel.sections;
        }
        if (updatedModel.mediaNames != null && (updatedModel.mediaNames.size() != this.mediaNames.size()) && (!updatedModel.mediaNames.get(0).url.equals(this.mediaNames.get(0).url))) {
            this.mediaNames = updatedModel.mediaNames;
            this.isDownloaded = updatedModel.isDownloaded;
        }
        if (updatedModel.activeTimeDays != null && (updatedModel.activeTimeDays.size() != this.activeTimeDays.size())) {
            this.activeTimeDays = updatedModel.activeTimeDays;
        }
        this.displayTime = updatedModel.displayTime;
        this.bannerInterval = updatedModel.bannerInterval;
        this.text = updatedModel.text;
        this.title = updatedModel.title;
        this.sound = updatedModel.sound;
        this.canExit = updatedModel.canExit;
        this.linkToFoodAndBev = updatedModel.linkToFoodAndBev;
        this.from = updatedModel.from;
        this.to = updatedModel.to;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isActive() {
        return TMUtil.activeTimeDaysPass(getActiveTimeDays());
    }

    public boolean isExpired() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            if (from == null || to == null) {
                return false;
            } else {
                Date fromDate = dateFormat.parse(from);
                Date toDate = dateFormat.parse(to);
                Date currentDate = new Date();
                return currentDate.before(fromDate) || currentDate.after(toDate);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
    }

    public String getId() {
        return id;
    }

    public List<Geometry> getSections() {
        return sections;
    }

    public List<MediaName> getMediaNames() {
        return mediaNames;
    }

    public List<ActiveTimeDaysItemModel> getActiveTimeDays() {
        return activeTimeDays;
    }

    public int getDisplayTime() {
        return displayTime;
    }

    public int getBannerInterval() {
        return bannerInterval;
    }

    public String getType() {
        return type;
    }

    public boolean isVideo() {
        return type.equalsIgnoreCase("video");
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public boolean isSound() {
        return sound;
    }

    public boolean isCanExit() {
        return canExit;
    }

    public boolean isLinkToFoodAndBev() {
        return linkToFoodAndBev;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public List<String> getDownloadsUrls() {
        return downloadsUrls;
    }

    public void setDownloadsUrls(List<String> downloadsUrls) {
        this.downloadsUrls = downloadsUrls;
    }

    public static class Geometry implements Serializable {

        @SerializedName("type")
        private String type;

        @SerializedName("coordinates")
        private List<List<Double>> coordinates;

        public String getType() {
            return type;
        }

        public List<List<Double>> getCoordinates() {
            return coordinates;
        }

        public ArrayList<LatLng> getLatLngCoordinates() {
            ArrayList<LatLng> latLngs = new ArrayList<>();
            for (int i = 0; i < getCoordinates().size(); i++)
                latLngs.add(new LatLng(getCoordinates().get(i).get(0), getCoordinates().get(i).get(1)));

            return latLngs;
        }

        public PolygonOptions getPolygons(int color, int backgroundColor) {
            return new PolygonOptions().addAll(getLatLngCoordinates()).clickable(false).fillColor(backgroundColor).strokeColor(color);
        }

        public PolygonOptions getPolygons() {
            return new PolygonOptions().addAll(getLatLngCoordinates()).clickable(false).visible(false);
        }
    }

    public static class MediaName implements Serializable {
        @SerializedName("name")
        private String name;

        @SerializedName("url")
        private String url;

        @SerializedName("_id")
        private String id;

        private boolean isDownloaded;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getID() {
            return id;
        }

        public boolean isDownloaded() {
            return isDownloaded;
        }

        public void setDownloaded(boolean downloaded) {
            isDownloaded = downloaded;
        }
    }
}
