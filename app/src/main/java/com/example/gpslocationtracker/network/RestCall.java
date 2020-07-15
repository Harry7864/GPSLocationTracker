package com.example.gpslocationtracker.network;


import com.example.gpslocationtracker.networkresponce.CommonResponce;
import com.example.gpslocationtracker.networkresponce.LoginResponce;
import com.example.gpslocationtracker.networkresponce.OtpResponce;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Single;

public interface RestCall {


    @FormUrlEncoded
    @POST("loginController.php")
    Single<LoginResponce> LoginCheck(
            @Field("login_action") String login_action,
            @Field("user_mobile") String user_mobile,
            @Field("login_password") String login_password,
            @Field("user_device_type") String user_device_type,
            @Field("user_phone") String user_phone,
            @Field("phone_model") String phone_model,
            @Field("token") String token

    );

    @FormUrlEncoded
    @POST("loginController.php")
    Single<CommonResponce> forgotPassword(
            @Field("forgot_password") String forgot_password,
            @Field("forgot_mobile") String forgot_mobile

    );

    @FormUrlEncoded
    @POST("loginController.php")
    Single<CommonResponce> ChangePassword(
            @Field("password2") String passwordtag,
            @Field("forgot_id") String forgot_id,
            @Field("otp") String otp,
            @Field("password") String password,
            @Field("password2") String password2

    );

    @FormUrlEncoded
    @POST("locationController.php")
    Single<CommonResponce> getLatLong(
            @Field("user_location") String user_location,
            @Field("user_id") String user_id,
            @Field("address") String address,
            @Field("area") String area,
            @Field("locality") String locality,
            @Field("user_lat") String user_lat,
            @Field("user_long") String user_long

    );

    @FormUrlEncoded
    @POST("agent_login_controller.php")
    Single<OtpResponce> sendOtp(
            @Field("checkLogin") String tag,
            @Field("agent_mobile") String agent_mobile
    );

    @FormUrlEncoded
    @POST("agent_login_controller.php")
    Single<CommonResponce> getLogout(
            @Field("checkLogin") String tag,
            @Field("agent_mobile") String agent_mobile
    );


}
