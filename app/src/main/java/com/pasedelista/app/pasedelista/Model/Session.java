package com.pasedelista.app.pasedelista.Model;


public class Session {

    public static int TYPE_PRACTICE = 1;
    public static int TYPE_THEORY = 2;

    private int mSessionNumber;
    private int mType;
    private Student[] mStudents;

    public Session( int sessionNumber, int type ){
        mSessionNumber = sessionNumber;
        mType = type;
    }

    public int getSessionNumber() {
        return mSessionNumber;
    }

    public Student[] getStudents() {
        return mStudents;
    }

    public void setStudents(Student[] mStudents) {
        this.mStudents = mStudents;
    }

    public void setSessionNumber(int sessionNumber) {
        mSessionNumber = sessionNumber;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }


}
