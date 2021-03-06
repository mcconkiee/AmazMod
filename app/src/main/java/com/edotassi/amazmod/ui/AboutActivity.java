package com.edotassi.amazmod.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.edotassi.amazmod.BuildConfig;

import amazmod.com.transport.Constants;

import com.edotassi.amazmod.R;
import com.edotassi.amazmod.support.FirebaseEvents;
import com.edotassi.amazmod.watch.Watch;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huami.watch.notification.data.StatusBarNotificationData;
import com.huami.watch.transport.DataBundle;
import com.huami.watch.transport.DataTransportResult;
import com.huami.watch.transport.Transporter;
import com.huami.watch.transport.TransporterClassic;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;
import com.pixplicity.easyprefs.library.Prefs;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import amazmod.com.models.Reply;
import amazmod.com.transport.data.NotificationData;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.mateware.snacky.Snacky;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.activity_about_version)
    TextView version;

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException exception) {
            System.out.println("AmazMod AboutActivity onCreate exception: " + exception.toString());
            //TODO log to crashlitics
        }
        getSupportActionBar().setTitle(R.string.about);

        ButterKnife.bind(this);

        version.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case (R.id.action_activity_about_custom_ui_test): {
                sendTestMessage('C');
                break;
            }
            case (R.id.action_activity_about_standard_test): {
                sendTestMessage('S');
                break;
            }
            case (R.id.action_activity_about_notification_test): {
                sendTestMessage('N');
                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void sendTestMessage(char type) {
        NotificationData notificationData = new NotificationData();
        final String snackTextOK, snackTextFailure;

        switch (type) {
            case ('C'): {
                notificationData.setForceCustom(true);
                notificationData.setText("Test Notification");
                break;
            }
            case ('S'): {
                notificationData.setForceCustom(false);
                notificationData.setText("Test Notification");
                break;
            }
            case ('N'): {
                notificationData.setForceCustom(false);
                notificationData.setText("Test Notification");
                sendNotificationWithStandardUI(notificationData);
                return;
            }
            default:
                System.out.println("AmazMod AboutActivity sendTestMessage: something went wrong...");
        }

        snackTextOK = getResources().getString(R.string.test_notification_sent);
        snackTextFailure = getResources().getString(R.string.failed_to_send_test_notification);

        notificationData.setId(999);
        notificationData.setKey("amazmod|test|999");
        notificationData.setTitle("AmazMod");
        notificationData.setTime("00:00");
        notificationData.setVibration(Integer.valueOf(Prefs.getString(Constants.PREF_NOTIFICATIONS_VIBRATION, Constants.PREF_DEFAULT_NOTIFICATIONS_VIBRATION)));
        notificationData.setHideReplies(Prefs.getBoolean(Constants.PREF_DISABLE_NOTIFICATIONS_REPLIES, Constants.PREF_DEFAULT_DISABLE_NOTIFICATIONS_REPLIES));
        notificationData.setHideButtons(false);

        try {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher_round);
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] intArray = new int[width * height];
            bitmap.getPixels(intArray, 0, width, 0, 0, width, height);

            notificationData.setIcon(intArray);
            notificationData.setIconWidth(width);
            notificationData.setIconHeight(height);
        } catch (Exception e) {
            notificationData.setIcon(new int[]{});
            System.out.println("AmazMod AboutActivity notificationData Failed to get bitmap " + e.toString());
        }

        Watch.get().postNotification(notificationData).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(@NonNull Task<Void> task) throws Exception {
                if (task.isSuccessful()) {
                    Snacky.builder()
                            .setActivity(AboutActivity.this)
                            .setText(snackTextOK)
                            .setDuration(Snacky.LENGTH_SHORT)
                            .build().show();
                } else {
                    Snacky.builder()
                            .setActivity(AboutActivity.this)
                            .setText(snackTextFailure)
                            .setDuration(Snacky.LENGTH_SHORT)
                            .build().show();
                }
                return null;
            }
        });
    }

    private void sendNotificationWithStandardUI(NotificationData nd) {
        DataBundle dataBundle = new DataBundle();
        Intent intent = new Intent();
        int nextId = (int) (long) (System.currentTimeMillis() % 10000L);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Bitmap pic = BitmapFactory.decodeResource(getResources(), R.drawable.art_canteen_intro1);

        Intent buttonIntent = new Intent(getBaseContext(), AboutActivity.class);
        buttonIntent.putExtra("notificationId", nextId);
        PendingIntent dismissIntent = PendingIntent.getBroadcast(getBaseContext(), 0, buttonIntent, 0);

        NotificationCompat.Action action2 =
                new NotificationCompat.Action.Builder(android.R.drawable.ic_delete, "Dismiss", dismissIntent)
                        .build();

        Bundle bundle = new Bundle();
        bundle.putParcelable(NotificationCompat.EXTRA_LARGE_ICON_BIG, pic);
        bundle.putParcelable(NotificationCompat.EXTRA_LARGE_ICON, pic);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "")
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addExtras(bundle)
                .addAction(android.R.drawable.ic_delete, "DISMISS", dismissIntent)
                .setContentIntent(pendingIntent)
                .setContentText(nd.getText())
                .setContentTitle("Test");

        final String INTENT_ACTION_REPLY = "com.amazmod.action.reply";
        final String EXTRA_REPLY = "extra.reply";
        final String EXTRA_NOTIFICATION_KEY = "extra.notification.key";
        final String EXTRA_NOTIFICATION_ID = "extra.notification.id";


        List<Reply> repliesList = loadReplies();

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setContentIcon(R.drawable.ic_launcher_round)
                .setContentIntentAvailableOffline(true)
                .addAction(action2)
                .setBackground(pic);
        for (Reply reply : repliesList) {
            intent.setPackage("com.amazmod.service");
            intent.setAction(INTENT_ACTION_REPLY);
            intent.putExtra(EXTRA_REPLY, reply.getValue());
            intent.putExtra(EXTRA_NOTIFICATION_KEY, "0|com.edotassi.amazmod|" + String.valueOf(nextId + 1) + "|tag|0");
            intent.putExtra(EXTRA_NOTIFICATION_ID, nextId + 1);
            PendingIntent replyIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(android.R.drawable.ic_input_add, reply.getValue(), replyIntent).build();
            wearableExtender.addAction(replyAction);
        }

        builder.extend(wearableExtender);
        Notification notification = builder.build();
