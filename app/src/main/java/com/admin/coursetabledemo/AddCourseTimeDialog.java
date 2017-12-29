package com.admin.coursetabledemo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by admin on 2017/12/29.
 */

public class AddCourseTimeDialog extends Dialog {

    @BindView(R.id.btn_cancel)
    Button btnCancel;
    @BindView(R.id.btn_ok)
    Button btnOk;
    @BindView(R.id.ed_start_time)
    EditText edStartTime;
    @BindView(R.id.ed_end_time)
    EditText edEndTime;

    public AddCourseTimeDialog(@NonNull Context context) {
        super(context, R.style.dialog_add_course);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_course_time);
        // 将对话框的大小按屏幕大小的百分比设置
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        final WindowManager.LayoutParams p = getWindow().getAttributes();
        p.height = (int) (d.getHeight() * 0.7);
        p.width = (int) (d.getWidth() * 0.7);
        getWindow().setAttributes(p);

        setCanceledOnTouchOutside(false);
        ButterKnife.bind(this);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setBtnSure(View.OnClickListener listener) {
        btnOk.setOnClickListener(listener);
    }

    public void setBtnCancel(View.OnClickListener listener) {
        btnCancel.setOnClickListener(listener);
    }

    public void setEdStartTime(String str){
        edStartTime.setText(str);
    }

    public EditText getEdStartTime() {
        return edStartTime;
    }

    public void setEdEndTime(String str){
        edEndTime.setText(str);
    }

    public EditText getEdEndTime() {
        return edEndTime;
    }
}
