package example.testgooglemapapi.testingmaps2;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by Lee on 6/4/2016.
 */
public class Server {
    Context context;
    private String device_id;
    private String date;
    private String lat;
    private String longit;

    public Server(Context context, String device_id, String date, String lat, String longit){
        this.context = context;
        this.device_id = device_id;
        this.date = date;
        this.lat = lat;
        this.longit = longit;
    }

    public void send() throws Exception{
        example.testgooglemapapi.testingmaps2.http.HttpPost post = new example.testgooglemapapi.testingmaps2.http.HttpPost(getUrl() + "?op=create", "UTF-8");
        post.addFormField("deviceID", this.device_id);
        post.addFormField("date", this.date);
        post.addFormField("latitude", this.lat);
        post.addFormField("longitude", this.longit);
        String json = post.finish();
        JSONObject obj = new JSONObject(json);
    }

    private static String getUrl() {
        return "https://carbide-kayak-128503.appspot.com/server";
    }
}
