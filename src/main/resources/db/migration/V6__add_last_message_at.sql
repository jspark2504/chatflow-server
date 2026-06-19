SET @column_exists = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'chat_room'
    AND column_name = 'last_message_at'
);

SET @sql = IF(@column_exists = 0,
  'ALTER TABLE chat_room ADD COLUMN last_message_at TIMESTAMP(3) NULL DEFAULT NULL AFTER room_type',
  'SELECT 1'
);

PREPARE add_column_stmt FROM @sql;
EXECUTE add_column_stmt;
DEALLOCATE PREPARE add_column_stmt;
