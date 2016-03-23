package util;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.example.bgm.logcollectorapp.BuildConfig;
import com.example.bgm.logcollectorapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bgm on 3/19/2016 AD.
 */
public class U {
    public static String getCurDateStr() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS ");
        return df.format(new Date());
    }

    public static void d(Object o) {
        Log.d("BG", o.toString());
    }

    public static void e(Object o) {
        if (o instanceof Throwable) Log.e("BG", o.toString(), (Throwable) o);
        else Log.e("BG", o.toString());
    }

    public static void quit() {
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
    }

    /**
     * Config version in your build.gradle (Module)
     * <br/>
     * EX:
     * defaultConfig {
     * ...
     * versionCode 1
     * versionName "1.0"
     * }
     *
     * @param activity
     */
    public static void setTitleAppVersion(Activity activity) {
        activity.setTitle(activity.getString(R.string.app_name) + " " + BuildConfig.VERSION_CODE + " " + BuildConfig.VERSION_NAME);
    }

    public static void setTextViewText(Activity activity, String r) {
        ((TextView) activity.findViewById(R.id.TV01)).setText(r);
    }

    public static void setTextViewTextWithTS(Activity activity, Object r) {
        ((TextView) activity.findViewById(R.id.TV01)).setText(getCurDateStr()+"\n"+r);
    }


}
