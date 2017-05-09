package com.example.lew.indoornavigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Lew on 4/5/2017.
 */
public class MetricsScreen extends Activity {
    private EditText height;
    private Button button;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Typeface fontRobo = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/cool.ttf");
        setContentView(R.layout.metricscreen);
        height = (EditText)findViewById(R.id.height);
        button = (Button)findViewById(R.id.save);
        title=(TextView)findViewById(R.id.title);

        title.setTypeface(fontRobo);

        height.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == height.getId())
                {
                    height.setCursorVisible(true);
                    height.setText("");
                }
            }
        });

        height.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                height.setCursorVisible(false);
                if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(height.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

        SharedPreferences prefs = getSharedPreferences(Constants.HEIGHT_PREFERENCES, MODE_PRIVATE);
        float str = prefs.getFloat(Constants.HEIGHT_STRING,0f);
        if (str != 0f) {
            float savedHeight = str;
            if (savedHeight != 0f) {
                Intent intent = new Intent(MetricsScreen.this, LoadingScreen.class);
                intent.putExtra("height", savedHeight);
                startActivity(intent);
            }
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (height.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), Constants.WARNING, Toast.LENGTH_SHORT).show();
                    return;
                }
                float savedHeight = Float.parseFloat(height.getText().toString());

                SharedPreferences.Editor editor = getSharedPreferences(Constants.HEIGHT_PREFERENCES, MODE_PRIVATE).edit();
                editor.putFloat(Constants.HEIGHT_STRING, savedHeight);
                editor.commit();

                Intent intent = new Intent(MetricsScreen.this, LoadingScreen.class);
                intent.putExtra("height",savedHeight);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

}
