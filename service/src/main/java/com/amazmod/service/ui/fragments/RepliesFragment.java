package com.amazmod.service.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.text.emoji.widget.EmojiButton;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazmod.service.Constants;
import com.amazmod.service.R;
import com.amazmod.service.events.ReplyNotificationEvent;
import com.amazmod.service.settings.SettingsManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import amazmod.com.models.Reply;
import amazmod.com.transport.data.NotificationData;
import xiaofei.library.hermeseventbus.HermesEventBus;

public class RepliesFragment extends Fragment implements DelayedConfirmationView.DelayedConfirmationListener {

    LinearLayout repliesContainer;
    BoxInsetLayout rootLayout;
    NotificationData notificationSpec;
    private DelayedConfirmationView delayedConfirmationView;

    private TextView textView;

    private float fontSizeSP;
    private String defaultLocale, selectedReply;
    private boolean enableInvertedTheme;
    private Context mContext;
    private SettingsManager settingsManager;

    private static final float FONT_SIZE_NORMAL = 14.0f;
    private static final float FONT_SIZE_LARGE = 18.0f;
    private static final float FONT_SIZE_HUGE = 22.0f;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity.getBaseContext();
        Log.i(Constants.TAG,"RepliesFragment onAttach context: " + mContext);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationSpec = NotificationData.fromBundle(getArguments());

        Log.d(Constants.TAG,"RepliesFragment onCreate " + notificationSpec);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(Constants.TAG,"RepliesFragment onCreateView");

