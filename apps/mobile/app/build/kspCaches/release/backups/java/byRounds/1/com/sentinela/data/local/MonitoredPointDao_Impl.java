package com.sentinela.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MonitoredPointDao_Impl implements MonitoredPointDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MonitoredPointEntity> __insertionAdapterOfMonitoredPointEntity;

  private final EntityDeletionOrUpdateAdapter<MonitoredPointEntity> __deletionAdapterOfMonitoredPointEntity;

  private final EntityDeletionOrUpdateAdapter<MonitoredPointEntity> __updateAdapterOfMonitoredPointEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateAlertState;

  public MonitoredPointDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMonitoredPointEntity = new EntityInsertionAdapter<MonitoredPointEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `monitored_points` (`id`,`name`,`latitude`,`longitude`,`radiusKm`,`inAlert`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MonitoredPointEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindDouble(3, entity.getLatitude());
        statement.bindDouble(4, entity.getLongitude());
        statement.bindDouble(5, entity.getRadiusKm());
        final int _tmp = entity.getInAlert() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__deletionAdapterOfMonitoredPointEntity = new EntityDeletionOrUpdateAdapter<MonitoredPointEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `monitored_points` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MonitoredPointEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfMonitoredPointEntity = new EntityDeletionOrUpdateAdapter<MonitoredPointEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `monitored_points` SET `id` = ?,`name` = ?,`latitude` = ?,`longitude` = ?,`radiusKm` = ?,`inAlert` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MonitoredPointEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindDouble(3, entity.getLatitude());
        statement.bindDouble(4, entity.getLongitude());
        statement.bindDouble(5, entity.getRadiusKm());
        final int _tmp = entity.getInAlert() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateAlertState = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE monitored_points SET inAlert = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final MonitoredPointEntity entity,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMonitoredPointEntity.insertAndReturnId(entity);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final MonitoredPointEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMonitoredPointEntity.handle(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final MonitoredPointEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMonitoredPointEntity.handle(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateAlertState(final long id, final boolean inAlert,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateAlertState.acquire();
        int _argIndex = 1;
        final int _tmp = inAlert ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateAlertState.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MonitoredPointEntity>> getAll() {
    final String _sql = "SELECT * FROM monitored_points ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"monitored_points"}, new Callable<List<MonitoredPointEntity>>() {
      @Override
      @NonNull
      public List<MonitoredPointEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRadiusKm = CursorUtil.getColumnIndexOrThrow(_cursor, "radiusKm");
          final int _cursorIndexOfInAlert = CursorUtil.getColumnIndexOrThrow(_cursor, "inAlert");
          final List<MonitoredPointEntity> _result = new ArrayList<MonitoredPointEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MonitoredPointEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final double _tmpRadiusKm;
            _tmpRadiusKm = _cursor.getDouble(_cursorIndexOfRadiusKm);
            final boolean _tmpInAlert;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfInAlert);
            _tmpInAlert = _tmp != 0;
            _item = new MonitoredPointEntity(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpRadiusKm,_tmpInAlert);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final long id,
      final Continuation<? super MonitoredPointEntity> $completion) {
    final String _sql = "SELECT * FROM monitored_points WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MonitoredPointEntity>() {
      @Override
      @Nullable
      public MonitoredPointEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRadiusKm = CursorUtil.getColumnIndexOrThrow(_cursor, "radiusKm");
          final int _cursorIndexOfInAlert = CursorUtil.getColumnIndexOrThrow(_cursor, "inAlert");
          final MonitoredPointEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final double _tmpRadiusKm;
            _tmpRadiusKm = _cursor.getDouble(_cursorIndexOfRadiusKm);
            final boolean _tmpInAlert;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfInAlert);
            _tmpInAlert = _tmp != 0;
            _result = new MonitoredPointEntity(_tmpId,_tmpName,_tmpLatitude,_tmpLongitude,_tmpRadiusKm,_tmpInAlert);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
