package com.badger;

import java.io.File;
import java.io.IOException;


import android.os.Environment;
import android.util.Log;

public class AlbumUtil {
  private static final String JPEG_FILE_SUFFIX = ".jpg";
  private static final String ALBUM_NAME = "badger";
  
  public static File getImageFile(String name) throws IOException {
    String imageFileName = name.replace(" ", "_") + JPEG_FILE_SUFFIX;
    File albumDirectory = getAlbumDir();
    return new File(albumDirectory, imageFileName);
  }
  
  public static File getAlbumDir() {
    File storageDir = null;
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      storageDir = AlbumUtil.getAlbumStorageDir();
      if (storageDir != null) {
        if (!storageDir.mkdirs()) {
          if (!storageDir.exists()){
            Log.d("CameraSample", "failed to create directory");
            return null;
          }
        }
      }
    } else {
      Log.v(Constants.TAG, "External storage is not mounted READ/WRITE.");
    }
    return storageDir;
  }
  
  public static File getAlbumStorageDir() {
    return new File(
        Environment.getExternalStoragePublicDirectory(
          Environment.DIRECTORY_PICTURES
        ), 
        ALBUM_NAME
      );
  }
}