        return inflater.inflate(R.layout.fragment_replies, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(Constants.TAG,"RepliesFragment onViewCreated");

        updateContent();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void updateContent(){
        mContext = getActivity();

        Log.d(Constants.TAG,"RepliesFragment updateContent " + notificationSpec);

        settingsManager = new SettingsManager(mContext);

        repliesContainer = getActivity().findViewById(R.id.fragment_replies_replies_container);
        rootLayout = getActivity().findViewById(R.id.fragment_replies_root_layout);
        textView = getActivity().findViewById(R.id.fragment_replies_textview);
        delayedConfirmationView = getActivity().findViewById(R.id.delayedView);
        delayedConfirmationView.setTotalTimeMs(3000);

        //Load preferences
        enableInvertedTheme = settingsManager.getBoolean(Constants.PREF_NOTIFICATIONS_INVERTED_THEME,
                Constants.PREF_DEFAULT_NOTIFICATIONS_INVERTED_THEME);
        defaultLocale = settingsManager.getString(Constants.PREF_DEFAULT_LOCALE, "");
        Log.i(Constants.TAG, "RepliesFragment defaultLocale: " + defaultLocale);


        // Set theme and font size
        //Log.d(Constants.TAG, "NotificationActivity enableInvertedTheme: " + enableInvertedTheme + " / fontSize: " + fontSize);
        if (enableInvertedTheme) {
            rootLayout.setBackgroundColor(getResources().getColor(R.color.white));
            textView.setTextColor(getResources().getColor(R.color.black));
        }

        delayedConfirmationView.setVisibility(View.GONE);
        setFontSizeSP();
        addReplies();

    }


    private void setFontSizeSP(){
        String fontSize = settingsManager.getString(Constants.PREF_NOTIFICATIONS_FONT_SIZE,
                Constants.PREF_DEFAULT_NOTIFICATIONS_FONT_SIZE);
        switch (fontSize) {
            case "l":
                fontSizeSP = FONT_SIZE_LARGE;
                break;
            case "h":
                fontSizeSP = FONT_SIZE_HUGE;
                break;
            default:
                fontSizeSP = FONT_SIZE_NORMAL;
        }
    }

    private void setFontLocale(TextView tv, String locale) {
        Log.i(Constants.TAG, "RepliesFragment setFontLocale TextView: " + locale);
        if (locale.contains("iw")) {
            Typeface face = Typeface.createFromAsset(mContext.getAssets(),"fonts/DroidSansFallback.ttf");
            tv.setTypeface(face);
        }
    }

    private void setFontLocale(Button b, String locale) {
        Log.i(Constants.TAG, "RepliesFragment setFontLocale Button: " + locale);
        if (locale.contains("iw")) {
            Typeface face = Typeface.createFromAsset(mContext.getAssets(),"fonts/DroidSansFallback.ttf");
            b.setTypeface(face);
        }
    }

    private void addReplies() {
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        param.setMargins(20,12,20,12);

        List<Reply> repliesList = loadReplies();
        for (final Reply reply : repliesList) {
            EmojiButton button = new EmojiButton(mContext);
            button.setLayoutParams(param);
            button.setPadding(0,10,0,10);
            button.setIncludeFontPadding(false);
            button.setMinHeight(24);
            setFontLocale(button, defaultLocale);
            button.setText(reply.getValue());
            button.setAllCaps(false);
            button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSizeSP);
            setButtonTheme(button, enableInvertedTheme ? Constants.BLUE : Constants.GREY);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedReply = reply.getValue();
                    sendReply(v);
                }
            });
            repliesContainer.addView(button);
        }

    }

    private List<Reply> loadReplies() {
        final String replies = settingsManager.getString(Constants.PREF_NOTIFICATION_CUSTOM_REPLIES, "[]");

        try {
            Type listType = new TypeToken<List<Reply>>() {
            }.getType();
            return new Gson().fromJson(replies, listType);
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private void setButtonTheme(Button button, String color){
        switch (color) {
            case ("red"): {
                button.setTextColor(Color.parseColor("#ffffff"));
                button.setBackground(mContext.getDrawable(R.drawable.close_red));
                break;
            }
            case ("blue"): {
                button.setTextColor(Color.parseColor("#ffffff"));
                button.setBackground(mContext.getDrawable(R.drawable.reply_blue));
                break;
            }
            case ("grey"): {
                button.setTextColor(Color.parseColor("#000000"));
                button.setBackground(mContext.getDrawable(R.drawable.reply_grey));
                break;
            }
            default: {
                button.setTextColor(Color.parseColor("#000000"));
                button.setBackground(mContext.getDrawable(R.drawable.reply_grey));
            }
        }
    }

    private void sendReply(View v) {
        repliesContainer.setVisibility(View.GONE);
        delayedConfirmationView.setVisibility(View.VISIBLE);
        textView.setText("Sending in 3s…");
        delayedConfirmationView.setPressed(false);
        delayedConfirmationView.start();
        delayedConfirmationView.setListener(this);
        Log.i(Constants.TAG, "RepliesFragment sendReply isPressed: " + delayedConfirmationView.isPressed());
    }

    @Override
    public void onTimerSelected(View v) {
        v.setPressed(true);
        delayedConfirmationView.reset();

        // Prevent onTimerFinished from being heard.
        ((DelayedConfirmationView) v).setListener(null);
        delayedConfirmationView.setVisibility(View.GONE);
        repliesContainer.setVisibility(View.VISIBLE);
        textView.setText(getActivity().getResources().getString(R.string.reply));
        Log.i(Constants.TAG, "RepliesFragment onTimerSelected isPressed: " + v.isPressed());
    }

    @Override
    public void onTimerFinished(View v) {
        Log.i(Constants.TAG, "RepliesFragment onTimerFinished isPressed: " + v.isPressed());

        ((DelayedConfirmationView) v).setListener(null);

        Intent intent = new Intent(mContext, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Reply Sent!");
        startActivity(intent);

        HermesEventBus.getDefault().post(new ReplyNotificationEvent(notificationSpec.getKey(), selectedReply));
        getActivity().finish();

    }

    public static RepliesFragment newInstance(Bundle b) {

        Log.i(Constants.TAG,"RepliesFragment newInstance");
        RepliesFragment myFragment = new RepliesFragment();
        myFragment.setArguments(b);

        return myFragment;
    }

}
