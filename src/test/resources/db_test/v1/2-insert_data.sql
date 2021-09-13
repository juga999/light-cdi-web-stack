INSERT INTO user(id, login, password, salt, permissions)
VALUES (
  'A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A00', 'admin',
  X'bc280017ebffb57ec8036bc39108c2bba4249b66af8aa677545bb447e32d0516',
  X'23b74cceca0f9ab0d82c4c725a1e6f67',
  '{"permissions": ["CAN_ADMIN"]}');

INSERT INTO user(id, login, password, salt, permissions)
VALUES (
  'A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A01', 'guest',
  X'bc280017ebffb57ec8036bc39108c2bba4249b66af8aa677545bb447e32d0516',
  X'23b74cceca0f9ab0d82c4c725a1e6f67',
  '{"permissions": ["NONE"]}');
