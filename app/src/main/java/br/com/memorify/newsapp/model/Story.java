package br.com.memorify.newsapp.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Story implements Parcelable {

    public String title;
    public String publicationDate;
    public String thumbnailURL;
    public String websiteURL;
    public String trailText;

    private static final String TITLE_KEY = "webTitle";
    private static final String PUBLICATION_DATE_KEY = "webPublicationDate";
    private static final String THUMBNAIL_URL_KEY = "thumbnail";
    private static final String WEBSITE_URL_KEY = "webUrl";
    private static final String TRAIL_TEXT_KEY = "trailText";
    private static final String EXTRAS_KEY = "fields";

    public Story() {}

    private Story(Parcel in) {
        super();
        title = in.readString();
        publicationDate = in.readString();
        websiteURL = in.readString();
        thumbnailURL = in.readString();
        trailText = in.readString();
    }

    @NonNull
    public static Story fromJSON(@NonNull JSONObject storyJSONObject) {
        Story story = new Story();
        story.title = storyJSONObject.optString(TITLE_KEY);
        story.publicationDate = formatDate(storyJSONObject.optString(PUBLICATION_DATE_KEY));
        story.websiteURL = storyJSONObject.optString(WEBSITE_URL_KEY);
        JSONObject extrasJSONObject = storyJSONObject.optJSONObject(EXTRAS_KEY);
        story.thumbnailURL = extrasJSONObject.optString(THUMBNAIL_URL_KEY);
        story.trailText = extrasJSONObject.optString(TRAIL_TEXT_KEY);

        return story;
    }

    private static String formatDate(String dateUnformatted) {
        if (dateUnformatted == null || dateUnformatted.isEmpty()) {
            return null;
        }

        final SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            Date parse = serverFormat.parse(dateUnformatted);
            return displayFormat.format(parse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(publicationDate);
        dest.writeString(websiteURL);
        dest.writeString(thumbnailURL);
        dest.writeString(trailText);
    }

    public static final Parcelable.Creator<Story> CREATOR = new Parcelable.Creator<Story>() {
        public Story createFromParcel(Parcel in) {
            return new Story(in);
        }

        public Story[] newArray(int size) {
            return new Story[size];
        }
    };
}
