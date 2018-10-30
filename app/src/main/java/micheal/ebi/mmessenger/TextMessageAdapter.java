package micheal.ebi.mmessenger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TextMessageAdapter extends ArrayAdapter<TextMessage> {
    public TextMessageAdapter(Context context, ArrayList<TextMessage> textMessages){
        super(context, 0, textMessages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        TextMessage textMessage = getItem(position);
        //check if an existing view is being used, otherwise inflate the view
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_list_item, parent, false);
        }
        //lookup view for data population
        TextView messageHead = (TextView) convertView.findViewById(R.id.message_head);
        TextView messageContent = (TextView) convertView.findViewById(R.id.message_contents);
        //populate the data into the template view using the data object
        messageHead.setText("\n" + textMessage.head);
        messageContent.setText(textMessage.body);
        //return the completed view to render on screen
        return convertView;
    }
}
