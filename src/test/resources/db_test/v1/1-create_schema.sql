CREATE TABLE db_version
(
  version integer NOT NULL
);

CREATE TABLE user
(
  id uuid PRIMARY KEY,
  login varchar NOT NULL UNIQUE,
  password binary NOT NULL,
  salt binary NOT NULL,
  permissions text NOT NULL
);

INSERT INTO db_version(version) VALUES(1);
