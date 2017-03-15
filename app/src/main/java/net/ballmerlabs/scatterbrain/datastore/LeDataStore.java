package net.ballmerlabs.scatterbrain.datastore;

import android.app.Activity;
import android.app.Service;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;
import net.ballmerlabs.scatterbrain.ScatterLogManager;
import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gnu3ra on 11/3/15.
 * <p/>
 * Imnplemnets a sort of queue for messages. Messages are added when recieved and can be
 * retrieved all at once for burst transmission. Mesages are deleted when older than a certain
 * age.
 */
public class LeDataStore {
    private SQLiteDatabase db;
    private MsgDbHelper helper;
    private int dataTrimLimit;
    private final String TAG = "DataStore";
    private Service mainService;
    private Cursor c;
    public boolean connected;
    public final String[] names = {
            MsgDataDb.MessageQueue.COLUMN_NAME_HASH,
            MsgDataDb.MessageQueue.COLUMN_NAME_EXTBODY,
            MsgDataDb.MessageQueue.COLUMN_NAME_BODY,
            MsgDataDb.MessageQueue.COLUMN_NAME_APPLICATION,
            MsgDataDb.MessageQueue.COLUMN_NAME_TEXT,
            MsgDataDb.MessageQueue.COLUMN_NAME_TTL,
            MsgDataDb.MessageQueue.COLUMN_NAME_REPLYLINK,
            MsgDataDb.MessageQueue.COLUMN_NAME_SENDERLUID,
            MsgDataDb.MessageQueue.COLUMN_NAME_RECEIVERLUID,
            MsgDataDb.MessageQueue.COLUMN_NAME_SIG,
            MsgDataDb.MessageQueue.COLUMN_NAME_FLAGS};

    public LeDataStore(Service mainService, int trim) {
        dataTrimLimit = trim;
        this.mainService = mainService;

    }

    public synchronized void flushDb() {
        db.execSQL("DELETE FROM " + MsgDataDb.MessageQueue.TABLE_NAME);
    }

    public synchronized void connect() {
        ScatterLogManager.v(TAG, "Connected to datastore");
        helper = new MsgDbHelper(mainService.getApplicationContext());
        db = helper.getWritableDatabase();
        connected = true;
    }
    public synchronized void disconnect() {
        ScatterLogManager.v(TAG, "Disconnected from datastore");
        db.close();
        connected = false;
    }


    public void setDataTrimLimit(int val) {
        dataTrimLimit = val;
    }

    public int getDataTrimLimit() {
        return dataTrimLimit;
    }


