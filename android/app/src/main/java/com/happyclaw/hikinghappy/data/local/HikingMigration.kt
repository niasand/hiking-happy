package com.happyclaw.hikinghappy.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // track_sessions table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS track_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                activityType TEXT NOT NULL,
                location TEXT,
                startTime INTEGER NOT NULL,
                endTime INTEGER,
                totalDistance REAL NOT NULL DEFAULT 0.0,
                totalDuration INTEGER NOT NULL DEFAULT 0,
                maxAltitude REAL NOT NULL DEFAULT 0.0,
                minAltitude REAL NOT NULL DEFAULT 0.0,
                maxSpeed REAL NOT NULL DEFAULT 0.0,
                pointCount INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // track_points table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS track_points (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                sessionId INTEGER NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                altitude REAL NOT NULL,
                speed REAL NOT NULL,
                accuracy REAL,
                timestamp INTEGER NOT NULL,
                FOREIGN KEY (sessionId) REFERENCES track_sessions(id) ON DELETE CASCADE
            )
        """.trimIndent())

        // Indices for track_points
        database.execSQL("CREATE INDEX IF NOT EXISTS index_track_points_sessionId ON track_points (sessionId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_track_points_sessionId_timestamp ON track_points (sessionId, timestamp)")
    }
}
