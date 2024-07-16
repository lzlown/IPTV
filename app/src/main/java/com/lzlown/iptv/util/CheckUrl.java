package com.lzlown.iptv.util;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class CheckUrl {
   private static List<String> stringList=new ArrayList<>();
    public static void ss(String path) {
        OkGo.<String>get(path)
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            BufferedReader bufferedReader = new BufferedReader(new StringReader( response.getRawResponse().body().string()));
                            String readLine = bufferedReader.readLine();
                            while (readLine != null) {
                                if (readLine.trim().isEmpty()) {
                                    readLine = bufferedReader.readLine();
                                } else {
                                    String[] split = readLine.split(",");
                                    if (split.length < 2) {
                                        readLine = bufferedReader.readLine();
                                    } else {
                                        if (readLine.contains("#genre#")) {

                                        } else {
                                            stringList.add(split[1]);
                                        }
                                        readLine = bufferedReader.readLine();
                                    }
                                }
                            }

                            bufferedReader.close();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);

                    }

                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return "";
                    }
                });
    }

    public static Boolean is(String path) {
        for (String s : stringList) {
            if (s.equals(path)){
                return true;
            }
        }
        return false;
    }
}
