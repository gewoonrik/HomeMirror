package com.morristaedt.mirror.modules;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by alex on 12/09/15.
 */
public class CalendarModule {
    public interface CalendarListener {
        void onCalendarUpdate(List<CalendarItem> calendarItems);
    }

    public static class CalendarItem
    {
        public final String title;
        public final String details;

        public CalendarItem(String title, String details) {
            this.title = title;
            this.details = details;
        }
    }

    public static void getCalendarEvents(final Context context, final CalendarListener calendarListener, final int nrOfItems) {
        new AsyncTask<Void, Void, Void>() {
            List<CalendarItem> items = new ArrayList<CalendarItem>();

            @Override
            protected void onPostExecute(Void aVoid) {

                calendarListener.onCalendarUpdate(items);
            }

            @Override
            protected Void doInBackground(Void... params) {
                Cursor cursor = null;
                ContentResolver contentResolver = context.getContentResolver();
                final String[] colsToQuery = new String[]{
                        CalendarContract.EventsEntity.TITLE,
                        CalendarContract.EventsEntity.DTSTART,
                        CalendarContract.EventsEntity.DTEND,
                        CalendarContract.EventsEntity.EVENT_LOCATION};

                Calendar now = Calendar.getInstance();
                SimpleDateFormat startFormat = new SimpleDateFormat("dd/MM/yy");
                String dateString = startFormat.format(now.getTime());
                long start = now.getTimeInMillis();

                SimpleDateFormat endFormat = new SimpleDateFormat("hh:mm:ss dd/MM/yy");
                Calendar endOfDay = Calendar.getInstance();
                Date endofDayDate = null;
                try {
                    endofDayDate = endFormat.parse("23:59:59 " + dateString);
                    endOfDay.setTime(endofDayDate);
                } catch (ParseException e) {
                    Log.e("CalendarModule", e.toString());
                }


                cursor = contentResolver.query(CalendarContract.Events.CONTENT_URI, colsToQuery,
                        "( dtstart >" + start + ") and (dtend  <" + endOfDay.getTimeInMillis() + ")",
                        null, "dtstart ASC");
                cursor.moveToFirst();
                int i = 0;
                while(i < nrOfItems && !cursor.isAfterLast())   {
                    String title = cursor.getString(0);
                    Calendar startTime = Calendar.getInstance();
                    startTime.setTimeInMillis(cursor.getLong(1));
                    Calendar endTime = Calendar.getInstance();
                    endTime.setTimeInMillis(cursor.getLong(2));
                    DateFormat formatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
                    String details = formatter.format(startTime.getTime()) + " - " + formatter.format(endTime.getTime());
                    if (!cursor.getString(3).equals("")) {
                        details += " ~ " + cursor.getString(3);
                    }
                    items.add(new CalendarItem(title, details));
                }

                cursor.close();
                return null;
            }
        }.execute();
    }
}
