package util;

import android.app.Activity;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import util.su.SUHelper;

/**
 * Created by bgm on 3/16/2016 AD.
 */
public class LCService {
    public static String dnFileStr = "/sdcard/dn.txt";
    public static String socialFileStr = "/sdcard/cerSocial.txt";
    public static String hiroFileStr = "/sdcard/HiroMacro/Temp/log.txt";
    public static String oriDBLocation = "/data/data/org.zwanoo.android.speedtest/databases/speedtest";
    public static String dbLocation = "/sdcard/speedtest";

    public static String outPath = "/sdcard/cer/";
    public static String uploadUrl = "http://203.209.87.52:8080/cer/file.jsp";

    public static String process(final Activity activity) throws IOException {
        U.d("buildAllLog... ");
        U.setTextViewTextWithTS(activity, "buildAllLog...");
        String outFileStr = buildAllLog(activity);

        U.d("uploading... " + outFileStr);
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... files) {
                U.setTextViewTextWithTS(activity, "Uploading: " + files[0]);
                upload(files[0]);
                return files[0];
            }
            @Override
            protected void onPostExecute(String s) {
                String uploadContent = null;
                try {
                    uploadContent = FileUtils.readFileToString(new File(s));
                    U.setTextViewTextWithTS(activity, "Uploaded: " + s + "\n" + uploadContent);
                    U.d("Done...2");
                } catch (IOException e) {
                    U.e(e);
                }
            }
        }.execute(outFileStr);

        U.d("CleanUp...");
        cleanUP();
        U.d("Done...1");
        return "Waiting Backgorund Process...";
    }

    public static void copySpeedTestDBToSDCARD() throws IOException {
        SUHelper.sudoForResult("cp " + oriDBLocation + " " + dbLocation);
        SUHelper.sudoForResult("chmod 777 " + dbLocation);
        //SUHelper.sudoForResult("ls -l "+dbLocation);
    }

    public static String buildAllLog(Activity activity) throws IOException {
        String r = "";

        // dn.txt
        U.d("read dn:"+dnFileStr);
        String dn = FileUtils.readFileToString(new File(dnFileStr));
        U.d("dn:" + dn);
        r += dn+"\n";

        // social
        U.d("read social:"+socialFileStr);
        String social = FileUtils.readFileToString(new File(socialFileStr));
        U.d("social:" + social);
        r += social;

        // hiro.txt
        U.d("read hiro:"+hiroFileStr);
        File hiroFile = new File(hiroFileStr);
        String hiro = hiroFile.exists() ? FileUtils.readFileToString(hiroFile) : "";
        U.d("hiro:" + hiro);
        r += "\n" + hiro;

        // speed test
        copySpeedTestDBToSDCARD();
        String speedTestDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        String speedTest = speedTestDateStr + " DATA" + " SpeedTest|";
        Map m = new DBHelper(activity.getApplicationContext(), dbLocation).getLastResult();
        U.d("m=" + m);
        speedTest += m;
        r += "\n" + speedTest;

        String msidn = getIMEI(activity);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmssSSS");
        String outFile = outPath + msidn + "-" + df.format(new Date()) + ".txt";
        U.d("outFile=" + outFile);

        FileUtils.write(new File(outFile), r);
        return outFile;
    }

    public static String getIMEI(Activity activity) {
        TelephonyManager tm = (TelephonyManager) activity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String IMEI = tm.getDeviceId();
        U.d("IMEI=" + IMEI);
        return IMEI;
    }

    public static void cleanUP() {
        U.d("delete file hiro: " + hiroFileStr);
        FileUtils.deleteQuietly(new File(hiroFileStr));
    }


    public static String getClipDataStr(Activity activity) {
        String clipDataStr = "";
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.getPrimaryClip();

        if (!(clipboard.hasPrimaryClip())) {   // If it does contain data, decide if you can handle the data.
            U.d("no PrimaryClip");
        } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))) {  // since the clipboard has data but it is not plain text
            U.d("clipdata is not plain text:" + clipboard.getPrimaryClip().getItemAt(0).getText());
        } else {  //since the clipboard contains plain text.
            clipDataStr = clipboard.getPrimaryClip().getItemAt(0).toString();
        }
        return clipDataStr;
    }


    public void postUp() {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] imageBytes = baos.toByteArray();
