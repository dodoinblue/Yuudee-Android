/*
 * Copyright (c) 2015 著作权由郑志佳所有。著作权人保留一切权利。
 *
 * 这份授权条款，在使用者符合以下三条件的情形下，授予使用者使用及再散播本
 * 软件包装原始码及二进位可执行形式的权利，无论此包装是否经改作皆然：
 *
 * * 对于本软件源代码的再散播，必须保留上述的版权宣告、此三条件表列，以
 *   及下述的免责声明。
 * * 对于本套件二进位可执行形式的再散播，必须连带以文件以及／或者其他附
 *   于散播包装中的媒介方式，重制上述之版权宣告、此三条件表列，以及下述
 *   的免责声明。
 * * 未获事前取得书面许可，不得使用郑志佳或本软件贡献者之名称，
 *   来为本软件之衍生物做任何表示支持、认可或推广、促销之行为。
 *
 * 免责声明：本软件是由郑志佳及本软件之贡献者以现状（"as is"）提供，
 * 本软件包装不负任何明示或默示之担保责任，包括但不限于就适售性以及特定目
 * 的的适用性为默示性担保。郑志佳及本软件之贡献者，无论任何条件、
 * 无论成因或任何责任主义、无论此责任为因合约关系、无过失责任主义或因非违
 * 约之侵权（包括过失或其他原因等）而起，对于任何因使用本软件包装所产生的
 * 任何直接性、间接性、偶发性、特殊性、惩罚性或任何结果的损害（包括但不限
 * 于替代商品或劳务之购用、使用损失、资料损失、利益损失、业务中断等等），
 * 不负任何责任，即在该种使用已获事前告知可能会造成此类损害的情形下亦然。
 */

package android.pattern.provider;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.pattern.util.Utility;

