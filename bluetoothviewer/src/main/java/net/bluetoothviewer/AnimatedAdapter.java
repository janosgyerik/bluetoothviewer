package net.bluetoothviewer;

import java.util.ArrayList;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AnimatedAdapter extends ArrayAdapter<String> {
    private Context context;
    private LayoutInflater mInflater;
    private ArrayList<String> strings;
    private DisplayMetrics metrics_;

    private class Holder {
        public TextView textview;
    }

    public AnimatedAdapter(Context context, ArrayList<String> strings) {
        super(context, R.layout.message, strings);
        this.context = context;
        this.mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.strings = strings;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final String str = this.strings.get(position);
        final Holder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
            convertView.setBackgroundColor(0xFF202020);

            holder = new Holder();
            holder.textview = (TextView) convertView.findViewById(android.R.id.text1);
            holder.textview.setTextColor(0xFFFFFFFF);
            holder.textview.setBackgroundResource(0);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.textview.setText(str);

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.push_left_in);

        animation.setDuration(500);
        convertView.startAnimation(animation);
        animation = null;

        return convertView;
    }
}