package com.example.android.waitlist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.android.waitlist.data.WaitlistContract;
import com.example.android.waitlist.data.WaitlistDbHelper;


public class MainActivity extends AppCompatActivity {

    private GuestListAdapter mAdapter;
    public SQLiteDatabase mDb;

    private EditText mGuestName;
    private EditText mGuestAge;
    private String mSex;

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    private Button btAdd;
    private int mPriority;
    private RadioGroup mRG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView waitlistRecyclerView;
        waitlistRecyclerView = (RecyclerView) this.findViewById(R.id.all_guests_list_view);
        waitlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        WaitlistDbHelper dbHelper = new WaitlistDbHelper(this);

        mDb = dbHelper.getWritableDatabase();
        Cursor cursor = getAllGuests();
//        Cursor cursor2 = sortByName();
        mAdapter = new GuestListAdapter(this, cursor);



        btAdd = (Button) findViewById(R.id.btnAddGuest);
        btAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog,null);
                mGuestName = (EditText) mView.findViewById(R.id.et_guest_name);
                mGuestAge = (EditText) mView.findViewById(R.id.et_guest_age);

                mRG = (RadioGroup) mView.findViewById(R.id.radGroup);
                mRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (mRG.getCheckedRadioButtonId()) {
                            case R.id.radButton1:
                                mPriority = 1;
                                break;
                            case R.id.radButton2:
                                mPriority = 2;
                                break;
                            default:
                                mPriority = 0;
                                break;
                        }
                    }
                });



                Button dialogCancel = (Button) mView.findViewById(R.id.btnCancel) ;
                Button dialogOK = (Button) mView.findViewById(R.id.btnOK) ;

                dialogCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.cancelDragAndDrop();
                    }
                });


                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });


        waitlistRecyclerView.setAdapter(mAdapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //do nothing, we only care about swiping
                return false;
            }

            // COMPLETED (5) Override onSwiped
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // COMPLETED (8) Inside, get the viewHolder's itemView's tag and store in a long variable id
                //get the id of the item being swiped
                long id = (long) viewHolder.itemView.getTag();
                // COMPLETED (9) call removeGuest and pass through that id
                //remove from DB
                removeGuest(id);
                // COMPLETED (10) call swapCursor on mAdapter passing in getAllGuests() as the argument
                //update the list
                mAdapter.swapCursor(getAllGuests());
            }

            //COMPLETED (11) attach the ItemTouchHelper to the waitlistRecyclerView
        }).attachToRecyclerView(waitlistRecyclerView);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.sortByName) {
            mAdapter.swapCursor(sortByName());
            return true;
        }
        else if(itemThatWasClickedId == R.id.sortByGender){
            mAdapter.swapCursor(sortByGender());
            return true;
        }
        else if(itemThatWasClickedId == R.id.sortByID){
            mAdapter.swapCursor(sortByID());
            return true;
        }
        else{
            mAdapter.swapCursor(sortByAge());
            return true;
        }
    }


    public void addToWaitlist(View view) {
        if (mGuestName.getText().length() == 0 ||
                mGuestAge.getText().length() == 0) {
            return;
        }
        //default party size to 1
        int gAge = 1;
        try {
            //mNewPartyCountEditText inputType="number", so this should always work
            gAge = Integer.parseInt(mGuestAge.getText().toString());

        } catch (NumberFormatException ex) {
            Log.e(LOG_TAG, "Failed to parse party size text to number: " + ex.getMessage());
        }


        mSex = " ";

        if(mPriority==1){
            mSex= "Male";
        }
        else if(mPriority==2){
            mSex= "Female";
        }
        else{
            mSex = "None";
        }

        // Add guest info to mDb
        addNewGuest(mGuestName.getText().toString(), gAge , mSex);

        // Update the cursor in the adapter to trigger UI to display the new list
        mAdapter.swapCursor(getAllGuests());

        //clear UI text fields
        mGuestAge.clearFocus();
        mGuestName.getText().clear();
        mGuestAge.getText().clear();

    }

    private Cursor getAllGuests() {
        return mDb.query(
                WaitlistContract.WaitlistEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private Cursor sortByName(){
         return mDb.query(
                 WaitlistContract.WaitlistEntry.TABLE_NAME,
                 null,
                 null,
                 null,
                 null,
                 null,
                 WaitlistContract.WaitlistEntry.COLUMN_GUEST_NAME
         );
    }

    private Cursor sortByGender(){
        return mDb.query(
                WaitlistContract.WaitlistEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                WaitlistContract.WaitlistEntry.COLUMN_GUEST_SEX
        );
    }
    private Cursor sortByID(){
        return mDb.query(
                WaitlistContract.WaitlistEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                WaitlistContract.WaitlistEntry._ID
        );
    }
    private Cursor sortByAge(){
        return mDb.query(
                WaitlistContract.WaitlistEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                WaitlistContract.WaitlistEntry.COLUMN_GUEST_AGE
        );
    }

    private long addNewGuest(String name, int age ,String sex) {
        ContentValues cv = new ContentValues();
        cv.put(WaitlistContract.WaitlistEntry.COLUMN_GUEST_NAME, name);
        cv.put(WaitlistContract.WaitlistEntry.COLUMN_GUEST_AGE, age);
        cv.put(WaitlistContract.WaitlistEntry.COLUMN_GUEST_SEX, sex);

        return mDb.insert(WaitlistContract.WaitlistEntry.TABLE_NAME, null, cv);
    }

    private boolean removeGuest(long id) {
        // COMPLETED (2) Inside, call mDb.delete to pass in the TABLE_NAME and the condition that WaitlistEntry._ID equals id
        return mDb.delete(WaitlistContract.WaitlistEntry.TABLE_NAME,
                WaitlistContract.WaitlistEntry._ID + "=" + id, null) > 0;
    }



}