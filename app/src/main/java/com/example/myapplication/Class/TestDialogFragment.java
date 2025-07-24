package com.example.myapplication.Class;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;


public class TestDialogFragment extends DialogFragment {
    public interface DialogListener {
        void onDialogWalkClick();
        void onDialogCarClick();
    }

    private DialogListener listener;

    private static final String TAG = "TestDialogFragment";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (DialogListener) context;
        Log.i(TAG,"onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
    }



    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG,"onViewCreated");


    }

    @Override
    public void onActivityCreated(@Nullable  Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG,"onActivityCreated");
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.i(TAG,"onDismiss dialog onDismiss");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG,"onStart");
        //window属性设置
        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
        layoutParams.width = 600;
        getDialog().getWindow().setAttributes(layoutParams);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG,"onStop");
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG,"onSaveInstanceState");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG,"onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG,"onDetach");
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i(TAG,"onCreateDialog");
//        AlertDialog dialog = new AlertDialog.Builder(getActivity())
//                .setTitle("DialogFragment示例")
//                .setMessage("我是DialogFragment示例onCreateDialog实现方式")
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .create();
//        return dialog;
        Dialog dialog1 = new Dialog(getActivity());
        return dialog1;

//        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i(TAG,"onCreateView");
        View view = inflater.inflate(R.layout.dialogfragment_test, null);
        view.findViewById(R.id.walk).setOnClickListener(v -> {
            listener.onDialogWalkClick();
        });
        view.findViewById(R.id.car).setOnClickListener(v -> {
            listener.onDialogCarClick();
        });
        return view;
//        return super.onCreateView(inflater,container,savedInstanceState);
    }
}