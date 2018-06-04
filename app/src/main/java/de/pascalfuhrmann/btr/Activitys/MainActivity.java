package de.pascalfuhrmann.btr.Activitys;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;
import de.pascalfuhrmann.btr.HTML.HTMLParser;
import de.pascalfuhrmann.btr.R;

public class MainActivity extends AppCompatActivity {
    private ListView mListView;
    private HTMLParser parser;
    private EditText mEditText;
    public String htmlContent;
    private StableArrayAdapter adapter;
    private List<String> htmlTableContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        htmlContent = getIntent().getExtras().getString("htmlContent");
        parser = new HTMLParser(htmlContent);
        htmlTableContent = parser.sortByClass("DI71");

        mEditText = findViewById(R.id.search_bar);
        mEditText.setOnEditorActionListener(new DoneOnEditorActionListener());

        adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1,
                htmlTableContent);
        mListView = findViewById(R.id.list_view);
        mListView.setAdapter(adapter);

        SharedPreferences settings = getSharedPreferences(SettingsActivity.SETTINGS, MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("schoolClass", "DI71");
        editor.putBoolean("notifications", true);
        editor.putBoolean("storeUserData", true);
        editor.apply();

        if (settings.getBoolean("notifications", true)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NotificationChannel.DEFAULT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_event_note)
                    .setContentTitle("Test")
                    .setContentText("Test")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            int notificationId = 1;
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(notificationId, mBuilder.build());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
                startActivity(intent);
                return true;
            case R.id.about:
                Snackbar snackbar = Snackbar.make(findViewById(R.id.main_activity),
                                                "made by Pascal Fuhrmann",
                                                    Snackbar.LENGTH_LONG);
                snackbar.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A custom actionlistener to detect if the user hits enter/done at the
     * edittext-view. Uses the information to update the list view accordingly.
     */
    private class DoneOnEditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //close keyboard
                InputMethodManager imm = (InputMethodManager)v.getContext().
                                          getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                /**
                 * if the search bar is empty show default list otherwise use the searchByClass function
                 * to receive the right list.
                 * @see HTMLParser
                 */
                if (!mEditText.getText().toString().isEmpty()) {
                    htmlTableContent = MainActivity.this.parser.
                                       searchSort(mEditText.getText().toString());
                    if(htmlTableContent.get(0).contains("No search")) {
                        Snackbar errorMessage = Snackbar.make(findViewById(R.id.main_activity),
                                                            "No search results found.",
                                                                Snackbar.LENGTH_LONG);
                        errorMessage.show();
                    } else {
                        updateAdapter(htmlTableContent);
                    }
                } else {
                    htmlTableContent = MainActivity.this.parser.getParsedList();
                    updateAdapter(htmlTableContent);
                }
                return true;
            }
            return false;
        }

        /**
         * Set's a new list as the adapters content and updates it.
         * @param html
         */
        private void updateAdapter(List<String> html) {
            adapter = new StableArrayAdapter(MainActivity.this,
                    android.R.layout.simple_list_item_1,
                    htmlTableContent);
            mListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {
        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}

