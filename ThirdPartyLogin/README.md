# ShareSDK for Android
- website -- http://www.mob.com
- wiki -- http://wiki.mob.com/Android_%E5%BF%AB%E9%80%9F%E9%9B%86%E6%88%90%E6%8C%87%E5%8D%97
- bbs -- http://bbs.mob.com/forum-36-1.html

# Step One: Download the SDK

Visit our [official website](http://www.mob.com/) and download the latest version of ShareSDK. After extracting the downloaded file, you will find the following directory structure：

![directory structure](http://wiki.sharesdk.cn/images/7/71/wiki_and_fi_1.png)

Open the **ShareSDK for Android** directory, you will find **MainLibs** and **OnekeyShare**. ShareSDK is stored in the MainLibs directory, and OnekeyShare is a GUI tool for developers to quickly complete the share feature by ShareSDK.

# Step Two: Import ShareSDK to Your Project

There are two ways to import ShareSDK into your project: **reference to the ShareSDK project** or **copy the jars and resources into your project**. If you select the second way, we provide the following tool to help you quickly finish these operations:

![quick integrating tool](http://wiki.sharesdk.cn/images/d/d6/ssdk_qig_qi_win.png)

Execute this tool and copy its products into your project.

ShareSDK encourage you integrate ShareSDK by referencing its project, because this will be much simpler. Here are the steps:

(1) Copy the extracted SDK into your workspace of Eclipse

(2) Import the SDK projects:

![projects of ShareSDK](http://wiki.sharesdk.cn/images/6/64/p1.png)

Select MainLibs and OnekeyShare

![select lib-projects](http://wiki.sharesdk.cn/images/0/0f/p2.png)

(3) Change dependency of your project to OnekeyShare (if you need this GUI tool) or MainLibs

![change dependency](http://wiki.sharesdk.cn/images/thumb/2/2d/p3.png/534px-p3.png)

# Step Three: Add Applications Information

There are three ways to add your applications information into ShareSDK: **register on the application console of ShareSDK, configurate the “assets/ShareSDK.xml” file, or modify by ShareSDK.setPlatformDevInfo(String, HashMap<String, Object>) method at runtime.**

Here is the example of “assets/ShareSDK.xml” way:

```` xml
<ShareSDK
   AppKey="add appkey you got from SahreSDK here" />

<Facebook
    Id="int field, custom value for developer to recognize this platform"
    SortId="int field, the priority in the registered platforms"
    ConsumerKey="consumer key you got from Facebook"
    ConsumerSecret="consumer secret you got from Facebook"
    Enable="Boolean field, false means to remove the platform from the registered platforms" />
```

All applications information is registered in the “assets/ShareSDK.xml” of ShareSDK Sample project.

# Step Four: Configurate AndroidManifest.xml

Add the following permissions into your AndroidMenifest.xml:

```` xml
<uses-permission android:name="android.permission.GET_TASKS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.MANAGE_ACCOUNTS"/>
<uses-permission android:name="android.permission.GET_ACCOUNTS"/>
```
You should add the intent-filter in your  launchActivity if you want to use the KakaoTalk to share msg.
<!--
	If you share msg in KakaoTalk, your share-params of executeUrl should set the value "kakaoTalkTest://starActivity"
	So it do, when the user to click the share-msg, then startActivity of your app's launch-activity. 
	When you use the lib of onekeyshare, you can use the method of 
    setExecuteUrl("kakaoTalkTest://starActivity") to set executeUrl.
-->
    <intent-filter>
        <data android:scheme="kakaoTalkTest" android:host="starActivity"/>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.BROWSABLE" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
```
		
And the single Activity for GUIs of ShareSDK:

```` xml
<activity
   android:name="cn.sharesdk.framework.ShareSDKUIShell"
   android:theme="@android:style/Theme.Translucent.NoTitleBar"
   android:configChanges="keyboardHidden|orientation|screenSize"
   android:screenOrientation="portrait"
   android:windowSoftInputMode="stateHidden|adjustResize" />
```

If you integrate Wechat, add this callback activity:

```` xml
<activity
   android:name=".wxapi.WXEntryActivity"
   android:theme="@android:style/Theme.Translucent.NoTitleBar"
   android:configChanges="keyboardHidden|orientation|screenSize"
   android:exported="true"
   android:screenOrientation="portrait" />
```

And if you integrate Yixin, add this callback activity:

```` xml
<activity
   android:name=".yxapi.YXEntryActivity"
   android:theme="@android:style/Theme.Translucent.NoTitleBar"
   android:configChanges="keyboardHidden|orientation|screenSize"
   android:exported="true"
   android:screenOrientation="portrait" />
```

# Step Five: Add Codes

Add the following line in the **onCreate** method of **the entrance activity**:

````java
ShareSDK.initSDK(this);
```

And add the following line int the **onDestroy** method of **the last activity**:

````java
ShareSDK.stopSDK(this);
```

# Screenshots
![logo grid view of onekeyshare](http://wiki.sharesdk.cn/images/thumb/a/ad/p4.png/337px-p4.png)
![edit page of onekeyshare](http://wiki.sharesdk.cn/images/thumb/b/b1/p5.png/337px-p5.png)
![image preview](http://wiki.sharesdk.cn/images/thumb/8/88/p6.png/337px-p6.png)
![authorizes](http://wiki.sharesdk.cn/images/thumb/6/69/p7.png/337px-p7.png)

# And the Next

For more information about how to integrate ShareSDK or how use ShareSDK to get your friends list, following someone, share statuses, etc. please visit our [official wiki](http://wiki.sharesdk.cn/Android_%E5%BF%AB%E9%80%9F%E9%9B%86%E6%88%90%E6%8C%87%E5%8D%97).
