package com.qiniu.bzcomp.network;


import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.google.gson.Gson;
import com.qiniu.comp.network.NetBzException;
import com.qiniu.bzcomp.user.UserInfoManager;


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class JsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private Gson gson;
    private Type type;
    private Annotation[] annotations;

    public JsonResponseBodyConverter(Gson gson, Type type, Annotation[] annotations) {
        this.gson = gson;
        this.type = type;
        this.annotations = annotations;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        String response = value.string();

        try {
            ParameterizedTypeImpl p = new ParameterizedTypeImpl(new Type[]{type}, HttpResp.class, HttpResp.class);
            HttpResp<T> baseBean = gson.fromJson(response, p);
            if(baseBean.getCode()==401001 ||401003==baseBean.getCode()){
                UserInfoManager.INSTANCE.onLogout(baseBean.getMessage());
                return null;
            }
            if ( baseBean.getCode()!=0) {
                throw new NetBzException(baseBean.getCode(), baseBean.getMessage());
            }
            return (T) baseBean.getData();
        } finally {
            value.close();
        }
    }
}
