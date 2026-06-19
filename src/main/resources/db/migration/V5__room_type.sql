ALTER TABLE chat_room
    ADD COLUMN IF NOT EXISTS room_type VARCHAR(10) NOT NULL DEFAULT 'DIRECT' AFTER room_name;

-- 기존 데이터: 멤버 3명 이상 → 그룹, 2명이면 방 이름이 "닉네임 · 닉네임" 패턴이 아닐 때 그룹으로 추정
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
