package com.anticorruptionforce.acf.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MySMSBroadcastReceiver extends BroadcastReceiver {

    private SharedPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub


        SmsMessage msgs[] = null;
        Bundle bundle = intent.getExtras();
        try {
            Object pdus[] = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int n = 0; n < pdus.length; n++) {
                byte[] byteData = (byte[]) pdus[n];
                SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdus[n]);
                msgs[n] = SmsMessage.createFromPdu(byteData);
                String from = msgs[n].getOriginatingAddress();
                String message = currentMessage.getDisplayMessageBody();
                try {
                    if (from.contains("CP-AHSSMS")) {
                        String  body = message ;
                        String[] strData = body.split(":");
                        String OTP = strData[1].trim();
                        if(!OTP.equalsIgnoreCase(""))
                        {
                            Intent myIntent = new Intent("otp");
                            myIntent.setAction("otp");
                            myIntent.putExtra("message", message);
                            myIntent.putExtra("number", OTP);
                            //LocalBroadcastManager.getInstance(context).sendBroadcast(myIntent);
                            context.sendBroadcast(myIntent);

                        }
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {

        }
       /* for (int i = 0; i < msgs.length; i++) {
                String message = msgs[i].getDisplayMessageBody();
            if (message != null && message.length() > 0) {
                String from = msgs[i].getOriginatingAddress();
                if(from.contains("CP-AHSSMS")){
                    String  body = message ;
                    String[] strData = body.split(":");
                    String OTP = strData[1].trim();
                    if(!OTP.equalsIgnoreCase(""))
                    {
                        Intent myIntent = new Intent("otp");
                        myIntent.putExtra("message", message);
                        myIntent.putExtra("number", OTP);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(myIntent);
                    }
                }
            }*/
        }
    }


       /* Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        String specificPhoneNumber = "QP-AINEXT";

        if (bundle != null)
        {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                String phNum = msgs[i].getOriginatingAddress();
                str += msgs[i].getMessageBody().toString();
                if (specificPhoneNumber.equals(phNum))

                {
                    Uri uri = Uri.parse("content://sms/inbox");

                    ContentResolver contentResolver = context.getContentResolver();

                    String where = "address="+phNum;
                    Cursor cursor = contentResolver.query(uri, new String[] { "_id", "thread_id"}, where, null,
                            null);

                    while (cursor.moveToNext()) {

                        long thread_id = cursor.getLong(1);
                        where = "thread_id="+thread_id;
                        Uri thread = Uri.parse("content://sms/inbox");
                        context.getContentResolver().delete(thread, where, null);

                    }
                }
            }
        }
    }*/