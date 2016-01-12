CREATE TABLE stories (
id SERIAL PRIMARY KEY,
title varchar,
link varchar,
imageurl varchar,
summary text,
dayOfCommit timestamp
);

CREATE TABLE opinions (
id SERIAL PRIMARY KEY,
story integer REFERENCES stories,
content text
);


INSERT INTO stories
  (id, title, link, imageurl, summary, dayOfCommit)
  VALUES (
    DEFAULT,
    'Story 1: Does salami really float?',
    'www.example.com',
    'http://1.bp.blogspot.com/-aOxDnC3RSrI/TaLU0qlpcFI/AAAAAAAACNU/pisguSiYW7k/s1600/zebra.jpg',
    'We interviewed this zebra about salami.  It knew nothing.',
    now()
);
