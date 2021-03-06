/* Copyright 2018 Pascal Fuhrmann, Jonas Lauschke

    This file is part of substitution_schedule.

    substitution_schedule is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    substitution_schedule is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with substitution_schedule.  If not, see <http://www.gnu.org/licenses/>.
  */
package de.pascalfuhrmann.btr.activitys;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import de.pascalfuhrmann.btr.R;

public class SettingsActivity extends AppCompatActivity {
    public static final String SETTINGS = "Settings";
    private EditText mEditText;
    private Button mButton;
    private CheckBox mCheckNotification;
    private CheckBox mCheckUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mEditText = findViewById(R.id.school_class);
        mEditText.setOnEditorActionListener(new DoneOnEditorActionListener());

        mCheckNotification = findViewById(R.id.notifications);
        mCheckUserData = findViewById(R.id.userdata);

        mButton = findViewById(R.id.confirm_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mButton != null) {
                    //close keyboard
                    InputMethodManager imm = (InputMethodManager)v.getContext().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    String schoolClass = mEditText.getText().toString();

                    //using the SharedPreferences file to permanently store the users decisions
                    SharedPreferences settings = getSharedPreferences(SETTINGS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("schoolClass", schoolClass);
                    editor.putBoolean("notifications", mCheckNotification.isChecked());
                    editor.putBoolean("storeUserData", mCheckUserData.isChecked());
                    editor.apply();

                    //little user response that the settings were saved
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.settings_activity),
                                                    "Settings applied.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    finish();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
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

                //simulate button click
                mButton.performClick();
                return true;
            }
            return false;
        }
    }
}
