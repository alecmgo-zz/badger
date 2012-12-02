package com.badger;

import android.nfc.Tag;

public class Util {
  public static String getTagAsHex(Tag tag) {
    byte[] bytes = tag.getId();
    StringBuilder sb = new StringBuilder();
    for (int i = bytes.length - 1; i >= 0; --i) {
      int b = bytes[i] & 0xff;
      if (b < 0x10)
        sb.append('0');
      sb.append(Integer.toHexString(b));
      if (i > 0) {
        sb.append(" ");
      }
    }
    return sb.toString();
  }
}
