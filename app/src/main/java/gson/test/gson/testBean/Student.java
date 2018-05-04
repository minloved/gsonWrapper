package gson.test.gson.testBean;

import android.app.Activity;

import com.google.gson.annotations.SerializedName;

import gson.test.gson.gsonWrapper.Optional;

public class Student extends Person{

    @Optional
    public Person friend;

    @Optional
    public Object object;

    @Optional
    @SerializedName(value = "name")
    public String name;

    @Optional(optional = false)
    public Integer age;

}
