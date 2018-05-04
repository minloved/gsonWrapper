package gson.test.gson;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.Random;

import gson.test.gson.gsonWrapper.GsonWrapper;
import gson.test.gson.testBean.Person;
import gson.test.gson.testBean.Student;

/**
 * @author zhangyu
 */
public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        final TextView jsonValue = findViewById(R.id.json_Value);
        final TextView beanValue = findViewById(R.id.bean_Value);
        final TextView exceptionValue = findViewById(R.id.exception_Value);

        findViewById(R.id.toJson).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Gson gson = GsonWrapper.wrapper(new Gson());

                String json = gson.toJson(createStudent());
                Log.e(TAG,"json= "+json);

                jsonValue.setText(json);
            }
        });

        findViewById(R.id.toBean).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Gson gson = GsonWrapper.wrapper(new Gson());

                try {
                    Student newStudent = gson.fromJson(jsonValue.getText().toString(),Student.class);
                    newStudent.name = "NewName#"+ newStudent.name;

                    String json = gson.toJson(newStudent);
                    Log.e(TAG,"Bean= "+json);

                    beanValue.setText(json);

                }catch (Throwable t){
                    Log.e("TAG","Throwable: " + t.toString());
                    exceptionValue.setText(t.getMessage());
                }

            }
        });
    }


    /**
     *
     * 1 Object 字段  反序列的时候有问题
     * 2 继承同名字段  会抛出异常
     *
     */


    private Student createStudent(){

        final Student student = new Student();

        Random random = new Random();
        student.id = random.nextInt(1000);
        student.name = "name:" + student.id;
//        student.age = random.nextInt(15);


        Person person = new Person();   
        person.id = 100;

        student.friend = person;

        student.object = person;

        return student;

    }
}
