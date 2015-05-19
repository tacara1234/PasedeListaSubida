package com.pasedelista.app.pasedelista.Model;


public class Student {

    private String mDNI;
    private String mName;
    private boolean mAssisted = false;

    public Student( String name, String dni ){
        mDNI = dni;
        mName = name;
    }

    public boolean isAssisted() {
        return mAssisted;
    }

    public void setAssisted(boolean mAssisted) {
        this.mAssisted = mAssisted;
    }

    public String getDNI() {
        return mDNI;
    }

    public void setDNI(String mDNI) {
        this.mDNI = mDNI;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }
}
