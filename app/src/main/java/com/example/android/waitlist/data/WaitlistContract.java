package com.example.android.waitlist.data;

import android.provider.BaseColumns;

public class WaitlistContract {

    public static final class WaitlistEntry implements BaseColumns {
        public static final String TABLE_NAME = "waitlist";
        public static final String COLUMN_GUEST_NAME = "guestName";
        public static final String COLUMN_GUEST_AGE = "guestAge";
        public static final String COLUMN_GUEST_SEX = "guestSex";
//        public static final String COLUMN_GUEST_ID = "id";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }

}
