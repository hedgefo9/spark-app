-- Основная таблица пользователей
CREATE TABLE users
(
    user_id       BIGSERIAL PRIMARY KEY,                                         -- Уникальный идентификатор пользователя
    first_name    VARCHAR(255)        NOT NULL,                                  -- Имя пользователя
    last_name     VARCHAR(255)        NOT NULL,                                  -- Фамилия пользователя
    gender        INT CHECK (gender BETWEEN 0 AND 3),                            -- Пол (0 - Мужской, 1 - Женский, 2 - Не указан)
    birth_date    DATE CHECK (birth_date <= CURRENT_DATE - INTERVAL '18 years'), -- Проверка возраста (не менее 18 лет)
    phone_number  VARCHAR(20) UNIQUE  NOT NULL,                                  -- Номер телефона (уникальный и обязательный)
    email         VARCHAR(255) UNIQUE NOT NULL,                                  -- Email пользователя (уникальное значение)
    city          VARCHAR(255)        NOT NULL,                                  -- Город (обязателен)
    education     INT CHECK (education BETWEEN 0 AND 4),                         -- Образование (0 - Basic, 1 - High School, 2 - Bachelor, 3 - Master, 4 - PhD)
    smokes        BOOLEAN   DEFAULT FALSE,                                       -- Курит ли пользователь
    password_hash VARCHAR(255)        NOT NULL,                                  -- Хеш пароля
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                           -- Дата создания записи
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP                            -- Дата последнего обновления записи
);

-- Таблица для хранения фотографий пользователей
CREATE TABLE user_photos
(
    photo_id    BIGSERIAL PRIMARY KEY,                                              -- Уникальный идентификатор фотографии
    user_id     BIGINT       NOT NULL REFERENCES users (user_id) ON DELETE CASCADE, -- ID пользователя (внешний ключ)
    file_name   VARCHAR(255) NOT NULL,                                              -- Название файла фотографии
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                -- Дата и время загрузки фотографии
    is_primary  BOOLEAN   DEFAULT FALSE                                             -- Является ли фотография основной
);


CREATE TABLE likes
(
    sender_id   BIGINT NOT NULL,                                                                   -- ID пользователя, отправившего лайк
    receiver_id BIGINT NOT NULL,                                                                   -- ID пользователя, получившего лайк
    timestamp   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,                                               -- Метка времени создания
    CONSTRAINT fk_sender FOREIGN KEY (sender_id) REFERENCES users (user_id) ON DELETE CASCADE,     -- Связь с пользователем (отправителем)
    CONSTRAINT fk_receiver FOREIGN KEY (receiver_id) REFERENCES users (user_id) ON DELETE CASCADE, -- Связь с пользователем (получателем)
    CONSTRAINT chk_different_users CHECK (sender_id != receiver_id),                               -- Запрет лайков самому себе
    CONSTRAINT pk_like PRIMARY KEY (sender_id, receiver_id)                                        -- Составной первичный ключ для уникальности
);

CREATE TABLE match
(
    user_id1   BIGINT NOT NULL,                     -- Меньший ID пользователя
    user_id2   BIGINT NOT NULL,                     -- Больший ID пользователя
    matched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Время создания мэтча
    PRIMARY KEY (user_id1, user_id2),
    CONSTRAINT user_order CHECK (user_id1 < user_id2),
    FOREIGN KEY (user_id1) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id2) REFERENCES users (user_id) ON DELETE CASCADE
);


------ ТРИГГЕРЫ --------
CREATE OR REPLACE FUNCTION add_match() RETURNS TRIGGER AS
$$
BEGIN
    -- Проверяем, существует ли взаимный лайк
    IF EXISTS (SELECT 1
               FROM likes
               WHERE sender_id = NEW.receiver_id
                 AND receiver_id = NEW.sender_id) THEN
        -- Добавляем запись о мэтче
        INSERT INTO match (user_id1, user_id2, matched_at)
        VALUES (LEAST(NEW.sender_id, NEW.receiver_id),
                GREATEST(NEW.sender_id, NEW.receiver_id),
                CURRENT_TIMESTAMP)
        ON CONFLICT DO NOTHING; -- Избегаем дублирования
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_match_on_insert
    AFTER INSERT
    ON likes
    FOR EACH ROW
