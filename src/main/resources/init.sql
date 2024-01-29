create table if not exists public.deleted_files (
    delete_date timestamp(6),
    id bigserial not null,
    user_id bigint unique,
    file_name varchar(255),
    original_file_path varchar(255),
    primary key (id));
create table if not exists public.fileshare_users (
    id bigserial not null,
    name varchar(255) not null,
    primary key (id));

alter table public.deleted_files drop constraint if exists user_constraint;
alter table if exists public.deleted_files add constraint user_constraint foreign key (user_id) references public.fileshare_users;