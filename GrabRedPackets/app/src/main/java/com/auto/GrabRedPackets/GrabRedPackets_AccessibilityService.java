package com.auto.GrabRedPackets;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

/**
 * <p>Created by  li  10:25.</p>
 * <p><a href="mailto:liguoqing2003@hotmail.com">Email:mailto:liguoqing2003@hotmail.com</a></p>
 * <p>
 * 抢红包外挂服务
 */
public class GrabRedPackets_AccessibilityService extends AccessibilityService {

    static final String TAG = "GrabRedPackets";
    static final String QTAG = "AccessibilityService";
    private static final String openRedBagClassnae = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI";
    /**
     * 微信的包名
     */
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    /**
     * 红包消息的关键字
     */
    static final String HONGBAO_TEXT_KEY = "微信红包";
    static final String HONGBAO_TEXT__OLD_KEY = "已领取";
    Handler handler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        Log.e(QTAG, "事件---->" + event + " eventType:" + eventType);
        Log.e(QTAG, "event.getPackageName()" + event.getPackageName());


        //通知栏事件
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            if (WECHAT_PACKAGENAME.equals(event.getPackageName())) {
                openNotify(event);
            }
//            List<CharSequence> texts = event.getText();
//            if(!texts.isEmpty()) {
//                for(CharSequence t : texts) {
//                    String text = String.valueOf(t);
//                    if(text.contains(HONGBAO_TEXT_KEY)) {
//                        openNotify(event);
//                        break;
//                    }
//                }
//            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
        //   ||eventType ==   AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
        {
            openHongBao(event);
        }
    }

    /*@Override
    protected boolean onKeyEvent(KeyEvent event) {
        //return super.onKeyEvent(event);
        return true;
    }*/

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "连接抢红包服务", Toast.LENGTH_SHORT).show();
    }

    private void sendNotifyEvent() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (!manager.isEnabled()) {
            return;
        }
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
        event.setPackageName(WECHAT_PACKAGENAME);
        event.setClassName(Notification.class.getName());
        CharSequence tickerText = HONGBAO_TEXT_KEY;
        event.getText().add(tickerText);
        manager.sendAccessibilityEvent(event);
    }

    /**
     * 打开通知栏消息
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotify(AccessibilityEvent event) {
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        //以下是精华，将微信的通知栏消息打开
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
        Log.e(QTAG, "classname:" + event.getClassName());
        if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName()) || openRedBagClassnae.equals(event.getClassName())) {
            //点中了红包，下一步就是去拆红包
            checkKey1();
        } else if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            //拆完红包后看详细的纪录界面
            //nonething
        } else if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
            //在聊天界面,去点中红包
            checkKey2();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey1() {
        final AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {    //红米手机这里为空，过不去
            Log.w(TAG, "rootWindow为空");
            return;
        }


        recycle(nodeInfo, true);
//        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("开");
//        for(AccessibilityNodeInfo n : list) {
//            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey2() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(QTAG, "rootWindow为空");
            return;
        }


        recycle(nodeInfo, false);
//        list = nodeInfo.findAccessibilityNodeInfosByText(HONGBAO_TEXT_KEY);//list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
//       if(!list.isEmpty())
//        {
//          //  list = nodeInfo.findAccessibilityNodeInfosByText(HONGBAO_TEXT_KEY);
//            for(AccessibilityNodeInfo n : list) {
//                Log.i(TAG, "-->微信红包:" + n);
//                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                break;
//            }
//        } else {
//            //最新的红包领起
//            for(int i = list.size() - 1; i >= 0; i --) {
//                AccessibilityNodeInfo parent = list.get(i).getParent();
//                Log.i(TAG, "-->领取红包:" + parent);
//                if(parent != null) {
//                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                    break;
//                }
//            }
//        }
    }


    public void recycle(AccessibilityNodeInfo info, boolean open) {
        if (info.getChildCount() == 0) {
            if (null != info.getViewIdResourceName())
                Log.e(QTAG, "getViewIdResourceName：" + info.getViewIdResourceName().toString());
            Log.i(QTAG, "child widget----------------------------" + info.getClassName());
            if (info.getClassName().equals("android.widget.Button") && open) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                //后退键
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                return;
            }
            Log.i(QTAG, "showDialog:" + info.canOpenPopup());
            Log.i(QTAG, "Text：" + info.getText());
            Log.i(QTAG, "windowId:" + info.getWindowId());
            if (null != info.getText()) {
                Log.e(QTAG, "Text：" + info.getText().toString());
                if (HONGBAO_TEXT__OLD_KEY.equals(info.getText().toString())) {
                    // recycle(info,open);
                } else {
                    if (HONGBAO_TEXT_KEY.equals(info.getText().toString())) {
                        Log.e(QTAG, "getViewIdResourceName：" + info.getViewIdResourceName().toString());
                        AccessibilityNodeInfo p = info.getParent();
                        Log.e(QTAG, "parent getViewIdResourceName：" + info.getViewIdResourceName().toString());
                        List<AccessibilityNodeInfo> list = p.findAccessibilityNodeInfosByText(HONGBAO_TEXT__OLD_KEY);//list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
                        if (list.isEmpty()) {
                            p.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {

                    if (null != info.getPackageName())
                        Log.e(QTAG, "getPackageName：" + info.getPackageName().toString());
                    if (null != info.getViewIdResourceName())
                        Log.e(QTAG, "getViewIdResourceName：" + info.getViewIdResourceName().toString());
                    if (null != info.getText()) {
                        Log.e(QTAG, "Text：" + info.getText().toString());

                        List<AccessibilityNodeInfo> list = info.findAccessibilityNodeInfosByText(HONGBAO_TEXT__OLD_KEY);//list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
                        if (list.isEmpty()) {
                            list = info.findAccessibilityNodeInfosByText(HONGBAO_TEXT_KEY);//list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
                            if (!list.isEmpty()) {

                                for (AccessibilityNodeInfo n : list) {
                                    // Log.i(TAG, "-->微信红包:" + n);

                                    Log.e(QTAG, "getViewIdResourceName：" + n.getViewIdResourceName().toString());
                                    AccessibilityNodeInfo p = n.getParent();
                                    Log.e(QTAG, "parent getViewIdResourceName：" + n.getViewIdResourceName().toString());
                                    p.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                                }
                            }
                        }
                    }
                }
                recycle(info.getChild(i), open);
            }
        }
    }
}

