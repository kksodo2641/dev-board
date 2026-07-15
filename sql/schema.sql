-- dev-board schema
-- MySQL 8.0.43
-- Initial version

create table member
(
    member_id     bigint primary key auto_increment,
    email         varchar(255) not null unique,
    password_hash varchar(255) not null,
    nickname      varchar(30)  not null unique,
    gender        varchar(10)  not null default 'NONE' check (gender in ('MALE', 'FEMALE', 'NONE')),
    role          varchar(10)  not null default 'USER' check (role in ('USER', 'ADMIN')),
    status        varchar(20)  not null default 'ACTIVE' check (status in ('ACTIVE', 'DELETED')),
    created_at    DATETIME     not null,
    updated_at    DATETIME     not null,

    constraint chk_member_nickname_length
        CHECK ((char_length(nickname) between 2 and 30))
);

-- ------------------------------------------------

create table board
(
    board_id   bigint primary key auto_increment,
    member_id  bigint       not null,
    title      varchar(100) not null,
    content    text         not null,
    category   varchar(20)  not null check (category in ('NOTICE', 'FREE', 'QNA', 'STUDY', 'JOB')),
    view_count int          not null default 0,
    status     varchar(20)  not null default 'ACTIVE' check (status in ('ACTIVE', 'DELETED')),
    created_at datetime     not null,
    updated_at datetime     not null,

    constraint fk_board_member
        foreign key (member_id)
            references member (member_id)
);

-- ----------------------------------------------------

create table comment
(
    comment_id bigint primary key auto_increment,
    member_id  bigint      not null,
    board_id   bigint      not null,
    parent_id  bigint null,
    content    text        not null,
    status     varchar(20) not null default 'ACTIVE' check (status in ('ACTIVE', 'DELETED')),
    created_at datetime    not null,
    updated_at datetime    not null,

    constraint fk_comment_member
        foreign key (member_id)
            references member (member_id),

    constraint fk_comment_board
        foreign key (board_id)
            references board (board_id),

    constraint fk_comment_parent
        foreign key (parent_id)
            references comment (comment_id)
);

-- ----------------------------------------------------

create table board_like
(
    board_like_id bigint primary key auto_increment,
    member_id     bigint   not null,
    board_id      bigint   not null,
    created_at    datetime not null,
    updated_at    datetime not null,

    constraint fk_board_like_member
        foreign key (member_id)
            references member (member_id),

    constraint fk_board_like_board
        foreign key (board_id)
            references board (board_id),

    constraint uk_board_like_board_member
        unique (board_id, member_id)
);

-- ----------------------------------------------------

create table upload_file
(
    upload_file_id     bigint primary key auto_increment,
    board_id           bigint       not null,
    original_file_name varchar(255) not null,
    stored_file_name   varchar(255) not null,
    file_size          bigint       not null,
    created_at         datetime     not null,
    updated_at         datetime     not null,

    constraint fk_upload_file_board
        foreign key (board_id)
            references board (board_id)
);
