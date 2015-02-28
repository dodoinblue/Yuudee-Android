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
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.pattern.provider.ContentCache.CacheToken;
import android.pattern.util.Utility;
import android.util.Log;

/**
 * Created by 郑志佳 on 1/23/15.
 * QQ: 34168035  Mail: statesman.zheng@gmail.com
 */
public class MessageProvider extends ContentProvider {

    private static final String TAG = Utility.getTAG(MessageProvider.class);

    protected static final String DATABASE_NAME = "MessageProvider.db";

    public static final Uri INTEGRITY_CHECK_URI =
        Uri.parse("content://" + MessageContent.AUTHORITY + "/integrityCheck");

    /** Appended to the notification URI for delete operations */
    public static final String NOTIFICATION_OP_DELETE = "delete";
    /** Appended to the notification URI for insert operations */
    public static final String NOTIFICATION_OP_INSERT = "insert";
    /** Appended to the notification URI for update operations */
    public static final String NOTIFICATION_OP_UPDATE = "update";

    // This is not a hard limit on accounts, per se, but beyond this, we can't guarantee that all
    // critical mailboxes, host auth's, accounts, and policies are cached
    private static final int MAX_CACHED_ACCOUNTS = 16;

    private final ContentCache mCacheMessage =
        new ContentCache("Message", Message.CONTENT_PROJECTION, MAX_CACHED_ACCOUNTS);

    private static final int MESSAGE_BASE = 0x7000;
    private static final int MESSAGE = MESSAGE_BASE;
    private static final int MESSAGE_ID = MESSAGE_BASE + 1;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final String MESSAGE_URI_PARAMETER_MAILBOX_ID = "mailboxId";

    static {
        // Message URI matching table
        UriMatcher matcher = sURIMatcher;

        matcher.addURI(MessageContent.AUTHORITY, "message", MESSAGE);
        matcher.addURI(MessageContent.AUTHORITY, "message/#", MESSAGE_ID);
    }

    /**
     * Wrap the UriMatcher call so we can throw a runtime exception if an unknown Uri is passed in
     * @param uri the Uri to match
     * @return the match value
     */
    private static int findMatch(Uri uri, String methodName) {
        int match = sURIMatcher.match(uri);
        if (match < 0) {
            throw new IllegalArgumentException("Unknown uri: " + uri);
        } else/* if (Logging.LOGD)*/ {
            Log.v(TAG, methodName + ": uri=" + uri + ", match is " + match);
        }
        return match;
    }

    private SQLiteDatabase mDatabase;

    public static Uri uiUri(String type, long id) {
        return Uri.parse(uiUriString(type, id));
    }

    public static String uiUriString(String type, long id) {
        return "content://" + MessageContent.AUTHORITY + "/" + type + ((id == -1) ? "" : ("/" + id));
    }

    /**
     * Orphan record deletion utility.  Generates a sqlite statement like:
     *  delete from <table> where <column> not in (select <foreignColumn> from <foreignTable>)
     * @param db the MessageProvider database
     * @param table the table whose orphans are to be removed
     * @param column the column deletion will be based on
     * @param foreignColumn the column in the foreign table whose absence will trigger the deletion
     * @param foreignTable the foreign table
     */
//    @VisibleForTesting
    void deleteUnlinked(SQLiteDatabase db, String table, String column, String foreignColumn,
            String foreignTable) {
        int count = db.delete(table, column + " not in (select " + foreignColumn + " from " +
                foreignTable + ")", null);
        if (count > 0) {
            Log.w(TAG, "Found " + count + " orphaned row(s) in " + table);
        }
    }

//    @VisibleForTesting
    synchronized SQLiteDatabase getDatabase(Context context) {
        // Always return the cached database, if we've got one
        if (mDatabase != null) {
            return mDatabase;
        }

        // Whenever we create or re-cache the databases, make sure that we haven't lost one
        // to corruption
        checkDatabases();

        DBHelper.DatabaseHelper helper = new DBHelper.DatabaseHelper(context, DATABASE_NAME);
        mDatabase = helper.getWritableDatabase();

        preCacheData();
        return mDatabase;
    }

