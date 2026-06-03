// macOS-only stubs for sqlite3 entry points that the androidx.sqlite
// `sqlite-framework` cinterop registry references but Apple's system
// libsqlite3 does not export (debug-only / SQLITE_ENABLE_UNLOCK_NOTIFY /
// Windows-only). They are never invoked on macOS — Room runs on the bundled
// SQLite driver at runtime — so these are inert link-time placeholders.
//
// This file is a member of the `macApp` target ONLY (excluded from `iosApp`,
// whose system libsqlite3 / dead-stripping resolves the same references).

int sqlite3_mutex_held(void *p) {
    (void)p;
    return 1;
}

int sqlite3_mutex_notheld(void *p) {
    (void)p;
    return 1;
}

int sqlite3_unlock_notify(void *db, void (*cb)(void **, int), void *arg) {
    (void)db;
    (void)cb;
    (void)arg;
    return 0; // SQLITE_OK
}

int sqlite3_win32_set_directory(unsigned long type, void *zValue) {
    (void)type;
    (void)zValue;
    return 0;
}

int sqlite3_win32_set_directory8(unsigned long type, const char *zValue) {
    (void)type;
    (void)zValue;
    return 0;
}

int sqlite3_win32_set_directory16(unsigned long type, const void *zValue) {
    (void)type;
    (void)zValue;
    return 0;
}
