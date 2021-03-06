package amazmod.com.transport.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.huami.watch.transport.DataBundle;

import amazmod.com.transport.Transportable;

public class NotificationData extends Transportable implements Parcelable {

    public static final String EXTRA = "notificationSpec";

    private final String DATA_KEY = "key";
    private final String DATA_ID = "id";
    private final String DATA_TITLE = "title";
    private final String DATA_TIME = "time";
    private final String DATA_TEXT = "text";
    private final String DATA_ICON = "icon";
    private final String DATA_ICON_WIDTH = "iconWidth";
    private final String DATA_ICON_HEIGHT = "iconHeight";
    private final String DATA_VIBRATION = "vibration";
    private final String DATA_FORCE_CUSTOM = "forceCustom";
    private final String DATA_HIDE_REPLIES = "hideReplies";
    private final String DATA_HIDE_BUTTONS = "hideButtons";


    private String key;
    private int id;
    private String title;
    private String time;
    private String text;
    private int[] icon;
    private int iconWidth;
    private int iconHeight;
    private int vibration;
    private boolean isDeviceLocked;
    private int timeoutRelock;
    private boolean forceCustom;
    private boolean hideReplies;
    private boolean hideButtons;

    public NotificationData() {
    }

    protected NotificationData(Parcel in) {
        key = in.readString();
        id = in.readInt();
        title = in.readString();
        time = in.readString();
        text = in.readString();
        icon = in.createIntArray();
        iconWidth = in.readInt();
        iconHeight = in.readInt();
        vibration = in.readInt();
        isDeviceLocked = in.readByte() != 0;
        timeoutRelock = in.readInt();
        forceCustom = in.readByte() != 0;
        hideReplies = in.readByte() != 0;
        hideButtons = in.readByte() != 0;
    }

    public static final Parcelable.Creator<NotificationData> CREATOR = new Parcelable.Creator<NotificationData>() {
        @Override
        public NotificationData createFromParcel(Parcel in) {
            return new NotificationData(in);
        }

        @Override
        public NotificationData[] newArray(int size) {
            return new NotificationData[size];
        }
    };

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int[] getIcon() {
        return icon;
    }

    public void setIcon(int[] icon) {
        this.icon = icon;
    }

    public boolean getForceCustom() {
        return forceCustom;
    }

    public void setForceCustom(boolean forceCustom) {
        this.forceCustom = forceCustom;
    }

    public boolean getHideReplies() {
        return hideReplies;
    }

    public void setHideReplies(boolean hideReplies) {
        this.hideReplies = hideReplies;
    }

    public boolean getHideButtons() {
        return hideButtons;
    }

    public void setHideButtons(boolean hideButtons) {
        this.hideButtons = hideButtons;
    }

    public boolean isDeviceLocked() {
        return isDeviceLocked;
    }

    public void setDeviceLocked(boolean deviceLocked) {
        isDeviceLocked = deviceLocked;
    }

    public int getVibration() {
        return vibration;
    }

    public void setVibration(int vibration) {
        this.vibration = vibration;
    }

    public int getTimeoutRelock() {
        return timeoutRelock;
    }

    public void setTimeoutRelock(int timeoutRelock) {
        this.timeoutRelock = timeoutRelock;
    }

    public int getIconWidth() {
        return iconWidth;
    }

    public void setIconWidth(int iconWidth) {
        this.iconWidth = iconWidth;
    }

    public int getIconHeight() {
        return iconHeight;
    }

    public void setIconHeight(int iconHeight) {
        this.iconHeight = iconHeight;
    }

    public void setTime (String time) { this.time = time; }

    public String getTime () { return time; }

    @Override
    public DataBundle toDataBundle(DataBundle dataBundle) {
        dataBundle.putString(DATA_KEY, key);
        dataBundle.putInt(DATA_ID, id);
        dataBundle.putString(DATA_TITLE, title);
        dataBundle.putString(DATA_TIME, time);
        dataBundle.putString(DATA_TEXT, text);
        dataBundle.putIntArray(DATA_ICON, icon);
        dataBundle.putInt(DATA_ICON_WIDTH, iconWidth);
        dataBundle.putInt(DATA_ICON_HEIGHT, iconHeight);
        dataBundle.putInt(DATA_VIBRATION, vibration);
        dataBundle.putBoolean(DATA_FORCE_CUSTOM, forceCustom);
        dataBundle.putBoolean(DATA_HIDE_REPLIES, hideReplies);
        dataBundle.putBoolean(DATA_HIDE_BUTTONS, hideButtons);

        return dataBundle;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA, this);
        return bundle;
    }

    public static NotificationData fromBundle(Bundle bundle) {
        return bundle.getParcelable(EXTRA);
    }

    public static NotificationData fromDataBundle(DataBundle dataBundle) {
        NotificationData notificationData = new NotificationData();

        String title = dataBundle.getString("title");
        String time = dataBundle.getString("time");
        String text = dataBundle.getString("text");
        int id = dataBundle.getInt("id");
        String key = dataBundle.getString("key");
        int[] icon = dataBundle.getIntArray("icon");
        int iconWidth = dataBundle.getInt("iconWidth");
        int iconHeight = dataBundle.getInt("iconHeight");
        int vibration = dataBundle.getInt("vibration");
        boolean forceCustom = dataBundle.getBoolean("forceCustom");
        boolean hideReplies = dataBundle.getBoolean("hideReplies");
        boolean hideButtons = dataBundle.getBoolean("hideButtons");

        notificationData.setTitle(title);
        notificationData.setTime(time);
        notificationData.setText(text);
        notificationData.setIcon(icon);
        notificationData.setIconWidth(iconWidth);
        notificationData.setIconHeight(iconHeight);
        notificationData.setVibration(vibration);
        notificationData.setId(id);
        notificationData.setKey(key);
        notificationData.setForceCustom(forceCustom);
        notificationData.setHideReplies(hideReplies);
        notificationData.setHideButtons(hideButtons);

        return notificationData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(time);
        dest.writeString(text);
        dest.writeIntArray(icon);
        dest.writeInt(iconWidth);
        dest.writeInt(iconHeight);
        dest.writeInt(vibration);
        dest.writeByte((byte) (isDeviceLocked ? 1 : 0));
        dest.writeInt(timeoutRelock);
        dest.writeByte((byte) (forceCustom ? 1 : 0));
        dest.writeByte((byte) (hideReplies ? 1 : 0));
        dest.writeByte((byte) (hideButtons ? 1 : 0));
    }

}