/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public final class Message extends MessageContent implements MessageContent.MessageColumns, Parcelable {
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    private static Message mInstance;
    public static final String MESSAGE_INFO = "message_info";
    public static final String CURRENT_SESSION_ID = "message_session_id";

    public static final boolean DEBUG_MESSAGE = false; // DO NOT SUBMIT WITH
    // THIS SET TO TRUE
    public static final String TAG = Utility.getTAG(Message.class);

    public static final String TABLE_NAME = "Message";
    public static final Uri CONTENT_URI = Uri.parse(BASE_CONTENT_URI + "/message");

    public static final int MESSAGE_SEND_SUCCESS = 1;
    public static final int MESSAGE_SEND_FAILED = 0;

    public static final int MESSAGE_READED = 1;
    public static final int MESSAGE_NOT_READED = 0;

    public static final char MESSAGE_STRING_DELIMITER = '\1';

    public byte[] mMediaObject;

    public String mMessageId;
    public String mUuid;
    public int mType;
    public int mDate;
    public int mStatus;
    public String mToUserAccount;
    public String mText;
    public String mMediaObjectSource;
    public String mChatId;

    public Bitmap mAppIconBitmap;
    public String mFromUserAccount;
    public int mSendSuccess;
    public int mReaded;

    public static final String[] CONTENT_PROJECTION = new String[] { RECORD_ID, MessageColumns.MESSAGE_ID,
        MessageColumns.UUID, MessageColumns.TYPE, MessageColumns.MEDIA_OBJECT,
        MessageColumns.DATE, MessageColumns.STATUS, MessageColumns.TO_USER_ACCOUNT,
        MessageColumns.FROM_USER_ACCOUNT, MessageColumns.TEXT,
        MessageColumns.MEDIA_OBJECT_SOURCE, MessageColumns.CHAT_ID, MessageColumns.SEND_SUCCESS,
        MessageColumns.READED };

    public static final Message NO_MESSAGE = new Message();

    public Message() {
    }

    public static synchronized Message getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Message();
        }

        return mInstance;
    }

    public void clear() {
        synchronized (Message.class) {
            mInstance = new Message();
        }
    }

    public static Message restoreMessageWithId(Context context, long id) {
        return MessageContent.restoreContentWithId(context, Message.class, Message.CONTENT_URI,
                Message.CONTENT_PROJECTION, id);
    }

    public static ArrayList<String> addMessageStringToList(String messageString, ArrayList<String> messageList) {
        if (messageString != null) {
            int start = 0;
            int len = messageString.length();
            while (start < len) {
                int end = messageString.indexOf(MESSAGE_STRING_DELIMITER, start);
                if (end > start) {
                    messageList.add(messageString.substring(start, end));
                    start = end + 1;
                } else {
                    break;
                }
            }
        }
        return messageList;
    }

    @Override
    public void restore(Cursor cursor) {
        mBaseUri = CONTENT_URI;
        mId = cursor.getLong(cursor.getColumnIndex(RECORD_ID));
        mMessageId = cursor.getString(cursor.getColumnIndex(MESSAGE_ID));
        mUuid = cursor.getString(cursor.getColumnIndex(UUID));
        mType = cursor.getInt(cursor.getColumnIndex(TYPE));
        mMediaObject = cursor.getBlob(cursor.getColumnIndex(MEDIA_OBJECT));
        mDate = cursor.getInt(cursor.getColumnIndex(DATE));
        mStatus = cursor.getInt(cursor.getColumnIndex(STATUS));
        mText = cursor.getString(cursor.getColumnIndex(TEXT));
        mFromUserAccount = cursor.getString(cursor.getColumnIndex(FROM_USER_ACCOUNT));
        mToUserAccount = cursor.getString(cursor.getColumnIndex(TO_USER_ACCOUNT));
        mMediaObjectSource = cursor.getString(cursor.getColumnIndex(MEDIA_OBJECT_SOURCE));
        mChatId = cursor.getString(cursor.getColumnIndex(CHAT_ID));
        mSendSuccess = cursor.getInt(cursor.getColumnIndex(SEND_SUCCESS));
        mReaded = cursor.getInt(cursor.getColumnIndex(READED));
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(MessageColumns.MESSAGE_ID, mMessageId);
        values.put(MessageColumns.UUID, mUuid);
        values.put(MessageColumns.TYPE, mType);
        values.put(MessageColumns.MEDIA_OBJECT, mMediaObject);
        values.put(MessageColumns.DATE, mDate);
        values.put(MessageColumns.STATUS, mStatus);
        values.put(MessageColumns.FROM_USER_ACCOUNT, mFromUserAccount);
        values.put(MessageColumns.TO_USER_ACCOUNT, mToUserAccount);
        values.put(MessageColumns.MEDIA_OBJECT_SOURCE, mMediaObjectSource);
        values.put(MessageColumns.CHAT_ID, mChatId);
        values.put(MessageColumns.SEND_SUCCESS, mSendSuccess);
        values.put(MessageColumns.READED, mReaded);

        return values;
    }

    /**
     * Supports Parcelable
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Supports Parcelable
     */
    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    /**
     * Supports Parcelable
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // mBaseUri is not parceled
        dest.writeLong(mId);
        dest.writeString(mMessageId);
        dest.writeString(mUuid);
        dest.writeInt(mType);
        dest.writeByteArray(mMediaObject);
        dest.writeInt(mDate);
        dest.writeInt(mStatus);
        dest.writeString(mFromUserAccount);
        dest.writeString(mToUserAccount);
        dest.writeString(mText);
        dest.writeString(mMediaObjectSource);
        dest.writeString(mChatId);
        dest.writeInt(mSendSuccess);
        dest.writeInt(mReaded);
    }

    /**
     * Supports Parcelable
     */
    public Message(Parcel in) {
        mBaseUri = CONTENT_URI;
        mId = in.readLong();
        mMessageId = in.readString();
        mUuid = in.readString();
        mType = in.readInt();
        in.readByteArray(mMediaObject);
        mDate = in.readInt();
        mStatus = in.readInt();
        mFromUserAccount = in.readString();
        mToUserAccount = in.readString();
        mText = in.readString();
        mMediaObjectSource = in.readString();
        mChatId = in.readString();
        mSendSuccess = in.readInt();
        mReaded = in.readInt();
    }
}
