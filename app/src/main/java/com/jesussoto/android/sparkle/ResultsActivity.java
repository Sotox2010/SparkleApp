package com.jesussoto.android.sparkle;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.RuleBasedCollator;

public class ResultsActivity extends ActionBarActivity {

    public static final String EXTRA_RESULT = "extra_result";

    private TextView mResultsView;
    private Handler mHandler= new Handler();

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        mResultsView = (TextView) findViewById(R.id.results);
        mResultsView.setText(extras.getString(EXTRA_RESULT));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    }, 150);
                    return true;
                }
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
