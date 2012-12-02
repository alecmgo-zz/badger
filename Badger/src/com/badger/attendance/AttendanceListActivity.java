package com.badger.attendance;

import java.util.List;


import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.badger.Constants;
import com.google.common.collect.Lists;

public class AttendanceListActivity extends ListActivity {
  public void onCreate(Bundle bundle)  {
    super.onCreate(bundle);
    SharedPreferences usersMap = getSharedPreferences(Constants.USERS_PREFS_NAME, 0);
    List<String> names = Lists.newArrayList();
    for (String key : usersMap.getAll().keySet()) {
      names.add(usersMap.getString(key, ""));
    }
    String[] values = names.toArray(new String[]{});
    AttendanceArrayAdapter adapter = new AttendanceArrayAdapter(this, values);
    setListAdapter(adapter);
  }
}
