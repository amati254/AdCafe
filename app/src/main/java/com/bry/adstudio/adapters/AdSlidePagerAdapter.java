package com.bry.adstudio.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bry.adstudio.R;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * Created by bryon on 6/4/2017.
 */

public class AdSlidePagerAdapter extends BaseAdapter{
    ArrayList<String> uris;
    Context context;

    public AdSlidePagerAdapter(ArrayList<String> uris, Context context){
        this.uris = uris;
        this.context = context;
    }

    @Override
    public int getCount() {
        return uris.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }


}
