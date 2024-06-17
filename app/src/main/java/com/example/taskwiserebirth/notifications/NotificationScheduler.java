package com.example.taskwiserebirth.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.taskwiserebirth.task.TaskModel;
import com.example.taskwiserebirth.utils.CalendarUtils;
import com.google.gson.Gson;

public class NotificationScheduler {

    private static String noDeadline = "No deadline";
    private static String noSchedule = "No schedule";
    private static String none = "None";
    private static int closeToDue = 12;
    private static String TAG_NOTIF = "NOTIFICATION_SCHEDULER";
    public static void scheduleNotification(Context context, TaskModel task) {
        if (task.getRecurrence().equals(none)) {
            if (task.getDeadline().equals(noDeadline) && task.getSchedule().equals(noSchedule)) {
                scheduleNotificationOnce(context, task, true);     // no deadline, sched or recurrence but reminder is turned on
            } else if (!task.getDeadline().equals(noDeadline) && task.getSchedule().equals(noSchedule)) {
                scheduleNotificationOnce(context, task, true);      // for close to due interval
            } else {
                scheduleNotificationOnce(context, task, false);      // no recurrence but a schedule was set by user
            }
        } else {
            scheduleNotificationOnce(context, task, true);
        }
    }

    private static long parseInterval(TaskModel task) {
        if (task.getSchedule().equals(noSchedule) && task.getDeadline().equals(noDeadline)) {
            Log.d("AlarmInterval", "Method: calculateDefaultInterval");
            return CalendarUtils.calculateDefaultInterval();
        } else if (task.getRecurrence().equals("Daily")) {
            Log.d("AlarmInterval", "Method: calculateDailyInterval");
            return CalendarUtils.calculateDailyInterval(task.getSchedule());
        } else if (!task.getRecurrence().equals(none) && !task.getRecurrence().equals("Daily")) {
            Log.d("AlarmInterval", "Method: findNextRecurrence");
            return CalendarUtils.findNextRecurrence(task);
        } else if (task.getSchedule().equals(noSchedule) && !task.getDeadline().equals(noDeadline)) {
            if (!CalendarUtils.isDateAccepted(task.getDeadline())) {
                Log.d("AlarmInterval", "Method: calculatePastDueInterval");
                return CalendarUtils.calculatePastDueInterval();
            } else {
                Log.d("AlarmInterval", "Method: calculateCloseToDueInterval");
                return CalendarUtils.calculateCloseToDueInterval(task);
            }
        } else if (!task.getSchedule().equals(noSchedule) && task.getRecurrence().equals(none)) {
            Log.d("AlarmInterval", "Method: calculateScheduleTime");
            return CalendarUtils.calculateScheduleTime(task.getSchedule());
        }
        Log.d("AlarmInterval", "Method: None, default to 0");
        return 0;
    }

    public static void scheduleNotificationOnce(Context context, TaskModel task, boolean isRepeating) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Gson gson = new Gson();
        String taskJson = gson.toJson(task);

        int notificationCode = task.getId().toHexString().hashCode();
        long interval = parseInterval(task);
        long triggerAtMillis = System.currentTimeMillis() + interval;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("task_json", taskJson);
        intent.putExtra("is_repeating", isRepeating);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (interval != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
            Log.d(TAG_NOTIF, "Alarm set for " + task.getTaskName());
        }
    }

    public static void cancelNotification(Context context, TaskModel task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        int notificationCode = task.getId().toHexString().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationCode, intent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }

        alarmManager.cancel(pendingIntent);
        Log.d(TAG_NOTIF, "Alarm canceled");
    }
}
