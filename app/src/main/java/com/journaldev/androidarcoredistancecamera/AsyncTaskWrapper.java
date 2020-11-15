package com.journaldev.androidarcoredistancecamera;

import android.os.AsyncTask;

public class AsyncTaskWrapper {

    public static abstract class Task {
        public void before() {
            // available for hire
        }

        public abstract Void during(Void... params);

        public void after() {
            // available for hire
        }

        public void cancelled() {
            // available for hire
        }
    }

    private AsyncTaskWrapper() {
        // restrict instantiation
    }

    public static void run(final Task task) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                task.before();
            }

            @Override
            protected Void doInBackground(Void... params) {
                return task.during();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                task.after();
            }

            @Override
            protected void onCancelled() {
                task.cancelled();
            }
        }.execute();
    }
}
