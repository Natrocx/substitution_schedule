package de.pascalfuhrmann.btr.activitys;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import de.pascalfuhrmann.btr.html.HTMLParser;
import de.pascalfuhrmann.btr.R;
import de.pascalfuhrmann.btr.broadcast_receivers.JobSchedulerService;

public class MainActivity extends AppCompatActivity {
    public String htmlContent;
    private ListView mListView;
    private HTMLParser parser;
    private EditText mEditText;
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

        if (settings.getBoolean("notifications", true)) {
            createNotificationService(this);
        }
    }

    private void createNotificationService(Context context) {
        ComponentName componentName = new ComponentName(getApplicationContext(), JobSchedulerService.class);
        JobScheduler mJobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, componentName)
                .setPeriodic(15000) //triggers the job all 15s
                .setPersisted(true) //recreates the job after it is done
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); //requires non-cellular network
        if (mJobScheduler != null)
            mJobScheduler.schedule(builder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
                startActivity(intent);
                return true;
            case R.id.about:
                Toast toast = Toast.makeText(this,
                        "made by Pascal in coop. with Jonas",
                        Toast.LENGTH_LONG);
                toast.show();
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
                InputMethodManager imm = (InputMethodManager) v.getContext().
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
                    if (htmlTableContent.get(0).contains("No search")) {
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
         *
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

