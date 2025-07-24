package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.myapplication.Class.GlideEngine;
import com.example.myapplication.Class.MyMarker;
import com.example.myapplication.Method.MapMarkDBHelper;
import com.example.myapplication.Method.Show;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.util.ArrayList;
import java.util.List;

public class AddPos extends AppCompatActivity implements View.OnClickListener {

    private EditText title;
    private EditText desc;
    private Button save;
    private MapMarkDBHelper helper;
    private String name;
    private double lat;
    private double lon;
    private boolean isFirst = true;
    private Button delete;
    private ImageView pic;
    private  String path="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_pos);

        title = findViewById(R.id.et_title);
        desc = findViewById(R.id.et_desc);
        save = findViewById(R.id.bt_info_save);
        delete = findViewById(R.id.bt_info_delete);
        pic = findViewById(R.id.iv_info_pic);
        pic.setOnClickListener(v -> chose_pic());
        save.setOnClickListener(this);
        delete.setOnClickListener(this);
        name = getIntent().getStringExtra("name");
        lat = getIntent().getDoubleExtra("lat", 0);
        lon = getIntent().getDoubleExtra("lon", 0);
        helper = MapMarkDBHelper.getInstance(this);
        helper.openRead();
        helper.openWrite();

        if (name != null) {
            MyMarker marker = null;
            marker = helper.query_marker_byName(name);
            lat = marker.lat;
            lon = marker.lon;
            title.setText(marker.name);
            desc.setText(marker.description);
            path=marker.pic_path;
            if(path!="")
            {
                pic.setBackgroundColor(Color.TRANSPARENT);
                Glide.with(AddPos.this)
                        .load(path)
                        .centerInside()
                        .into(pic);
            }
            isFirst = false;
        } else {
            isFirst = true;
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void chose_pic() {
        XXPermissions.with(this)
               /* // 申请单个权限
                .permission(Permission.RECORD_AUDIO)*/
                // 申请多个权限
                .permission(Permission.WRITE_EXTERNAL_STORAGE, Permission.CAMERA)
                // 设置权限请求拦截器（局部设置）
                //.interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                .unchecked()
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                           Show.show(AddPos.this,"获取部分权限成功，但部分权限未正常授予");
                            return;
                        }
                            select_from_phone();
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                           Show.show(AddPos.this,"被永久拒绝授权，请手动授予权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(AddPos.this, permissions);
                        } else {
                           Show.show(AddPos.this,"获取权限失败");
                        }
                    }
                });
    }

    private void select_from_phone() {
        PictureSelector.create(this)
                .openGallery(SelectMimeType.ofImage())
                .setMaxSelectNum(1)
                .setImageEngine(GlideEngine.createGlideEngine())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {

                        pic.setBackgroundColor(Color.TRANSPARENT);
                        path = result.get(0).getPath();
                        Glide.with(AddPos.this)
                                .load(path)
                                .centerInside()
                                .into(pic);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.closelink();
        name = null;
        lat = 0.0;
        lon = 0.0;
        isFirst = true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_info_save) {
            //这是我定义的marker
            MyMarker marker = new MyMarker();
            marker.name = title.getText().toString();
            marker.description = desc.getText().toString();
            marker.lat = lat;
            marker.lon = lon;
            marker.pic_path=path;
            if (isFirst) {
                helper.insert_marker(marker);
            } else {
                helper.update_marker(marker);
            }
            Intent intent = new Intent();
            intent.putExtra("name", marker.name);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        if (v.getId() == R.id.bt_info_delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示！");
            builder.setMessage("您确定要删了我吗");
            builder.setNegativeButton("等等",null);
            builder.setPositiveButton("是滴", (dialog, which) ->{
                Intent intent = new Intent();
                helper.delete_marker_byName(title.getText().toString());
                intent.putExtra("name", "delete");
                setResult(Activity.RESULT_OK, intent);
                finish();
            });
            AlertDialog dialog=builder.create();
            dialog.show();
        }
    }
}
