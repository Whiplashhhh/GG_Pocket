-- Push notifications (D-128, D-130): the devices a member turned them on for, and what they want.
create table push_subscription (
    id          uuid           primary key,
    member_id   uuid           not null references member (id) on delete cascade,
    -- Given by the browser's push service (Apple, Google, Mozilla); one device, one endpoint.
    endpoint    varchar(1000)  not null unique,
    p256dh      varchar(120)   not null,
    auth        varchar(40)    not null,
    created_at  timestamptz    not null,
    updated_at  timestamptz    not null
);

create index push_subscription_member_idx on push_subscription (member_id);

-- No row: the defaults (comments, answers and friend requests on; reactions and friends' sessions off).
create table push_preference (
    member_id        uuid     primary key references member (id) on delete cascade,
    comments         boolean  not null,
    replies          boolean  not null,
    reactions        boolean  not null,
    friend_sessions  boolean  not null,
    friend_requests  boolean  not null
);
