package com.martin.ads.omoshiroilib.debug.teststmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.martin.ads.omoshiroilib.R;

import java.util.List;

public class SearchHistoryActivity extends AppCompatActivity {
    private HistoryDBHelper helper;
    private List<String> historys;
    private LinearLayout main2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_history);
      main2= findViewById(R.id.main2);
      findViewById(R.id.bt_clear).setOnClickListener(v -> {
          helper.delete_all_chosen();
          main2.removeAllViews();
          Toast.makeText(this, "清除成功", Toast.LENGTH_LONG).show();
      });
        init_historys();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void init_historys() {
        helper = HistoryDBHelper.getInstance(this);
        historys = helper.queryall();
        if (historys.size() == 0) {
            return;
        }
        main2.removeAllViews();
        for (String history : historys) {
            View view = LayoutInflater.from(this).inflate(R.layout.simple_list_item_1, null);
            TextView textView = view.findViewById(android.R.id.text1);
            textView.setText(history);
            main2.addView(textView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        init_historys();
    }
}
