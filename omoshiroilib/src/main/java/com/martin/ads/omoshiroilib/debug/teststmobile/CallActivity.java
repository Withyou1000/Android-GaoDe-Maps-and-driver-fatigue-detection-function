package com.martin.ads.omoshiroilib.debug.teststmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.martin.ads.omoshiroilib.R;

public class CallActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences config;
    private EditText et1;
    private EditText et2;
    private EditText et3;
    private EditText et4;
    private EditText et5;
    private EditText et6;
    private EditText et7;
    private EditText et8;
    private String cur_number;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;
    private RadioButton rb4;
    private EditText et_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_call);
        et1 = findViewById(R.id.one_name);
        et2 = findViewById(R.id.one_num);
        et3 = findViewById(R.id.two_name);
        et4 = findViewById(R.id.two_num);
        et5 = findViewById(R.id.three_name);
        et6 = findViewById(R.id.three_num);
        et7 = findViewById(R.id.four_name);
        et8 = findViewById(R.id.four_num);
        rb1 = findViewById(R.id.rb_1);
        rb1.setOnClickListener(this);
        rb2 = findViewById(R.id.rb_2);
        rb2.setOnClickListener(this);
        rb3 = findViewById(R.id.rb_3);
        rb3.setOnClickListener(this);
        rb4 = findViewById(R.id.rb_4);
        et_time = findViewById(R.id.et_time);
        findViewById(R.id.bt_confirm).setOnClickListener(v->{
            if (rb1.isChecked()) {
                cur_number = et2.getText().toString();
            } else if (rb2.isChecked()) {
                cur_number = et4.getText().toString();
            } else if (rb3.isChecked()) {
                cur_number = et6.getText().toString();
            } else if (rb4.isChecked()) {
                cur_number = et8.getText().toString();
            }
            Intent intent = new Intent();
            intent.putExtra("number", cur_number);
            intent.putExtra("time",et_time.getText().toString());
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
        rb4.setOnClickListener(this);

        config = getSharedPreferences("config", Context.MODE_PRIVATE);
        reload();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        SharedPreferences.Editor editor = config.edit();
        editor.putString("et1", et1.getText().toString());
        editor.putString("et2", et2.getText().toString());
        editor.putString("et3", et3.getText().toString());
        editor.putString("et4", et4.getText().toString());
        editor.putString("et5", et5.getText().toString());
        editor.putString("et6", et6.getText().toString());
        editor.putString("et7", et7.getText().toString());
        editor.putString("et8", et8.getText().toString());
        editor.commit();
        super.onDestroy();
        Intent intent = new Intent();
        intent.putExtra("number", cur_number);
        setResult(Activity.RESULT_OK, intent);
    }

    private void reload() {
        et1.setText(config.getString("et1", ""));
        et2.setText(config.getString("et2", ""));
        et3.setText(config.getString("et3", ""));
        et4.setText(config.getString("et4", ""));
        et5.setText(config.getString("et5", ""));
        et6.setText(config.getString("et6", ""));
        et7.setText(config.getString("et7", ""));
        et8.setText(config.getString("et8", ""));

    }


    @Override
    public void onClick(View v) {
        rb1.setChecked(R.id.rb_1 == v.getId());
        rb2.setChecked(R.id.rb_2 == v.getId());
        rb3.setChecked(R.id.rb_3 == v.getId());
        rb4.setChecked(R.id.rb_4 == v.getId());
    }
}