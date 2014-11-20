package net.bluetoothviewer;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AnimatedAdapter<T> extends ArrayAdapter<T> {
    private static final int DURATION = 500;
    private static final int REMOVE_BACKGROUND = 0;

    private final Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<T> mList;

    public AnimatedAdapter(Context context, ArrayList<T> list) {
        super(context, R.layout.message, list);
        this.mContext = context;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mList = list;
    }

    private static class Holder {
        private TextView mTextview;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final T elem = this.mList.get(position);
        final Holder mHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);

            mHolder = new Holder();
            mHolder.mTextview = (TextView) convertView.findViewById(android.R.id.text1);
            mHolder.mTextview.setBackgroundResource(REMOVE_BACKGROUND);

            convertView.setTag(mHolder);
        } else {
            mHolder = (Holder) convertView.getTag();
        }

        mHolder.mTextview.setText(elem.toString());

        // Choose whatever animation you want as defined in /res/anim/
        Animation mAnimation = AnimationUtils.loadAnimation(mContext, R.anim.push_left_in);
        mAnimation.setDuration(DURATION);
        convertView.startAnimation(mAnimation);

        // Return the animation as a view
        return convertView;
    }
}