    /*
     * sticks a message into the datastore at the front?.
     */
    public synchronized void enqueueMessage(String uuid, int extbody,   String body, String application, int text,  int ttl,
                               String replyto, String luid, String receiverLuid,
                               String sig, String flags) {

       // ScatterLogManager.e(TAG, "Enqueued a message to the datastore.");
        ContentValues values = new ContentValues();
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_HASH, uuid);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_EXTBODY, extbody);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_BODY, body);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_APPLICATION, application);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_TEXT, text);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_TTL, ttl);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_REPLYLINK, replyto);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_SENDERLUID, luid);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_RECEIVERLUID, receiverLuid);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_SIG, sig);
        values.put(MsgDataDb.MessageQueue.COLUMN_NAME_FLAGS, flags);

        long newRowId;
        newRowId = db.insert(MsgDataDb.MessageQueue.TABLE_NAME,
                null,
                values);

        trimDatastore(dataTrimLimit);

    }

    public int enqueueMessageNoDuplicate(BlockDataPacket bd) {
        Cursor cu = db.rawQuery("SELECT * FROM " +
                MsgDataDb.MessageQueue.TABLE_NAME +
                " WHERE " +
                MsgDataDb.MessageQueue.COLUMN_NAME_HASH +
                " = " + "?",
                new String[] {bd.getHash()});

        if(cu.getCount() == 0){
           // ScatterLogManager.v(TAG, "No duplicate found (" + cu.getCount() + ") Inserting hash " + bd.getHash() +"  "+  bd.size + "  " + bd.senderluid.length);
            return enqueueMessage(bd);
        }
        else {
           // ScatterLogManager.e(TAG, "Attempted to insert duplicate data to datastore");
            return 1;
        }
    }

    /* Very temporary method for writing a blockdata stanza to datastore */
    public synchronized int enqueueMessage(BlockDataPacket bd) {
        try {
            if (connected) {
                if (bd.isInvalid()) {
                    ScatterLogManager.e(TAG, "Tried to store an invalid packet");
                    return -1;
                }
                enqueueMessage(bd.getHash(), 0, Base64.encodeToString(bd.body, Base64.DEFAULT),
                        "SenpaiDetector", 1, -1, Base64.encodeToString(bd.senderluid, Base64.DEFAULT),
                        Base64.encodeToString(bd.senderluid, Base64.DEFAULT), "none",
                        Base64.encodeToString(bd.receiverluid, Base64.DEFAULT), "none, none");
            } else {
                ScatterLogManager.e(TAG, "Tried to save a packet with datastore disconnected.");
                return -2;
            }
        }
        catch(Exception e) {
            ScatterLogManager.e(TAG, "Exception in enqueueMessage:\n" + e.getStackTrace());
        }
        return 0;
    }

    public synchronized BlockDataPacket messageToBlockData(Message m) {
        BlockDataPacket result = new BlockDataPacket(Base64.decode(m.body, Base64.DEFAULT),true,
                Base64.decode(m.senderluid,Base64.DEFAULT));
        return result;
    }


    /*
     * this is called to trim the datastore and leave only the x newest entires
     * Makes messages 'die out' after a while
     */
    public synchronized void trimDatastore(int limit) {
     //   ScatterLogManager.v(TAG, "Trimming message queue. Too long.");
        String del = "DELETE FROM " + MsgDataDb.MessageQueue.TABLE_NAME +
                " WHERE ROWID IN (SELECT ROWID FROM "
                + MsgDataDb.MessageQueue.TABLE_NAME +
                " ORDER BY ROWID DESC LIMIT -1 OFFSET " + limit + ")\n";
        db.execSQL(del);
    }



    /*
     * (Hopefully) returns an array list of Message objects with all the data
     * in the datastore in it.
     */
    public synchronized ArrayList<Message> getMessages() {

    //    ScatterLogManager.v(TAG, "Mass dumping all messages from datastore");

        final String SEP = ", ";
        Cursor cu = db.rawQuery("SELECT "+
                        MsgDataDb.MessageQueue.COLUMN_NAME_HASH + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_EXTBODY + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_BODY + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_APPLICATION + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_TEXT + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_TTL + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_REPLYLINK + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_SENDERLUID + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_RECEIVERLUID + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_SIG + SEP +
                        MsgDataDb.MessageQueue.COLUMN_NAME_FLAGS +
                " FROM " + MsgDataDb.MessageQueue.TABLE_NAME, null);

        ArrayList<Message> finalresult = new ArrayList<Message>();
        cu.moveToFirst();
        //check here for overrun problems
        while (!cu.isAfterLast()) {
            String  hash = cu.getString(0);
            int extbody = cu.getInt(1);
            String body = cu.getString(2);
            String application = cu.getString(3);
            int text = cu.getInt(4);
            int ttl = cu.getInt(5);
            String replylink = cu.getString(6);
            String senderluid = cu.getString(7);
            String receiverluid = cu.getString(8);
            String sig = cu.getString(9);
            String flags = cu.getString(10);

            cu.moveToNext();
            finalresult.add(new Message(hash, extbody, body, application,
                    text, ttl, replylink, senderluid, receiverluid, sig, flags));
        }

        return finalresult;

    }


    public ArrayList<Message> getMessageByHash(String compare_hash) {

      //  ScatterLogManager.v(TAG, "Retreiving message from hash");

        final String SEP = ", ";
        Cursor cu = db.rawQuery("SELECT "+
                MsgDataDb.MessageQueue.COLUMN_NAME_HASH + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_EXTBODY + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_BODY + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_APPLICATION + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_TEXT + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_TTL + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_REPLYLINK + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_SENDERLUID + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_RECEIVERLUID + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_SIG + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_FLAGS +
                " FROM " + MsgDataDb.MessageQueue.TABLE_NAME +
                " WHERE " + MsgDataDb.MessageQueue.COLUMN_NAME_HASH + " = " +
                 "?", new String[] {compare_hash});

        ArrayList<Message> finalresult = new ArrayList<Message>();
        cu.moveToFirst();
        //check here for overrun problems
        while (!cu.isAfterLast()) {
            String  hash = cu.getString(0);
            int extbody = cu.getInt(1);
            String body = cu.getString(2);
            String application = cu.getString(3);
            int text = cu.getInt(4);
            int ttl = cu.getInt(5);
            String replylink = cu.getString(6);
            String senderluid = cu.getString(7);
            String receiverluid = cu.getString(8);
            String sig = cu.getString(9);
            String flags = cu.getString(10);

            cu.moveToNext();
            finalresult.add(new Message(hash, extbody, body, application,
                    text, ttl, replylink, senderluid, receiverluid, sig, flags));
        }


        return finalresult;

    }


    /*
     * Gets n rows from the datastore in a random order. For use when there is no time to transmit
     * the entire datastore.
     */
    public ArrayList<BlockDataPacket> getTopRandomMessages(int count) {
        final String SEP = ", ";
        Cursor cu = db.rawQuery("SELECT " +
                MsgDataDb.MessageQueue.COLUMN_NAME_HASH + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_EXTBODY + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_BODY + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_APPLICATION + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_TEXT + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_TTL + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_REPLYLINK + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_SENDERLUID + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_RECEIVERLUID + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_SIG + SEP +
                MsgDataDb.MessageQueue.COLUMN_NAME_FLAGS  +" FROM " + MsgDataDb.MessageQueue.TABLE_NAME
                + " ORDER BY RANDOM() LIMIT " + count, null);


       // ScatterLogManager.v(TAG, "Attempting to retrieve a random packet from datastore");
        //check here for overrun problems
        ArrayList<BlockDataPacket> finalresult = new ArrayList<BlockDataPacket>();
        cu.moveToFirst();
        //check here for overrun problems
        while (!cu.isAfterLast()) {
            String  hash = cu.getString(0);
            int extbody = cu.getInt(1);
            String body = cu.getString(2);
            String application = cu.getString(3);
            int text = cu.getInt(4);
            int ttl = cu.getInt(5);
            String replylink = cu.getString(6);
            String senderluid = cu.getString(7);
            String receiverluid = cu.getString(8);
            String sig = cu.getString(9);
            String flags = cu.getString(10);

            cu.moveToNext();
            boolean t;
            if(text == 0) {
                t = false;
            }
            else {
                t = true;
            }
            //ScatterLogManager.e(TAG, body);
            if(body.length() > 0) {
                finalresult.add(new BlockDataPacket(Base64.decode(body, Base64.DEFAULT), t, Base64.decode(senderluid, Base64.DEFAULT)));
            }
        }

        return finalresult;

    }


}
