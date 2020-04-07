package com.example.gchat.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAilHEvNU:APA91bHSjMYfrRVjDgAorscOeHZjX1NljUBGmp-mRqEk2LyyzbQ7M1dquAyGToIX4hfsTKAVEd3OzEcwVTEXpznrdm_LE6sXGtY98sCO9BCFMjv_pGxtuQVpx9_ddvNVst_DK5vcZ8xE"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
