package com.admin.coursetabledemo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.layout_course_number)
    LinearLayout layoutCourseNumber;//课程表左侧的节数整体

    private static int MAX_COURSE_NUMBER = 12;//课程表左侧的最大节数
    private static int WIDTH_SINGLE_COURSE_NUMBER = 110;//左侧单个课程编号的宽度，此处110px与xml布局里“节/周”的宽度一样
    private static int HEIGHT_SINGLE_COURSE_NUMBER = 180;//左侧单个课程编号的高度
    private MessageDialog messageDialog;
    private AddCourseDialog addCourseDialog;
    private AddCourseTimeDialog addCourseTimeDialog;
    private RelativeLayout layoutDay = null; //星期几
    private DatabaseHelper databaseHelper = new DatabaseHelper(this, "database.db", null, 1); //SQLite Helper类
    private ArrayList<CourseTimeMode> courseTimeList = new ArrayList<>(); //用于程序启动时从数据库加载多个课程对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        loadCourseData();
        loadCourseTime();
    }

    /**
     * toolbar的菜单项
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_courses:
                addCourse();
                break;
        }
        return true;
    }

    /**
     * 加载数据
     */
    private void loadCourseData() {
        SQLiteDatabase sqLiteDatabase =  databaseHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from course", null);
        if (cursor.moveToFirst()) {
            do {
                CourseMode course = new CourseMode(
                        cursor.getString(cursor.getColumnIndex("course_name")),
                        cursor.getString(cursor.getColumnIndex("teacher")),
                        cursor.getString(cursor.getColumnIndex("class_room")),
                        cursor.getInt(cursor.getColumnIndex("day")),
                        cursor.getInt(cursor.getColumnIndex("start")),
                        cursor.getInt(cursor.getColumnIndex("end")));
                course.setId( cursor.getInt(cursor.getColumnIndex("id")));
                createCourseInfoView(course);
            } while(cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * 加载课程时间
     */
    private void loadCourseTime() {
        SQLiteDatabase sqLiteDatabase =  databaseHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from coursetime", null);
        if (cursor.moveToFirst()) {
            do {
                courseTimeList.add( new CourseTimeMode(
                        cursor.getInt(cursor.getColumnIndex("courseindex")),
                        cursor.getString(cursor.getColumnIndex("starttime")),
                        cursor.getString(cursor.getColumnIndex("endtime"))));
            } while(cursor.moveToNext());
        }
        cursor.close();
        //加载左边"节数"视图
        createCourseNumberView();
    }

    /**
     * 创建课程表左边"节数"的视图：
     */
    private void createCourseNumberView() {
        View courseNumberView;//单个课程节数
        TextView tvCourseNumber;
        TextView tvCourseStartTime;
        TextView tvCourseEndTime;
        int count = 0;
        if(courseTimeList != null && courseTimeList.size() > 0){
            for( int i = 0; i < courseTimeList.size(); i++){
                courseNumberView = LayoutInflater.from(this).inflate(R.layout.item_course_number, null);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WIDTH_SINGLE_COURSE_NUMBER,HEIGHT_SINGLE_COURSE_NUMBER);
                courseNumberView.setLayoutParams(params);
                tvCourseNumber = courseNumberView.findViewById(R.id.tv_course_number);
                tvCourseStartTime = courseNumberView.findViewById(R.id.tv_start_time);
                tvCourseEndTime = courseNumberView.findViewById(R.id.tv_end_time);
                tvCourseNumber.setText("" + (i+1));
                tvCourseStartTime.setText(courseTimeList.get(i).getStartTime());
                tvCourseEndTime.setText(courseTimeList.get(i).getEndTime());
                final int index = i;
                layoutCourseNumber.addView(courseNumberView,index);
                courseNumberView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addCourseTime(index);
                    }
                });
            }
            count = courseTimeList.size();
        }
        for (int i = count; i < MAX_COURSE_NUMBER; i++) {
            courseNumberView = LayoutInflater.from(this).inflate(R.layout.item_course_number, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WIDTH_SINGLE_COURSE_NUMBER,HEIGHT_SINGLE_COURSE_NUMBER);
            courseNumberView.setLayoutParams(params);
            tvCourseNumber = courseNumberView.findViewById(R.id.tv_course_number);
            tvCourseNumber.setText("" + (i+1));
            final int index = i;
            layoutCourseNumber.addView(courseNumberView,index);
            courseNumberView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addCourseTime(index);
                }
            });
        }
    }

    /**
     * 创建一个课程的卡片视图
     * @param course
     */
    private void createCourseInfoView(final CourseMode course) {
        int integer = course.getDay();
        if ((integer < 1 && integer > 7) || course.getStart() > course.getEnd()) {
            Toast.makeText(this, "星期几没写对,或课程结束时间比开始时间还早~~", Toast.LENGTH_LONG).show();
        } else {
            //若使用LinearLayout，View控件添加的位置偏移会不受控制。
            switch (integer) {
                case 1: layoutDay = (RelativeLayout) findViewById(R.id.layout_monday);break;
                case 2: layoutDay = (RelativeLayout) findViewById(R.id.layout_tuesday);break;
                case 3: layoutDay = (RelativeLayout) findViewById(R.id.layout_wednesday);break;
                case 4: layoutDay = (RelativeLayout) findViewById(R.id.layout_thursday);break;
                case 5: layoutDay = (RelativeLayout) findViewById(R.id.layout_friday);break;
                case 6: layoutDay = (RelativeLayout) findViewById(R.id.layout_saturday);break;
                case 7: layoutDay = (RelativeLayout) findViewById(R.id.layout_weekday);break;
            }
            final View courseCardView = LayoutInflater.from(this).inflate(R.layout.item_course_card, null); //加载单个课程布局
            courseCardView.setY(HEIGHT_SINGLE_COURSE_NUMBER * (course.getStart()-1)); //设置开始高度,即第几节课开始
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT,(course.getEnd()-course.getStart()+1)*HEIGHT_SINGLE_COURSE_NUMBER - 2); //设置布局高度,即跨多少节课
            courseCardView.setLayoutParams(params);
            TextView tvCourseInfo = courseCardView.findViewById(R.id.tv_course_info);
            tvCourseInfo.setText(course.getCourseName() + "\n" + course.getTeacher() + "\n" + course.getClassRoom()); //显示课程名
            Random random = new Random();
            int index = random.nextInt(8);//生成0-8之间的随机数，包括0，不包括8
            switch (index){
                case 0: tvCourseInfo.setBackgroundColor(this.getResources().getColor(R.color.colorRed)); break;
                case 1: tvCourseInfo.setBackgroundColor(this.getResources().getColor(R.color.colorOrange)); break;
                case 2: tvCourseInfo.setBackgroundColor(this.getResources().getColor(R.color.colorYellow)); break;
                case 3: tvCourseInfo.setBackgroundColor(this.getResources().getColor(R.color.colorGreen)); break;
                case 4: tvCourseInfo.setBackgroundColor(this.getResources().getColor(R.color.colorCyan)); break;
                case 5: tvCourseInfo.setBackgroundColor(this.getResources().getColor(R.color.colorBlue)); break;
                case 6: tvCourseInfo.setBackgroundColor(this.getResources().getColor(R.color.colorPurple)); break;
                case 7: tvCourseInfo.setBackgroundColor(this.getResources().getColor(R.color.colorScarlet)); break;
            }
            layoutDay.addView(courseCardView);
            //点击修改此课程
            courseCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(addCourseDialog == null){
                        addCourseDialog = new AddCourseDialog(MainActivity.this);
                    }
                    addCourseDialog.show();
                    addCourseDialog.setTvTitle("修改课程");
                    addCourseDialog.getLayoutCourseName().setVisibility(View.GONE);
                    addCourseDialog.getLayoutWeek().setVisibility(View.GONE);
                    addCourseDialog.getLayoutCourseStart().setVisibility(View.GONE);
                    addCourseDialog.getLayoutEnd().setVisibility(View.GONE);
                    addCourseDialog.getEdTeacherName().setText(course.getTeacher());
                    addCourseDialog.getEdCourseRoom().setText(course.getClassRoom());
                    addCourseDialog.setBtnSure(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String teacher = addCourseDialog.getEdTeacherName().getText().toString();
                            String classRoom = addCourseDialog.getEdCourseRoom().getText().toString();
                            if (TextUtils.isEmpty(teacher) || TextUtils.isEmpty(classRoom)) {
                                Toast.makeText(MainActivity.this, "课程信息未填写", Toast.LENGTH_SHORT).show();
                            } else {
                                course.setTeacher(teacher);
                                course.setClassRoom(classRoom);
                                //存储数据到数据库
                                if(updateData(course)){
                                    //创建课程表视图
                                    createCourseInfoView(course);
                                    addCourseDialog.dismiss();
                                    Toast.makeText(MainActivity.this,"修改课程成功",Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(MainActivity.this,"修改课程失败",Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            });
            //长按删除此课程
            courseCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(messageDialog == null){
                        messageDialog = new MessageDialog(MainActivity.this);
                    }
                    messageDialog.show();
                    messageDialog.setContent("删除后不可恢复，确定要删除此课程吗？");
                    messageDialog.setBtnSure("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            courseCardView.setVisibility(View.GONE);//先隐藏
                            layoutDay.removeView(courseCardView);//再移除课程视图
                            SQLiteDatabase sqLiteDatabase =  databaseHelper.getWritableDatabase();
                            sqLiteDatabase.execSQL("delete from course where course_name = ?", new String[] {course.getCourseName()});
                            messageDialog.dismiss();
                            Toast.makeText(MainActivity.this,"删除课程成功",Toast.LENGTH_SHORT).show();
                        }
                    });
                    return true;
                }
            });
        }
    }

    /**
     * 添加课程具体信息
     */
    private void addCourse(){
        if(addCourseDialog == null){
            addCourseDialog = new AddCourseDialog(MainActivity.this);
        }
        addCourseDialog.show();
        addCourseDialog.setTvTitle("新增课程");
        addCourseDialog.getLayoutCourseName().setVisibility(View.VISIBLE);
        addCourseDialog.getLayoutWeek().setVisibility(View.VISIBLE);
        addCourseDialog.getLayoutCourseStart().setVisibility(View.VISIBLE);
        addCourseDialog.getLayoutEnd().setVisibility(View.VISIBLE);
        addCourseDialog.setBtnSure(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String courseName = addCourseDialog.getEdCourseName().getText().toString();
                String teacher = addCourseDialog.getEdTeacherName().getText().toString();
                String classRoom = addCourseDialog.getEdCourseRoom().getText().toString();
                String day = addCourseDialog.getEdWeek().getText().toString();
                String start = addCourseDialog.getEdCourseBegin().getText().toString();
                String end = addCourseDialog.getEdCourseEnds().getText().toString();
                if (TextUtils.isEmpty(courseName) || TextUtils.isEmpty(day) || TextUtils.isEmpty(start) || TextUtils.isEmpty(end)) {
                    Toast.makeText(MainActivity.this, "基本课程信息未填写", Toast.LENGTH_SHORT).show();
                } else if(!TextUtils.isEmpty(start) && Integer.parseInt(start) > MAX_COURSE_NUMBER){
                    Toast.makeText(MainActivity.this, "课程开始节数不能大于" + MAX_COURSE_NUMBER , Toast.LENGTH_SHORT).show();
                } else if(!TextUtils.isEmpty(end) && Integer.parseInt(end) > MAX_COURSE_NUMBER){
                    Toast.makeText(MainActivity.this, "课程结束节数不能大于" + MAX_COURSE_NUMBER , Toast.LENGTH_SHORT).show();
                } else {
                    CourseMode course = new CourseMode(courseName, teacher, classRoom,
                            Integer.valueOf(day), Integer.valueOf(start), Integer.valueOf(end));
                    //存储数据到数据库
                    if(saveData(course)){
                        //创建课程表视图
                        createCourseInfoView(course);
                        addCourseDialog.dismiss();
                        Toast.makeText(MainActivity.this,"添加课程成功",Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MainActivity.this,"添加课程失败",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * 新增课程数据
     * @param course
     */
    private boolean saveData(CourseMode course) {
        SQLiteDatabase sqLiteDatabase =  databaseHelper.getWritableDatabase();
//        sqLiteDatabase.execSQL("insert into course(course_name, teacher, class_room, day, start, end) " +
//                "values(?, ?, ?, ?, ?, ?)", new String[] {course.getCourseName(), course.getTeacher(),
//                course.getClassRoom(), course.getDay()+"", course.getStart()+"", course.getEnd()+""});
        ContentValues values = new ContentValues();
        values.put("course_name", course.getCourseName());
        values.put("teacher", course.getTeacher());
        values.put("class_room", course.getClassRoom());
        values.put("day", course.getDay());
        values.put("start", course.getStart());
        values.put("end", course.getEnd());
        long rowid = sqLiteDatabase.insert("course", null, values);//返回新添记录的行号，与主键id无关
        if(rowid != -1){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 更新课程数据
     * @param course
     */
    private boolean updateData(CourseMode course) {
        SQLiteDatabase sqLiteDatabase =  databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("teacher", course.getTeacher());
        values.put("class_room", course.getClassRoom());
        long rowid = sqLiteDatabase.update("course", values, "id=?",
                new String[]{course.getId()+""});
        if(rowid != -1){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 添加课程开始与结束时间（新增/更新）:
     * index从0开始的
     */
    private void addCourseTime(final int index){
        View courseNumberView = layoutCourseNumber.getChildAt(index);
        if(courseNumberView != null){
            final TextView tvCourseStartTime = courseNumberView.findViewById(R.id.tv_start_time);
            final TextView tvCourseEndTime = courseNumberView.findViewById(R.id.tv_end_time);
            if(addCourseTimeDialog == null){
                addCourseTimeDialog = new AddCourseTimeDialog(MainActivity.this);
            }
            addCourseTimeDialog.show();
            if(!TextUtils.isEmpty(tvCourseStartTime.getText().toString())){
                addCourseTimeDialog.setEdStartTime(tvCourseStartTime.getText().toString());
            }
            if(!TextUtils.isEmpty(tvCourseEndTime.getText().toString())){
                addCourseTimeDialog.setEdEndTime(tvCourseEndTime.getText().toString());
            }
            addCourseTimeDialog.setBtnSure(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String starTime = addCourseTimeDialog.getEdStartTime().getText().toString().trim();
                    String endTime = addCourseTimeDialog.getEdEndTime().getText().toString().trim();
                    if(!TextUtils.isEmpty(starTime)){
                       if(saveCourseTime(index,starTime,endTime)){
                           tvCourseStartTime.setText(starTime);
                           tvCourseEndTime.setText(endTime);
                           addCourseTimeDialog.setEdStartTime("");
                           addCourseTimeDialog.setEdEndTime("");
                           addCourseTimeDialog.dismiss();
                           Toast.makeText(MainActivity.this,"设置时间成功",Toast.LENGTH_SHORT).show();
                       }else {
                           Toast.makeText(MainActivity.this,"设置时间失败",Toast.LENGTH_SHORT).show();
                       }
                    }else {
                        Toast.makeText(MainActivity.this,"课程开始时间不能为空!",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean saveCourseTime(int index, String starTime, String endTime){
        boolean isSaved = false;
        SQLiteDatabase sqLiteDatabase =  databaseHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from coursetime where courseindex =" + index, null);
        if( cursor != null && cursor.getCount() > 0){
            //更新
//            sqLiteDatabase.execSQL("update coursetime set starttime ='" + starTime
//                    + "',endtime ='" + endTime+ "' where courseindex=" + index);
            ContentValues values = new ContentValues();
            values.put("starttime", starTime);
            values.put("endtime", endTime);
            long rowid = sqLiteDatabase.update("coursetime", values, "courseindex=?", new String[]{index+""});
            if(rowid != -1){
                isSaved = true;
            }
        }else {
            //新增
//            sqLiteDatabase.execSQL("insert into coursetime(courseindex, starttime, endtime) " +
//                    "values(?, ?, ?)", new String[] {index+"",starTime,endTime});
            ContentValues values = new ContentValues();
            values.put("courseindex", index);
            values.put("starttime", starTime);
            values.put("endtime", endTime);
            long rowid = sqLiteDatabase.insert("coursetime", null, values);
            if(rowid != -1){
                isSaved = true;
            }
        }
        cursor.close();
        return isSaved;
    }

}