//
//        HttpClient httpclient = new DefaultHttpClient();
//        HttpPost httpPost = new HttpPost(StaticData.AMBAJE_SERVER_URL + StaticData.AMBAJE_ADD_AMBAJ_TO_GROUP);
//
//        String boundary = "-------------" + System.currentTimeMillis();
//
//        httpPost.setHeader("Content-type", "multipart/form-data; boundary="+boundary);
//
//        ByteArrayBody bab = new ByteArrayBody(imageBytes, "pic.png");
//        StringBody sbOwner = new StringBody(StaticData.loggedUserId, ContentType.TEXT_PLAIN);
//        StringBody sbGroup = new StringBody("group", ContentType.TEXT_PLAIN);
//
//        HttpEntity entity = MultipartEntityBuilder.create()
//                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
//                .setBoundary(boundary)
//                .addPart("group", sbGroup)
//                .addPart("owner", sbOwner)
//                .addPart("image", bab)
//                .build();
//
//        httpPost.setEntity(entity);

//        try {
//            HttpResponse response = httpclient.execute(httpPost);
//            ...then reading response
    }

    public static void upload(String fileStr) {
        String fileUploadName = fileStr.substring(fileStr.lastIndexOf("/") + 1);
        U.d("fileUploadName=" + fileUploadName);

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        String existingFileName = fileStr;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        String urlString = uploadUrl;
        try {
            //------------------ CLIENT REQUEST
            FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
            // open a URL connection to the Servlet
            URL url = new URL(urlString);
            // Open a HTTP connection to the URL
            conn = (HttpURLConnection) url.openConnection();
            // Allow Inputs
            conn.setDoInput(true);
            // Allow Outputs
            conn.setDoOutput(true);
            // Don't use a cached copy.
            conn.setUseCaches(false);
            // Use a post method.
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            dos = new DataOutputStream(conn.getOutputStream());

//            dos.writeBytes(twoHyphens + boundary + lineEnd);
//            dos.writeBytes("Content-Disposition: form-data; name=\"dir\"" + lineEnd);
//            dos.writeBytes("/opt/cer" + lineEnd);

//            dos.writeBytes(twoHyphens + boundary + lineEnd);
//            dos.writeBytes("Content-Disposition: form-data; name=\"sort\"" + lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"myFile\";filename=\"" + fileUploadName + "\"" + lineEnd); // uploaded_file_name is the Name of the File to be uploaded
            dos.writeBytes("Content-Type: text/plain" + lineEnd);
            dos.writeBytes(lineEnd);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"Submit\"" + lineEnd);
            dos.writeBytes("Upload" + lineEnd);

            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


//            "-----------------------------7e035420ae0650"
//            Content-Disposition: form-data; name="dir"
//                    /opt/cer
//                    -----------------------------7e035420ae0650
//            Content-Disposition: form-data; name="sort"
//                    -----------------------------7e035420ae0650
//            Content-Disposition: form-data; name="Submit"
//            Upload
//                    -----------------------------7e035420ae0650--


            fileInputStream.close();
            dos.flush();
            dos.close();
        } catch (MalformedURLException ex) {
            Log.e("Debug", "error: " + ex.getMessage(), ex);
        } catch (IOException ioe) {
            Log.e("Debug", "error: " + ioe.getMessage(), ioe);
        }
        //------------------ read the SERVER RESPONSE
        try {
            inStream = new DataInputStream(conn.getInputStream());
            String str;
            while ((str = inStream.readLine()) != null) {
                Log.e("Debug", "Server Response " + str);
                //reponse_data = str;
            }
            inStream.close();
        } catch (IOException ioex) {
            Log.e("Debug", "error: " + ioex.getMessage(), ioex);
        }

    }


}
