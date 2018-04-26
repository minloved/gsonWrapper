package gson.test.gson.testBean;

import android.app.Activity;

import gson.test.gson.gsonWrapper.Optional;

public class Student extends Person{

    @Optional
    public Person friend;

    @Optional
    public Object object;

    @Optional
    public String name;

    @Optional
    public Integer age;

//    public int id;

}
