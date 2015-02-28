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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;


/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public abstract class MessageContent {
    public static String AUTHORITY = "android.pattern.provider";
    // The notifier authority is used to send notifications regarding changes to messages (insert,
    // delete, or update) and is intended as an optimization for use by clients of message list
    // cursors (initially, the email AppWidget).
    public static final String NOTIFIER_AUTHORITY = "com.android.email.notifier";

    public static final String PARAMETER_LIMIT = "limit";

    public static final Uri CONTENT_NOTIFIER_URI = Uri.parse("content://" + NOTIFIER_AUTHORITY);

    public static final String PROVIDER_PERMISSION = "com.android.email.permission.ACCESS_PROVIDER";

    // All classes share this
    public static final String RECORD_ID = "_id";

    public static final String[] COUNT_COLUMNS = new String[]{"count(*)"};

    /**
     * This projection can be used with any of the MessageContent classes, when all you need
     * is a list of id's.  Use ID_PROJECTION_COLUMN to access the row data.
     */
    public static final String[] ID_PROJECTION = new String[] {
        RECORD_ID
    };
    public static final int ID_PROJECTION_COLUMN = 0;

    public static final String ID_SELECTION = RECORD_ID + " =?";

    public static final String FIELD_COLUMN_NAME = "field";
    public static final String ADD_COLUMN_NAME = "add";
    public static final String SET_COLUMN_NAME = "set";

    // Newly created objects get this id
    public static final int NOT_SAVED = -1;
    // The base Uri that this piece of content came from
    public Uri mBaseUri;
    // Lazily initialized uri for this Content
    private Uri mUri = null;
    // The id of the Content
    public long mId = NOT_SAVED;

    // Write the Content into a ContentValues container
    public abstract ContentValues toContentValues();
    // Read the Content from a ContentCursor
    public abstract void restore (Cursor cursor);

    // The Uri is lazily initialized
    public Uri getUri() {
        if (mUri == null) {
            mUri = ContentUris.withAppendedId(mBaseUri, mId);
        }
        return mUri;
    }

    public boolean isSaved() {
        return mId != NOT_SAVED;
    }


    /**
     * Restore a subclass of MessageContent from the database
     * @param context the caller's context
     * @param klass the class to restore
     * @param contentUri the content uri of the MessageContent subclass
     * @param contentProjection the content projection for the MessageContent subclass
     * @param id the unique id of the object
     * @return the instantiated object
     */
    public static <T extends MessageContent> T restoreContentWithId(Context context,
            Class<T> klass, Uri contentUri, String[] contentProjection, long id) {
        long token = Binder.clearCallingIdentity();
        Uri u = ContentUris.withAppendedId(contentUri, id);
        Cursor c = context.getContentResolver().query(u, contentProjection, null, null, null);
        /*if (c == null) throw new ProviderUnavailableException();*/ if (c == null) {
            return null;
        }

        try {
            if (c.moveToFirst()) {
                return getContent(c, klass);
            } else {
                return null;
            }
        } finally {
            c.close();
            Binder.restoreCallingIdentity(token);
        }
    }


    // The Content sub class must have a no-arg constructor
    static public <T extends MessageContent> T getContent(Cursor cursor, Class<T> klass) {
        try {
            T content = klass.newInstance();
            content.mId = cursor.getLong(0);
            content.restore(cursor);
            return content;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Uri save(Context context) {
        if (isSaved()) {
            throw new UnsupportedOperationException();
        }
        Uri res = context.getContentResolver().insert(mBaseUri, toContentValues());
        mId = (null != res) ? Long.parseLong(res.getPathSegments().get(1)) : NOT_SAVED;
        return res;
    }

    public int update(Context context, ContentValues contentValues) {
        if (!isSaved()) {
            throw new UnsupportedOperationException();
        }
        return context.getContentResolver().update(getUri(), contentValues, null, null);
    }

    static public int update(Context context, Uri baseUri, long id, ContentValues contentValues) {
        return context.getContentResolver()
                .update(ContentUris.withAppendedId(baseUri, id), contentValues, null, null);
    }

    static public int delete(Context context, Uri baseUri, long id) {
        return context.getContentResolver()
                .delete(ContentUris.withAppendedId(baseUri, id), null, null);
    }

    static public Uri uriWithLimit(Uri uri, int limit) {
        return uri.buildUpon().appendQueryParameter(MessageContent.PARAMETER_LIMIT,
                Integer.toString(limit)).build();
    }

    /**
     * no public constructor since this is a utility class
     */
    protected MessageContent() {
    }

    public interface MessageColumns {
        public static final String ID = "_id";

        public static final String MESSAGE_ID = "messageID";
        public static final String UUID = "uuid";
        public static final String TYPE = "type";
        public static final String DATE = "date";
        public static final String STATUS = "status";
        public static final String TO_USER_ACCOUNT = "toUserAccount";
        public static final String TEXT = "text";
        public static final String MEDIA_OBJECT = "mediaObject";
        public static final String FROM_USER_ACCOUNT = "fromUserAccount";
        public static final String MEDIA_OBJECT_SOURCE = "mediaObject_source";
        public static final String CHAT_ID = "chatid";
        public static final String SEND_SUCCESS = "send_success";
        public static final String READED = "readed";
    }
}
