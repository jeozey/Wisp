package com.rj.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rj.view.button.CustomWidgetButton;

import java.util.List;

public class FormTopBtnPopupAdapter extends BaseAdapter {
    private List<CustomWidgetButton> formTopButtonlist;
    private LayoutInflater inflater;
    private int resource;
    private Context context;

    public FormTopBtnPopupAdapter(Context context,
                                  List<CustomWidgetButton> formTopButtonlist, int resource) {
        this.formTopButtonlist = formTopButtonlist;
        this.inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resource = resource;
        this.context = context;

    }

    @Override
    public int getCount() {
        return formTopButtonlist.size();
    }

    @Override
    public Object getItem(int position) {
        return formTopButtonlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewCache viewCache = null;
        if (convertView == null) {
            convertView = inflater.inflate(resource, null);
            viewCache = new viewCache();
            viewCache.popupIcon = (ImageView) convertView
                    .findViewById(R.id.popup_icon);
            viewCache.popupText = (TextView) convertView
                    .findViewById(R.id.popup_txt);
            convertView.setTag(viewCache);
        } else {
            viewCache = (viewCache) convertView.getTag();
        }
        final CustomWidgetButton info = formTopButtonlist.get(position);
        viewCache.popupIcon.setBackgroundDrawable(info.getBeforeImg());
        if (info.getBeforeImg() == null) {
            viewCache.popupIcon.setVisibility(View.GONE);
        } else {
            viewCache.popupIcon.setVisibility(View.VISIBLE);
        }

        // AutoSizeTool.setFormTopNavigationBarImageView(viewCache.popupIcon);
        viewCache.popupText.setText(info.getTitle());
        // AutoSizeTool.setTopNavigationPopupWindowTextSize(viewCache.popupText);
        // AutoSizeTool.setFormTopNavigationBarImageView(viewCache.popupTickIconImgv);
        return convertView;
    }

    public class viewCache {
        ImageView popupIcon;
        TextView popupText;
    }

}
