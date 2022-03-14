package com.qiniu.comp.network;

import android.text.TextUtils;

import androidx.constraintlayout.solver.state.State;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Form2JsonInterceptor implements Interceptor {

    public static String headerForm2Json = "headerForm2Json";
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody body = request.body();
        if(TextUtils.isEmpty(request.header(headerForm2Json))){
            return chain.proceed(request);
        }
        if (body instanceof FormBody) {
            FormBody formBody = (FormBody) body;
            Map<String, String> formMap = new HashMap<>();
            // 从 formBody 中拿到请求参数，放入 formMap 中
            for (int i = 0; i < formBody.size(); i++) {
                formMap.put(formBody.name(i), formBody.value(i));
            }
            // 将 formMap 转化为 json 然后 AES 加密
            Gson gson = new Gson();
            String jsonParams = gson.toJson(formMap);
            // 重新修改 body 的内容
            body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);

        }
        request = request.newBuilder()
                .post(body)
                .build();
        return chain.proceed(request);
    }
}