EXECUTE FUNCTION add_match();


CREATE OR REPLACE FUNCTION remove_match() RETURNS TRIGGER AS
$$
BEGIN
    -- Удаляем запись о мэтче, если один из лайков удалён
    DELETE
    FROM match
    WHERE (user_id1, user_id2) = (
                                  LEAST(OLD.sender_id, OLD.receiver_id),
                                  GREATEST(OLD.sender_id, OLD.receiver_id)
        );

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_match_on_delete
    AFTER DELETE
    ON likes
    FOR EACH ROW
EXECUTE FUNCTION remove_match();

----------------------------------------

CREATE TABLE messages
(
    id          BIGSERIAL PRIMARY KEY,               -- Уникальный идентификатор сообщения
    sender_id   BIGINT NOT NULL,                     -- ID отправителя
    receiver_id BIGINT NOT NULL,                     -- ID получателя
    content     TEXT   NOT NULL,                     -- Текст сообщения
    sent_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Время отправки сообщения
    is_read     BOOLEAN   DEFAULT FALSE,             -- Прочитано ли сообщение
    CONSTRAINT fk_sender FOREIGN KEY (sender_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_receiver FOREIGN KEY (receiver_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- Таблица bios
CREATE TABLE bios
(
    user_id     BIGINT PRIMARY KEY,      -- Первичный ключ и внешний ключ для связи с пользователем
    about_me    TEXT,                    -- Информация "Обо мне"
    looking_for TEXT,                    -- Информация "Ищу"
    updated_at  TIMESTAMP DEFAULT NOW(), -- Время последнего обновления
    CONSTRAINT fk_bios_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)       -- Ссылка на таблицу users
        ON DELETE CASCADE                -- Удаление записи при удалении пользователя
);

INSERT INTO bios (user_id, about_me, looking_for)
VALUES (22, 'Привет, я Елена, работаю дизайнером в Санкт-Петербурге. Увлекаюсь искусством и йогой.',
        'Ищу единомышленника, который ценит творчество и активный образ жизни.');

INSERT INTO bios (user_id, about_me, looking_for)
VALUES (23, 'Я Иван, инженер из Москвы. Люблю путешествовать и играть в шахматы.',
        'Ищу человека, с которым можно интересно проводить время.');

INSERT INTO bios (user_id, about_me, looking_for)
VALUES (24, 'Меня зовут Сергей, я программист из Казани. Люблю решать сложные задачи и заниматься спортом.',
        'Ищу человека, с которым можно делиться увлечениями и развиваться.');

INSERT INTO bios (user_id, about_me, looking_for)
VALUES (25, 'Я Мария из Екатеринбурга, работаю врачом. Увлекаюсь кулинарией и чтением книг.',
        'Ищу интересного собеседника и верного друга.');

INSERT INTO bios (user_id, about_me, looking_for)
VALUES (26, 'Привет! Я Алексей из Нижнего Новгорода, занимаюсь бизнесом. Увлекаюсь автоспортом и фотографией.',
        'Ищу человека, который разделяет мои увлечения и стремление к новым вершинам.');


-- Таблица user_photos
CREATE TABLE user_photos
(
    photo_id    BIGSERIAL PRIMARY KEY,   -- Уникальный идентификатор фотографии
    user_id     BIGINT       NOT NULL,   -- Внешний ключ для связи с пользователем
    file_name   VARCHAR(255) NOT NULL,   -- Имя файла фотографии
    uploaded_at TIMESTAMP DEFAULT NOW(), -- Время загрузки фотографии
    is_primary  BOOLEAN   DEFAULT FALSE, -- Признак основной фотографии
    CONSTRAINT fk_photos_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)       -- Ссылка на таблицу users
        ON DELETE CASCADE                -- Удаление записи при удалении пользователя
);


-- Таблица subscriptions
CREATE TABLE subscriptions
(
    subscription_id BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    start_date      DATE        NOT NULL,
    end_date        DATE        NOT NULL,
    plan_type       VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- Таблица admins
create table admins
(
    admin_id      serial primary key,
    name          varchar(255) not null,
    email         varchar(255) not null unique,
    password_hash varchar(255) not null,
    created_at    timestamp default CURRENT_TIMESTAMP
);
