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
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by admin on 2017/12/28.
 */

public class AddCourseDialog extends Dialog {

    @BindView(R.id.ed_course_name)
    EditText edCourseName;
    @BindView(R.id.ed_week)
    EditText edWeek;
    @BindView(R.id.ed_course_begin)
    EditText edCourseBegin;
    @BindView(R.id.ed_course_ends)
    EditText edCourseEnds;
    @BindView(R.id.ed_teacher_name)
    EditText edTeacherName;
    @BindView(R.id.ed_course_room)
    EditText edCourseRoom;
    @BindView(R.id.btn_cancel)
    Button btnCancel;
    @BindView(R.id.btn_ok)
    Button btnOk;

    public AddCourseDialog(@NonNull Context context) {
        super(context, R.style.dialog_add_course);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_course);

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

    public void setBtnSure(View.OnClickListener listener){
        btnOk.setOnClickListener(listener);
    }

    public void setBtnCancel(View.OnClickListener listener){
        btnCancel.setOnClickListener(listener);
    }

    public EditText getEdCourseName() {
        return edCourseName;
    }

    public EditText getEdWeek() {
        return edWeek;
    }

    public EditText getEdCourseBegin() {
        return edCourseBegin;
    }

    public EditText getEdCourseEnds() {
        return edCourseEnds;
    }

    public EditText getEdTeacherName() {
        return edTeacherName;
    }

    public EditText getEdCourseRoom() {
        return edCourseRoom;
    }

}
