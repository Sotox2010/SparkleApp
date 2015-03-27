package com.jesussoto.android.sparkle;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity {

    private Toolbar mToolbarActionBar;

    private EditText mEndpointUrlView;
    private EditText mSparqlQueryView;
    private Button mExecuteQueryButton;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mToolbarActionBar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbarActionBar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEndpointUrlView = (EditText) findViewById(R.id.endpoint_url_view);
        mSparqlQueryView = (EditText) findViewById(R.id.sparql_query_view);
        mExecuteQueryButton = (Button) findViewById(R.id.execute_button);

        mExecuteQueryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
