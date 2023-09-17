insert into "user" (username, password, name, email, activated, created_date, modified_date) values ('admin', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', '관리자', 'dizzyfox734@gmail.com', 1, NOW(), NOW());
--insert into "user" (username, password, activated, created_date, modified_date) values ('user', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 1, NOW(), NOW());

insert into authority (name) values ('ROLE_USER');
insert into authority (name) values ('ROLE_ADMIN');

insert into user_authority (user_id, authority_name) values (1, 'ROLE_ADMIN');
insert into user_authority (user_id, authority_name) values (1, 'ROLE_USER');
--insert into user_authority (user_id, authority_name) values (2, 'ROLE_USER');