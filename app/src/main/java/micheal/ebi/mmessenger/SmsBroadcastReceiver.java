package micheal.ebi.mmessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();

        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            //String smsMessageStr = "";
            TextMessage newSmsMessage = new TextMessage("0","0", "0");
            for (int i = 0; i < sms.length; ++i) {
                String format = intentExtras.getString("format");
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);

                String smsBody = smsMessage.getMessageBody();
                String address = smsMessage.getOriginatingAddress();


                //smsMessageStr += "SMS From: " + address + "\n";
                //smsMessageStr += smsBody + "\n";
                newSmsMessage = new TextMessage(address, smsBody, address);

            }


            Toast.makeText(context, "Message Received!", Toast.LENGTH_SHORT).show();

            if (MainActivity.active) {
                MainActivity inst = MainActivity.instance();
                inst.updateInbox(newSmsMessage);
            } else {
                Intent i = new Intent(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);

            }

        }
    }
}

