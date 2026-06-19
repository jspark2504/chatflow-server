SET @column_exists = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'chat_room'
    AND column_name = 'room_type'
);

SET @sql = IF(@column_exists = 0,
  'ALTER TABLE chat_room ADD COLUMN room_type VARCHAR(10) NOT NULL DEFAULT ''DIRECT'' AFTER room_name',
  'SELECT 1'
);

PREPARE add_column_stmt FROM @sql;
EXECUTE add_column_stmt;
DEALLOCATE PREPARE add_column_stmt;

UPDATE chat_room r
SET r.room_type = 'GROUP'
WHERE r.room_type = 'DIRECT'
  AND (
      (SELECT COUNT(*) FROM chat_room_member m WHERE m.room_id = r.id) > 2
      OR (
          (SELECT COUNT(*) FROM chat_room_member m WHERE m.room_id = r.id) = 2
          AND r.room_name NOT LIKE '% · %'
      )
  );
