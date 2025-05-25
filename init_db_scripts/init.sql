--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2 (Debian 17.2-1.pgdg120+1)
-- Dumped by pg_dump version 17.2 (Debian 17.2-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: add_match(); Type: FUNCTION; Schema: public; Owner: hedgefo9
--

CREATE FUNCTION public.add_match() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Проверяем, существует ли взаимный лайк
    IF EXISTS (
        SELECT 1
        FROM likes
        WHERE sender_id = NEW.receiver_id AND receiver_id = NEW.sender_id
    ) THEN
        -- Добавляем запись о мэтче
        INSERT INTO matches (user_id1, user_id2, matched_at)
        VALUES (
                   LEAST(NEW.sender_id, NEW.receiver_id),
                   GREATEST(NEW.sender_id, NEW.receiver_id),
                   CURRENT_TIMESTAMP
               )
        ON CONFLICT DO NOTHING; -- Избегаем дублирования
    END IF;

    RETURN NEW;
END;
$$;


ALTER FUNCTION public.add_match() OWNER TO hedgefo9;

--
-- Name: remove_match(); Type: FUNCTION; Schema: public; Owner: hedgefo9
--

CREATE FUNCTION public.remove_match() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Удаляем запись о мэтче, если один из лайков удалён
    DELETE
    FROM matches
    WHERE (user_id1, user_id2) = (
                                  LEAST(OLD.sender_id, OLD.receiver_id),
                                  GREATEST(OLD.sender_id, OLD.receiver_id)
        );

    RETURN OLD;
END;
$$;


ALTER FUNCTION public.remove_match() OWNER TO hedgefo9;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: admins; Type: TABLE; Schema: public; Owner: hedgefo9
--

CREATE TABLE public.admins (
    admin_id integer NOT NULL,
    name character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.admins OWNER TO hedgefo9;

--
-- Name: admins_admin_id_seq; Type: SEQUENCE; Schema: public; Owner: hedgefo9
--

CREATE SEQUENCE public.admins_admin_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.admins_admin_id_seq OWNER TO hedgefo9;

--
-- Name: admins_admin_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hedgefo9
--

ALTER SEQUENCE public.admins_admin_id_seq OWNED BY public.admins.admin_id;


--
-- Name: bios; Type: TABLE; Schema: public; Owner: hedgefo9
--

CREATE TABLE public.bios (
    user_id bigint NOT NULL,
    about_me text,
    looking_for text,
    updated_at timestamp without time zone DEFAULT now()
);


ALTER TABLE public.bios OWNER TO hedgefo9;

--
-- Name: likes; Type: TABLE; Schema: public; Owner: hedgefo9
--

CREATE TABLE public.likes (
    sender_id bigint NOT NULL,
    receiver_id bigint NOT NULL,
    "timestamp" timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_different_users CHECK ((sender_id <> receiver_id))
);


ALTER TABLE public.likes OWNER TO hedgefo9;

--
-- Name: matches; Type: TABLE; Schema: public; Owner: hedgefo9
--

CREATE TABLE public.matches (
    user_id1 bigint NOT NULL,
    user_id2 bigint NOT NULL,
    matched_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT user_order CHECK ((user_id1 < user_id2))
);


ALTER TABLE public.matches OWNER TO hedgefo9;

--
-- Name: messages; Type: TABLE; Schema: public; Owner: hedgefo9
--

CREATE TABLE public.messages (
    message_id bigint NOT NULL,
    sender_id bigint NOT NULL,
    receiver_id bigint NOT NULL,
    content text NOT NULL,
    sent_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_read boolean DEFAULT false
);


ALTER TABLE public.messages OWNER TO hedgefo9;

--
-- Name: messages_message_id_seq; Type: SEQUENCE; Schema: public; Owner: hedgefo9
--

CREATE SEQUENCE public.messages_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.messages_message_id_seq OWNER TO hedgefo9;

--
-- Name: messages_message_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hedgefo9
--

ALTER SEQUENCE public.messages_message_id_seq OWNED BY public.messages.message_id;


--
-- Name: subscriptions; Type: TABLE; Schema: public; Owner: hedgefo9
--

CREATE TABLE public.subscriptions (
    subscription_id bigint NOT NULL,
    user_id bigint NOT NULL,
    start_at timestamp without time zone NOT NULL,
    end_at timestamp without time zone NOT NULL,
    plan_type character varying(50) NOT NULL
);


ALTER TABLE public.subscriptions OWNER TO hedgefo9;

--
-- Name: subscriptions_subscription_id_seq; Type: SEQUENCE; Schema: public; Owner: hedgefo9
--

CREATE SEQUENCE public.subscriptions_subscription_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subscriptions_subscription_id_seq OWNER TO hedgefo9;

--
-- Name: subscriptions_subscription_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hedgefo9
--

ALTER SEQUENCE public.subscriptions_subscription_id_seq OWNED BY public.subscriptions.subscription_id;


--
-- Name: user_photos; Type: TABLE; Schema: public; Owner: hedgefo9
--

CREATE TABLE public.user_photos (
    photo_id bigint NOT NULL,
    user_id bigint NOT NULL,
    file_name character varying(255) NOT NULL,
    uploaded_at timestamp without time zone DEFAULT now(),
    is_primary boolean DEFAULT false
);


ALTER TABLE public.user_photos OWNER TO hedgefo9;

--
-- Name: user_photos_photo_id_seq; Type: SEQUENCE; Schema: public; Owner: hedgefo9
--

CREATE SEQUENCE public.user_photos_photo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_photos_photo_id_seq OWNER TO hedgefo9;

--
-- Name: user_photos_photo_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hedgefo9
--

ALTER SEQUENCE public.user_photos_photo_id_seq OWNED BY public.user_photos.photo_id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: hedgefo9
--

CREATE TABLE public.users (
    user_id bigint NOT NULL,
    first_name character varying(255) NOT NULL,
    last_name character varying(255) NOT NULL,
    gender integer,
    birth_date date,
    phone_number character varying(20) NOT NULL,
    email character varying(255) NOT NULL,
    city character varying(255) NOT NULL,
    education integer,
    smokes boolean DEFAULT false,
    password_hash character varying(255) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_birth_date_check CHECK ((birth_date <= (CURRENT_DATE - '18 years'::interval))),
    CONSTRAINT users_education_check CHECK (((education >= 0) AND (education <= 4))),
    CONSTRAINT users_gender_check CHECK (((gender >= 0) AND (gender <= 3)))
);


ALTER TABLE public.users OWNER TO hedgefo9;

--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: hedgefo9
--

CREATE SEQUENCE public.users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_user_id_seq OWNER TO hedgefo9;

--
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: hedgefo9
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- Name: admins admin_id; Type: DEFAULT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.admins ALTER COLUMN admin_id SET DEFAULT nextval('public.admins_admin_id_seq'::regclass);


--
-- Name: messages message_id; Type: DEFAULT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.messages ALTER COLUMN message_id SET DEFAULT nextval('public.messages_message_id_seq'::regclass);


--
-- Name: subscriptions subscription_id; Type: DEFAULT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.subscriptions ALTER COLUMN subscription_id SET DEFAULT nextval('public.subscriptions_subscription_id_seq'::regclass);


--
-- Name: user_photos photo_id; Type: DEFAULT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.user_photos ALTER COLUMN photo_id SET DEFAULT nextval('public.user_photos_photo_id_seq'::regclass);


--
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- Data for Name: admins; Type: TABLE DATA; Schema: public; Owner: hedgefo9
--

COPY public.admins (admin_id, name, email, password_hash, created_at) FROM stdin;
1	Зубенко Михаил Петрович	zmp@yandex.ru	$2a$10$sIgGGJUPilZQ/IB6KhTzBuo3jT4aIxr/RoyH.M.7mgNvltoEwdKeW	2024-12-20 06:54:16.593847
\.


--
-- Data for Name: bios; Type: TABLE DATA; Schema: public; Owner: hedgefo9
--

COPY public.bios (user_id, about_me, looking_for, updated_at) FROM stdin;
23	Я Иван, инженер из Москвы. Люблю путешествовать и играть в шахматы.	Ищу человека, с которым можно интересно проводить время.	2024-12-12 23:42:07.828013
24	Меня зовут Сергей, я программист из Казани. Люблю решать сложные задачи и заниматься спортом.	Ищу человека, с которым можно делиться увлечениями и развиваться.	2024-12-12 23:42:07.851629
25	Я Мария из Екатеринбурга, работаю врачом. Увлекаюсь кулинарией и чтением книг.	Ищу интересного собеседника и верного друга.	2024-12-12 23:42:07.878535
26	Привет! Я Алексей из Нижнего Новгорода, занимаюсь бизнесом. Увлекаюсь автоспортом и фотографией.	Ищу человека, который разделяет мои увлечения и стремление к новым вершинам.	2024-12-12 23:42:07.897368
47	Люблю путешествовать и открывать новые культуры. Работаю в сфере маркетинга, интересуюсь искусством и психологией.	Ищу интересных собеседников и людей для общения. Надеюсь найти кого-то, с кем можно разделить увлечения.	2024-12-13 06:56:36.694009
22	Привет, я Елена, работаю дизайнером в Санкт-Петербурге. Увлекаюсь искусством и йогой.	Пупупу	2024-12-13 07:03:16.418651
\.


--
-- Data for Name: likes; Type: TABLE DATA; Schema: public; Owner: hedgefo9
--

COPY public.likes (sender_id, receiver_id, "timestamp") FROM stdin;
26	22	2024-12-05 22:56:22.472781
22	26	2024-12-08 05:53:27.907753
25	26	2024-12-08 05:54:39.633366
23	26	2024-12-08 07:13:31.374041
26	24	2024-12-10 18:03:57.382861
26	25	2024-12-10 18:13:55.965891
22	23	2024-12-10 20:02:40.865246
23	24	2024-12-11 09:02:49.965
23	25	2024-12-11 09:45:52.21
25	23	2024-12-11 09:46:08.174
25	24	2024-12-11 14:36:07.799
25	22	2024-12-11 14:36:58.637
22	25	2024-12-11 15:59:27.255
23	22	2024-12-13 17:38:06.56
\.


--
-- Data for Name: matches; Type: TABLE DATA; Schema: public; Owner: hedgefo9
--

COPY public.matches (user_id1, user_id2, matched_at) FROM stdin;
23	25	2024-12-11 09:46:08.187103
22	25	2024-12-11 15:59:27.271377
22	23	2024-12-13 17:38:06.570395
\.


--
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: hedgefo9
--

COPY public.messages (message_id, sender_id, receiver_id, content, sent_at, is_read) FROM stdin;
7	22	23	привет, Иван! как дела?	2024-12-12 21:40:42.332949	f
8	23	22	привет! у меня всё отлично, а у тебя?	2024-12-12 21:40:59.756459	f
10	22	23	тоже всё супер	2024-12-12 21:41:46.927509	f
11	22	23	гоооооооол	2024-12-12 21:42:02.184815	f
12	23	22	мда	2024-12-12 21:42:25.116807	f
13	22	23	куку	2024-12-12 23:18:32.798793	f
14	23	22	нет ты	2024-12-12 23:19:36.541231	f
15	22	23	Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis eget tortor libero. Aliquam ullamcorper urna sed accumsan commodo. Sed consectetur eu risus id imperdiet. Etiam luctus tempor risus, sit amet finibus lectus fermentum id. Fusce pulvinar mollis diam, a volutpat felis dapibus vel. Pellentesque nisl sapien, facilisis at sem non, lobortis interdum nisi. Cras pharetra commodo sapien sit amet tristique. Phasellus nec congue sapien. Fusce non massa nulla. Aenean dignissim tempus est, ac porta magna lacinia sed. Etiam et fringilla est. Pellentesque facilisis, augue ut molestie vulputate, est urna facilisis mauris, laoreet rutrum odio ipsum eu odio. Nunc quis magna ut erat dignissim sodales eget sit amet nulla.	2024-12-12 23:50:40.293897	f
16	22	25	добрый вечер, Мария! какую пиццу любите?	2024-12-13 00:00:30.627305	f
17	25	22	добрый вечер! с ананасами	2024-12-13 00:01:06.332354	f
18	25	22	гооооооооооооол	2024-12-13 00:03:51.471138	f
19	22	23	гооооооооол	2024-12-13 01:44:01.045315	f
20	22	23	пупупу	2024-12-13 17:38:40.451805	f
\.


--
-- Data for Name: subscriptions; Type: TABLE DATA; Schema: public; Owner: hedgefo9
--

COPY public.subscriptions (subscription_id, user_id, start_at, end_at, plan_type) FROM stdin;
2	24	2024-12-13 12:58:55.663717	2025-01-12 12:58:55.663717	basic
4	25	2024-12-13 17:39:38.243	2025-01-13 17:39:38.244	basic
5	23	2024-12-20 07:19:10.255	2025-01-20 07:19:10.255	basic
\.


--
-- Data for Name: user_photos; Type: TABLE DATA; Schema: public; Owner: hedgefo9
--

COPY public.user_photos (photo_id, user_id, file_name, uploaded_at, is_primary) FROM stdin;
4	22	c29a031a-e936-412b-84c5-15dc5db21da0.jpeg	2024-12-13 10:04:25.780551	f
9	22	2396e377-d1e0-452b-b583-58feb69c9b08.png	2024-12-13 17:36:38.650457	f
2	22	f0c1ddbe-adad-4f11-b46c-cef0ec5678d8.jpg	2024-12-13 08:31:38.804828	t
6	25	a1fa809b-abae-42c9-8ef2-b75c88afdcd7.jpg	2024-12-13 13:22:50.036409	f
8	25	2d3b5cb8-c6a7-4e72-8c93-b7d5589341b0.jpg	2024-12-13 14:36:06.809062	t
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: hedgefo9
--

COPY public.users (user_id, first_name, last_name, gender, birth_date, phone_number, email, city, education, smokes, password_hash, created_at, updated_at) FROM stdin;
24	Сергей	Петров	0	1985-08-13	+79165543210	sergey.petrov@mail.ru	Казань	3	f	$2a$10$7eVvG19wIWZNmoanxrYfZOQJfQdk9x66/mkHp4Gf2PbkJO6I.UHcO	2024-12-05 21:32:30.065024	2024-12-05 21:32:30.065024
25	Мария	Иванова	1	1995-11-30	+79169881234	maria.ivanova@mail.ru	Екатеринбург	2	t	$2a$10$A2FLkbvUB4kCIz2HxiJgqe6vabbjehIj/t0tbIoFhs1.fPaGqKkiu	2024-12-05 21:33:17.773923	2024-12-05 21:33:17.773923
26	Алексей	Григорьев	0	1988-01-25	+79162345678	alexey.grigoryev@mail.ru	Нижний Новгород	1	f	$2a$10$lU6ggP4SiLlYosCBMEbJ7./fCM8wJsXC2CEAtq65360bPaX0njL7.	2024-12-05 21:33:40.812144	2024-12-05 21:33:40.812144
47	Елена	Петрова	1	1975-11-22	+79261234567	elena.petrova@mail.ru	Москва	3	t	$2a$10$o6Y7vOz7oOVFHh2dmQ0TCOF9w72lNJ5i06eLjDHNJmLfZ/sD9aSk2	2024-12-13 06:56:36.644929	2024-12-13 06:56:36.644929
23	Иван	Кузнецов	0	1990-06-15	+79161234566	ivan.kuznetsov@mail.ru	Москва	3	f	$2a$10$L/m4IUxhvxcfwXpvb.gPTOtMVMmNt1m3wtpjphwq3aUmPvV/ns8h6	2024-12-05 21:27:39.257006	2024-12-13 04:10:20.141433
22	Елена	Смирнова	1	1992-03-21	+79169876543	elena.smirnova@mail.ru	Санкт-Петербург	1	t	$2a$10$iPwKtXtvU/7fCac/UvE9ge8apO227ZvNAdy7QL.rR6fdz8ZoIXg16	2024-12-05 21:19:52.228074	2024-12-13 07:04:57.543113
\.


--
-- Name: admins_admin_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hedgefo9
--

SELECT pg_catalog.setval('public.admins_admin_id_seq', 1, true);


--
-- Name: messages_message_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hedgefo9
--

SELECT pg_catalog.setval('public.messages_message_id_seq', 20, true);


--
-- Name: subscriptions_subscription_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hedgefo9
--

SELECT pg_catalog.setval('public.subscriptions_subscription_id_seq', 5, true);


--
-- Name: user_photos_photo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hedgefo9
--

SELECT pg_catalog.setval('public.user_photos_photo_id_seq', 9, true);


--
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hedgefo9
--

SELECT pg_catalog.setval('public.users_user_id_seq', 47, true);


--
-- Name: admins admins_email_key; Type: CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.admins
    ADD CONSTRAINT admins_email_key UNIQUE (email);


--
-- Name: admins admins_pkey; Type: CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.admins
    ADD CONSTRAINT admins_pkey PRIMARY KEY (admin_id);


--
-- Name: bios bios_pkey; Type: CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.bios
    ADD CONSTRAINT bios_pkey PRIMARY KEY (user_id);


--
-- Name: matches match_pkey; Type: CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.matches
    ADD CONSTRAINT match_pkey PRIMARY KEY (user_id1, user_id2);


--
-- Name: messages messages_pkey; Type: CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (message_id);


--
-- Name: likes pk_like; Type: CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT pk_like PRIMARY KEY (sender_id, receiver_id);


--
-- Name: subscriptions subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT subscriptions_pkey PRIMARY KEY (subscription_id);


--
-- Name: user_photos user_photos_pkey; Type: CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.user_photos
    ADD CONSTRAINT user_photos_pkey PRIMARY KEY (photo_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_phone_number_key; Type: CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_phone_number_key UNIQUE (phone_number);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: likes check_match_on_delete; Type: TRIGGER; Schema: public; Owner: hedgefo9
--

CREATE TRIGGER check_match_on_delete AFTER DELETE ON public.likes FOR EACH ROW EXECUTE FUNCTION public.remove_match();


--
-- Name: likes check_match_on_insert; Type: TRIGGER; Schema: public; Owner: hedgefo9
--

CREATE TRIGGER check_match_on_insert AFTER INSERT ON public.likes FOR EACH ROW EXECUTE FUNCTION public.add_match();


--
-- Name: bios fk_bios_user; Type: FK CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.bios
    ADD CONSTRAINT fk_bios_user FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: user_photos fk_photos_user; Type: FK CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.user_photos
    ADD CONSTRAINT fk_photos_user FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: likes fk_receiver; Type: FK CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT fk_receiver FOREIGN KEY (receiver_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: messages fk_receiver; Type: FK CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fk_receiver FOREIGN KEY (receiver_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: likes fk_sender; Type: FK CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT fk_sender FOREIGN KEY (sender_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: messages fk_sender; Type: FK CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fk_sender FOREIGN KEY (sender_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: subscriptions fk_user; Type: FK CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.subscriptions
    ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: matches match_user_id1_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.matches
    ADD CONSTRAINT match_user_id1_fkey FOREIGN KEY (user_id1) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: matches match_user_id2_fkey; Type: FK CONSTRAINT; Schema: public; Owner: hedgefo9
--

ALTER TABLE ONLY public.matches
    ADD CONSTRAINT match_user_id2_fkey FOREIGN KEY (user_id2) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