/*
        notification.extras.getBundle("android.wearable.EXTENSIONS");

        if (notification.extras == null) {
            Bundle bundle = new Bundle();
            notification.extras.putBundle("android.car.EXTENSIONS", bundle);
        }
*/
        StatusBarNotification sbn = new StatusBarNotification("com.edotassi.amazmod", "",
                nextId + 1, "tag", 0, 0, 0,
                notification, android.os.Process.myUserHandle(),
                System.currentTimeMillis());

        StatusBarNotificationData sbnd = StatusBarNotificationData.from(this, sbn, false);

        dataBundle.putParcelable("data", sbnd);
/*
        StatusBarNotificationData sbnd2 = dataBundle.getParcelable("notification");
        System.out.println("AmazMod AboutActivity StandardUI sbnd: " + sbnd);

        //NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        //notificationManagerCompat.notify(123456, sbnd2.notification.originalNotification);

        Bundle bundle = new Bundle();
        bundle.putParcelable("notification", sbn);
*/

        //Connect transporter
        Transporter notificationTransporter = TransporterClassic.get(this, "com.huami.action.notification");
        notificationTransporter.connectTransportService();

        notificationTransporter.send("add", dataBundle, new Transporter.DataSendResultCallback() {
            @Override
            public void onResultBack(DataTransportResult dataTransportResult) {
                System.out.println("AmazMod AboutActivity dataTransportResult: " + dataTransportResult.toString());
                switch (dataTransportResult.getResultCode()) {
                    case (DataTransportResult.RESULT_FAILED_TRANSPORT_SERVICE_UNCONNECTED):
                    case (DataTransportResult.RESULT_FAILED_CHANNEL_UNAVAILABLE):
                    case (DataTransportResult.RESULT_FAILED_IWDS_CRASH):
                    case (DataTransportResult.RESULT_FAILED_LINK_DISCONNECTED): {
                        Snacky.builder()
                                .setActivity(AboutActivity.this)
                                .setText(R.string.failed_to_send_test_notification)
                                .setDuration(Snacky.LENGTH_SHORT)
                                .build().show();
                        break;
                    }
                    case (DataTransportResult.RESULT_OK): {
                        Snacky.builder()
                                .setActivity(AboutActivity.this)
                                .setText(R.string.test_notification_sent)
                                .setDuration(Snacky.LENGTH_SHORT)
                                .build().show();
                    }
                    break;
                }
            }
        });

        //Disconnect transporter to avoid leaking
        notificationTransporter.disconnectTransportService();

    }

    private List<Reply> loadReplies() {
        final String replies = Prefs.getString(Constants.PREF_NOTIFICATIONS_REPLIES, "[]");

        try {
            Type listType = new TypeToken<List<Reply>>() {
            }.getType();
            return new Gson().fromJson(replies, listType);
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
}