    /**
     * Pre-cache all of the items in a given table meeting the selection criteria
     * @param tableUri the table uri
     * @param baseProjection the base projection of that table
     * @param selection the selection criteria
     */
    private void preCacheTable(Uri tableUri, String[] baseProjection, String selection) {
        Cursor c = query(tableUri, MessageContent.ID_PROJECTION, selection, null, null);
        try {
            while (c.moveToNext()) {
                long id = c.getLong(MessageContent.ID_PROJECTION_COLUMN);
                Cursor cachedCursor = query(ContentUris.withAppendedId(
                        tableUri, id), baseProjection, null, null, null);
                if (cachedCursor != null) {
                    cachedCursor.close();
                }
            }
        } finally {
            c.close();
        }
    }

    private final HashMap<Long, HashMap<Integer, Long>> mMailboxTypeMap =
        new HashMap<Long, HashMap<Integer, Long>>();

    private void preCacheData() {
        synchronized(mMailboxTypeMap) {
            mMailboxTypeMap.clear();

            preCacheTable(Message.CONTENT_URI, Message.CONTENT_PROJECTION, null);
        }
    }

    /*package*/ static SQLiteDatabase getReadableDatabase(Context context) {
        DBHelper.DatabaseHelper helper = new DBHelper.DatabaseHelper(context, DATABASE_NAME);
        return helper.getReadableDatabase();
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = findMatch(uri, "delete");
        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        String id = "0";
        ContentResolver resolver = context.getContentResolver();

        ContentCache cache = mCacheMessage;
        String tableName = Message.TABLE_NAME;
        int result = -1;

        try {
            switch (match) {
                case MESSAGE_ID:
                    id = uri.getPathSegments().get(1);
                    if (cache != null) {
                        cache.lock(id);
                    }
                    try {
                        result = db.delete(tableName, whereWithId(id, selection), selectionArgs);
                        if (cache != null) {
                            switch(match) {
                                case MESSAGE_ID:
                                    cache.invalidate("Delete", uri, selection);
                                    // Make sure all data is properly cached
                                    preCacheData();
                                    break;
                            }
                        }
                    } finally {
                        if (cache != null) {
                            cache.unlock(id);
                        }
                    }
                    break;

                case MESSAGE:
                    cache.invalidate("Delete", uri, selection);
                    result = db.delete(tableName, selection, selectionArgs);
                    // Make sure all data is properly cached
                    preCacheData();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri + " match:" + match);
            }
        } catch (SQLiteException e) {
            checkDatabases();
            throw e;
        }

        // Notify all message content cursors
        resolver.notifyChange(Message.BASE_CONTENT_URI, null);
        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = findMatch(uri, "insert");
        Context context = getContext();
        ContentResolver resolver = context.getContentResolver();

        // See the comment at delete(), above
        SQLiteDatabase db = getDatabase(context);
        long longId;

        Uri resultUri = null;

        try {
            switch (match) {
                case MESSAGE:
                    longId = db.insert(Message.TABLE_NAME, "foo", values);
                    resultUri = ContentUris.withAppendedId(uri, longId);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URL " + uri);
            }
        } catch (SQLiteException e) {
            checkDatabases();
            throw e;
        }

