package com.badger.attendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.badger.Constants;
import com.badger.R;

public class AttendanceArrayAdapter extends ArrayAdapter<String> {
  private final Context context;
  private final String[] values;

  public AttendanceArrayAdapter(Context context, String[] values) {
    super(context, R.layout.attendance, values);
    this.context = context;
    this.values = values;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    String name = values[position];
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.attendance, parent, false);
    TextView textView = (TextView) rowView.findViewById(R.id.label);
    ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
    textView.setText(name);

    SharedPreferences attendanceMap = context.getSharedPreferences(Constants.ATTENDANCE_PREFS_NAME, 0);
    if (attendanceMap.contains(name) && attendanceMap.getBoolean(name, false)) {
      imageView.setImageResource(R.drawable.yes);
    } else {
      imageView.setImageResource(R.drawable.no);
    }
    return rowView;
  }
}
