package com.zx.jdkill.test;

import com.alibaba.fastjson.JSONObject;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: zhaoxu
 * @date: 2021/1/9 18:59
 */
public class Login {
    static String venderId = "";
    static Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>(16);
    static String ticket = "";

    public static void Login() throws IOException, URISyntaxException, InterruptedException {
        JSONObject headers = new JSONObject();
        headers.put(Start.headerAgent, Start.headerAgentArg);
        headers.put(Start.Referer, Start.RefererArg);
        //获取二维码
        Long now = System.currentTimeMillis();
        HttpUrlConnectionUtil.getQCode(headers, "https://qr.m.jd.com/show?appid=133&size=147&t=" + now);
        //打开二维码
        Runtime.getRuntime().exec("cmd /c QCode.png");
        URI url = new URI("https://qr.m.jd.com/show?appid=133&size=147&t=" + now);
        Map<String, List<String>> stringListMap = new HashMap<String, List<String>>();
        stringListMap = Start.manager.get(url, requestHeaders);
        List cookieList = stringListMap.get("Cookie");
        String cookies = cookieList.get(0).toString();
        String token = cookies.split("wlfstk_smdl=")[1];
        headers.put("Cookie", cookies);
        //判断是否扫二维码
        while (true) {
            String checkUrl = "https://qr.m.jd.com/check?appid=133&callback=jQuery" + (int) ((Math.random() * (9999999 - 1000000 + 1)) + 1000000) + "&token=" + token + "&_=" + System.currentTimeMillis();
            String qrCode = HttpUrlConnectionUtil.get(headers, checkUrl);
            if (qrCode.indexOf("二维码未扫描") != -1) {
                System.out.println("二维码未扫描，请扫描二维码登录");
            } else if (qrCode.indexOf("请手机客户端确认登录") != -1) {
                System.out.println("请手机客户端确认登录");
            } else {
                ticket = qrCode.split("\"ticket\" : \"")[1].split("\"\n" +
                        "}\\)")[0];
                System.out.println("已完成二维码扫描登录");
                close();
                break;
            }
            Thread.sleep(3000);
        }
        //验证，获取cookie
        String qrCodeTicketValidation = HttpUrlConnectionUtil.get(headers, "https://passport.jd.com/uc/qrCodeTicketValidation?t=" + ticket);
        stringListMap = Start.manager.get(url, requestHeaders);
        cookieList = stringListMap.get("Cookie");
        cookies = cookieList.get(0).toString();
        headers.put("Cookie", cookies);
    }

    public static void close() throws IOException, InterruptedException {
//        通过窗口标题获取窗口句柄
        WinDef.HWND hWnd;
        final User32 user32 = User32.INSTANCE;
        user32.EnumWindows(new WinUser.WNDENUMPROC() {
            @Override
            public boolean callback(WinDef.HWND hWnd, Pointer arg1) {
                char[] windowText = new char[512];
                user32.GetWindowText(hWnd, windowText, 512);
                String wText = Native.toString(windowText);
                // get rid of this if block if you want all windows regardless of whether
                // or not they have text
                if (wText.isEmpty()) {
                    return true;
                }
                if (wText.contains("照片")) {
                    hWnd = User32.INSTANCE.FindWindow(null, wText);
                    WinDef.LRESULT lresult = User32.INSTANCE.SendMessage(hWnd, 0X10, null, null);
                }
                return true;
            }
        }, null);
    }
}