        // Notify all existing cursors.
        resolver.notifyChange(Message.BASE_CONTENT_URI, null);
        return resultUri;
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "MessageProvider oncreate!");
        checkDatabases();
        new DBHelper.DatabaseHelper(getContext(), DATABASE_NAME);
        Log.d(TAG, "db created!");
        return true;
    }

    public void checkDatabases() {
        // Uncache the databases
        if (mDatabase != null) {
            mDatabase = null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Cursor c = null;
        int match;
        try {
            match = findMatch(uri, "query");
        } catch (IllegalArgumentException e) {
            String uriString = uri.toString();
            // If we were passed an illegal uri, see if it ends in /-1
            // if so, and if substituting 0 for -1 results in a valid uri, return an empty cursor
            if (uriString != null && uriString.endsWith("/-1")) {
                uri = Uri.parse(uriString.substring(0, uriString.length() - 2) + "0");
                match = findMatch(uri, "query");
                if (match == MESSAGE_ID) {
                    return new MatrixCursor(projection, 0);
                }
            }
            throw e;
        }
        Context context = getContext();
        // See the comment at delete(), above
        SQLiteDatabase db = getDatabase(context);
        String limit = uri.getQueryParameter(MessageContent.PARAMETER_LIMIT);
        String id;

        // Find the cache for this query's table (if any)
        ContentCache cache = null;
        String tableName = Message.TABLE_NAME;
        // We can only use the cache if there's no selection
        if (selection == null) {
            cache = mCacheMessage;
        }
        if (cache == null) {
            ContentCache.notCacheable(uri, selection);
        }

        try {
            switch (match) {
                case MESSAGE:
                    c = db.query(tableName, projection,
                            selection, selectionArgs, null, null, sortOrder, limit);
                    break;
                case MESSAGE_ID:
                    id = uri.getPathSegments().get(1);
                    if (cache != null) {
                        c = cache.getCachedCursor(id, projection);
                    }
                    if (c == null) {
                        CacheToken token = null;
                        if (cache != null) {
                            token = cache.getCacheToken(id);
                        }
                        c = db.query(tableName, projection, whereWithId(id, selection),
                                selectionArgs, null, null, sortOrder, limit);
                        if (cache != null) {
                            c = cache.putCursor(c, id, projection, token);
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (SQLiteException e) {
            Log.d(TAG, "SQLiteException");
            checkDatabases();
            throw e;
        } catch (RuntimeException e) {
            Log.d(TAG, "RuntimeException");
            checkDatabases();
            e.printStackTrace();
            throw e;
        } finally {
            if (c == null) {
                // This should never happen, but let's be sure to log it...
                Log.e(TAG, "Query returning null for uri: " + uri + ", selection: " + selection);
            }
        }

        if ((c != null) && !isTemporary()) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    private String whereWithId(String id, String selection) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("_id=");
        sb.append(id);
        if (selection != null) {
            sb.append(" AND (");
            sb.append(selection);
            sb.append(')');
        }
        return sb.toString();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Handle this special case the fastest possible way
        if (uri == INTEGRITY_CHECK_URI) {
            checkDatabases();
            return 0;
        }

        // Notify all existing cursors, except for ACCOUNT_RESET_NEW_COUNT(_ID)
        Uri notificationUri = Message.BASE_CONTENT_URI;

        int match = findMatch(uri, "update");
        Context context = getContext();
        ContentResolver resolver = context.getContentResolver();
        // See the comment at delete(), above
        SQLiteDatabase db = getDatabase(context);
        int result;

        ContentCache cache = mCacheMessage;
        String tableName = Message.TABLE_NAME;
        String id = "0";

        try {
            switch (match) {
                case MESSAGE_ID:
                    id = uri.getPathSegments().get(1);
                    if (cache != null) {
                        cache.lock(id);
                    }
                    try {
                        result = db.update(tableName, values, whereWithId(id, selection),
                                selectionArgs);
                    } catch (SQLiteException e) {
                        // Null out values (so they aren't cached) and re-throw
                        values = null;
                        throw e;
                    } finally {
                        if (cache != null) {
                            cache.unlock(id, values);
                        }
                    }
                    break;
                case MESSAGE:
                    Cursor c = db.query(tableName, MessageContent.ID_PROJECTION,
                            selection, selectionArgs, null, null, null);
                    db.beginTransaction();
                    result = 0;
                    try {
                        while (c.moveToNext()) {
                            update(ContentUris.withAppendedId(
                                        uri, c.getLong(MessageContent.ID_PROJECTION_COLUMN)),
                                    values, null, null);
                            result++;
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                        c.close();
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (SQLiteException e) {
            checkDatabases();
            throw e;
        }

        resolver.notifyChange(notificationUri, null);
        return result;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        db.beginTransaction();
        try {
            ContentProviderResult[] results = super.applyBatch(operations);
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }
}
