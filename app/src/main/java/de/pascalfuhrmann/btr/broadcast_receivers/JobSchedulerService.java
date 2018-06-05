package de.pascalfuhrmann.btr.broadcast_receivers;

import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import de.pascalfuhrmann.btr.MainActivity;
import de.pascalfuhrmann.btr.R;

public class NotificationService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        mJobHandler.sendMessage(Message.obtain(mJobHandler, 1, params));
        //returning true to tell the system that there is still work going on.
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mJobHandler.removeMessages(1);
        return false;
    }

    private Handler mJobHandler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg) {
            Intent intent = new Intent(NotificationService.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(NotificationService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NotificationService.this, "BTR")
                    .setSmallIcon(R.drawable.ic_event_note)
                    .setContentTitle("Test title")
                    .setContentText("This is a test notification.")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(NotificationService.this);
            notificationManager.notify(0, mBuilder.build());

            jobFinished((JobParameters) msg.obj, true );
            return true;
        }

    } );
}