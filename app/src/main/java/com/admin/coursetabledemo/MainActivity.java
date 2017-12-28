package com.admin.coursetabledemo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.layout_course_number)
    LinearLayout layoutCourseNumber;//课程表左侧的节数整体

    private static int WIDTH_SINGLE_COURSE_NUMBER = 110;//左侧单个课程编号的宽度，此处110px与xml布局里“节/周”的宽度一样
    private static int HEIGHT_SINGLE_COURSE_NUMBER = 180;//左侧单个课程编号的高度
    private MessageDialog messageDialog;
    private AddCourseDialog addCourseDialog;
    private int maxClassNumber = 0;//课程表左侧的最大节数
    private int currentCourseNumber = 1; //课程表左侧的当前节数
    private RelativeLayout layoutDay = null; //星期几
    private DatabaseHelper databaseHelper = new DatabaseHelper(this, "database.db", null, 1); //SQLite Helper类
    private ArrayList<CourseMode> courseList = new ArrayList<>(); //用于程序启动时从数据库加载多个课程对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        loadData();
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
                if(addCourseDialog == null){
                    addCourseDialog = new AddCourseDialog(MainActivity.this);
                }
                addCourseDialog.show();
                addCourseDialog.setBtnSure(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(addCourse()){
                            addCourseDialog.dismiss();
                        }
                    }
                });
                break;
        }
        return true;
    }

    /**
     * 加载数据
     */
    private void loadData() {
        SQLiteDatabase sqLiteDatabase =  databaseHelper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from course", null);
        if (cursor.moveToFirst()) {
            do {
                courseList.add(new CourseMode(
                        cursor.getString(cursor.getColumnIndex("course_name")),
                        cursor.getString(cursor.getColumnIndex("teacher")),
                        cursor.getString(cursor.getColumnIndex("class_room")),
                        cursor.getInt(cursor.getColumnIndex("day")),
                        cursor.getInt(cursor.getColumnIndex("start")),
                        cursor.getInt(cursor.getColumnIndex("end"))));
            } while(cursor.moveToNext());
        }
        cursor.close();

        //根据从数据库读取的数据加载课程表视图
        for (CourseMode course : courseList) {
            createCourseNumberView(course);
            createCourseInfoView(course);
        }
    }

    /**
     * 创建课程表左边"节数"的视图：
     * 根据课程结束的节数来判断要不要增加"节数"视图
     * @param course
     */
    private void createCourseNumberView(CourseMode course) {
        int len = course.getEnd();
        if (len > maxClassNumber) {
            View courseNumberView;//单个课程节数
            TextView tvCourseNumber;
            for (int i = 0; i < len - maxClassNumber; i++) {
                courseNumberView = LayoutInflater.from(this).inflate(R.layout.item_course_number, null);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WIDTH_SINGLE_COURSE_NUMBER,HEIGHT_SINGLE_COURSE_NUMBER);
                courseNumberView.setLayoutParams(params);
                tvCourseNumber = courseNumberView.findViewById(R.id.tv_course_number);
                tvCourseNumber.setText("" + currentCourseNumber);
                layoutCourseNumber.addView(courseNumberView);
                currentCourseNumber++;
            }
            maxClassNumber = len;
        }
    }

    /**
     * 创建此课程的卡片视图
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
            //点击删除课程
            courseCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                        }
                    });
                }
            });
        }
    }

    /**
     * 添加课程
     */
    public boolean addCourse(){
        String courseName = addCourseDialog.getEdCourseName().getText().toString();
        String teacher = addCourseDialog.getEdTeacherName().getText().toString();
        String classRoom = addCourseDialog.getEdCourseRoom().getText().toString();
        String day = addCourseDialog.getEdWeek().getText().toString();
        String start = addCourseDialog.getEdCourseBegin().getText().toString();
        String end = addCourseDialog.getEdCourseEnds().getText().toString();
        if (courseName.equals("") || day.equals("") || start.equals("") || end.equals("")) {
            Toast.makeText(MainActivity.this, "基本课程信息未填写", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            CourseMode course = new CourseMode(courseName, teacher, classRoom,
                    Integer.valueOf(day), Integer.valueOf(start), Integer.valueOf(end));
            //存储数据到数据库
            saveData(course);
            //创建课程表左边视图(节数)
            createCourseNumberView(course);
            //创建课程表视图
            createCourseInfoView(course);
            return true;
        }
    }

    /**
     * 保存数据
     * @param course
     */
    private void saveData(CourseMode course) {
        SQLiteDatabase sqLiteDatabase =  databaseHelper.getWritableDatabase();
        sqLiteDatabase.execSQL("insert into course(course_name, teacher, class_room, day, start, end) " +
                "values(?, ?, ?, ?, ?, ?)", new String[] {course.getCourseName(), course.getTeacher(),
                course.getClassRoom(), course.getDay()+"", course.getStart()+"", course.getEnd()+""});
    }

}
