package com.happyclaw.hikinghappy.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.happyclaw.hikinghappy.data.local.dao.ActivityRecordDao;
import com.happyclaw.hikinghappy.data.local.dao.ActivityRecordDao_Impl;
import com.happyclaw.hikinghappy.data.local.dao.TrackPointDao;
import com.happyclaw.hikinghappy.data.local.dao.TrackPointDao_Impl;
import com.happyclaw.hikinghappy.data.local.dao.TrackSessionDao;
import com.happyclaw.hikinghappy.data.local.dao.TrackSessionDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HikingDatabase_Impl extends HikingDatabase {
  private volatile ActivityRecordDao _activityRecordDao;

  private volatile TrackSessionDao _trackSessionDao;

  private volatile TrackPointDao _trackPointDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `activity_record` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `altitude` REAL NOT NULL, `speed` REAL NOT NULL, `type` TEXT NOT NULL, `location` TEXT, `timestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_activity_record_timestamp` ON `activity_record` (`timestamp`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `track_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `activityType` TEXT NOT NULL, `location` TEXT, `startTime` INTEGER NOT NULL, `endTime` INTEGER, `totalDistance` REAL NOT NULL, `totalDuration` INTEGER NOT NULL, `maxAltitude` REAL NOT NULL, `minAltitude` REAL NOT NULL, `maxSpeed` REAL NOT NULL, `pointCount` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `track_points` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `altitude` REAL NOT NULL, `speed` REAL NOT NULL, `accuracy` REAL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`sessionId`) REFERENCES `track_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_track_points_sessionId` ON `track_points` (`sessionId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_track_points_sessionId_timestamp` ON `track_points` (`sessionId`, `timestamp`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '27b25ed90d0f4cfcc9fbc6ee1223e8e3')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `activity_record`");
        db.execSQL("DROP TABLE IF EXISTS `track_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `track_points`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsActivityRecord = new HashMap<String, TableInfo.Column>(6);
        _columnsActivityRecord.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActivityRecord.put("altitude", new TableInfo.Column("altitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActivityRecord.put("speed", new TableInfo.Column("speed", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActivityRecord.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActivityRecord.put("location", new TableInfo.Column("location", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActivityRecord.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysActivityRecord = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesActivityRecord = new HashSet<TableInfo.Index>(1);
        _indicesActivityRecord.add(new TableInfo.Index("index_activity_record_timestamp", true, Arrays.asList("timestamp"), Arrays.asList("ASC")));
        final TableInfo _infoActivityRecord = new TableInfo("activity_record", _columnsActivityRecord, _foreignKeysActivityRecord, _indicesActivityRecord);
        final TableInfo _existingActivityRecord = TableInfo.read(db, "activity_record");
        if (!_infoActivityRecord.equals(_existingActivityRecord)) {
          return new RoomOpenHelper.ValidationResult(false, "activity_record(com.happyclaw.hikinghappy.data.local.entity.ActivityRecord).\n"
                  + " Expected:\n" + _infoActivityRecord + "\n"
                  + " Found:\n" + _existingActivityRecord);
        }
        final HashMap<String, TableInfo.Column> _columnsTrackSessions = new HashMap<String, TableInfo.Column>(11);
        _columnsTrackSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackSessions.put("activityType", new TableInfo.Column("activityType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackSessions.put("location", new TableInfo.Column("location", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackSessions.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackSessions.put("endTime", new TableInfo.Column("endTime", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackSessions.put("totalDistance", new TableInfo.Column("totalDistance", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackSessions.put("totalDuration", new TableInfo.Column("totalDuration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackSessions.put("maxAltitude", new TableInfo.Column("maxAltitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackSessions.put("minAltitude", new TableInfo.Column("minAltitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackSessions.put("maxSpeed", new TableInfo.Column("maxSpeed", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackSessions.put("pointCount", new TableInfo.Column("pointCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTrackSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTrackSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTrackSessions = new TableInfo("track_sessions", _columnsTrackSessions, _foreignKeysTrackSessions, _indicesTrackSessions);
        final TableInfo _existingTrackSessions = TableInfo.read(db, "track_sessions");
        if (!_infoTrackSessions.equals(_existingTrackSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "track_sessions(com.happyclaw.hikinghappy.data.local.entity.TrackSession).\n"
                  + " Expected:\n" + _infoTrackSessions + "\n"
                  + " Found:\n" + _existingTrackSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsTrackPoints = new HashMap<String, TableInfo.Column>(8);
        _columnsTrackPoints.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("sessionId", new TableInfo.Column("sessionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("altitude", new TableInfo.Column("altitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("speed", new TableInfo.Column("speed", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("accuracy", new TableInfo.Column("accuracy", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTrackPoints = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTrackPoints.add(new TableInfo.ForeignKey("track_sessions", "CASCADE", "NO ACTION", Arrays.asList("sessionId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTrackPoints = new HashSet<TableInfo.Index>(2);
        _indicesTrackPoints.add(new TableInfo.Index("index_track_points_sessionId", false, Arrays.asList("sessionId"), Arrays.asList("ASC")));
        _indicesTrackPoints.add(new TableInfo.Index("index_track_points_sessionId_timestamp", false, Arrays.asList("sessionId", "timestamp"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoTrackPoints = new TableInfo("track_points", _columnsTrackPoints, _foreignKeysTrackPoints, _indicesTrackPoints);
        final TableInfo _existingTrackPoints = TableInfo.read(db, "track_points");
        if (!_infoTrackPoints.equals(_existingTrackPoints)) {
          return new RoomOpenHelper.ValidationResult(false, "track_points(com.happyclaw.hikinghappy.data.local.entity.TrackPoint).\n"
                  + " Expected:\n" + _infoTrackPoints + "\n"
                  + " Found:\n" + _existingTrackPoints);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "27b25ed90d0f4cfcc9fbc6ee1223e8e3", "6c42c278336bad34ba7e1af8a786fc1a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "activity_record","track_sessions","track_points");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `activity_record`");
      _db.execSQL("DELETE FROM `track_sessions`");
      _db.execSQL("DELETE FROM `track_points`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ActivityRecordDao.class, ActivityRecordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TrackSessionDao.class, TrackSessionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TrackPointDao.class, TrackPointDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ActivityRecordDao activityRecordDao() {
    if (_activityRecordDao != null) {
      return _activityRecordDao;
    } else {
      synchronized(this) {
        if(_activityRecordDao == null) {
          _activityRecordDao = new ActivityRecordDao_Impl(this);
        }
        return _activityRecordDao;
      }
    }
  }

  @Override
  public TrackSessionDao trackSessionDao() {
    if (_trackSessionDao != null) {
      return _trackSessionDao;
    } else {
      synchronized(this) {
        if(_trackSessionDao == null) {
          _trackSessionDao = new TrackSessionDao_Impl(this);
        }
        return _trackSessionDao;
      }
    }
  }

  @Override
  public TrackPointDao trackPointDao() {
    if (_trackPointDao != null) {
      return _trackPointDao;
    } else {
      synchronized(this) {
        if(_trackPointDao == null) {
          _trackPointDao = new TrackPointDao_Impl(this);
        }
        return _trackPointDao;
      }
    }
  }
}
