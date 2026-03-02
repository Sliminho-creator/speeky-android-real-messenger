# Speeky Android Messenger Starter Kit

Это **стартовый комплект** для настоящего mobile-first мессенджера:

- `android/` - Android-клиент на Kotlin + Jetpack Compose
- `backend/` - Node.js backend на Express + Socket.IO + JWT + загрузка файлов
- `infra/` - Docker Compose для PostgreSQL, Redis, MinIO и coturn

## Что уже есть

### Android
- регистрация и логин (экраны + API-клиент)
- список чатов
- экран чата
- экран профиля контакта
- экран настроек, устройств и темы
- тёмная визуальная тема в стиле референса
- анимации переключения экранов
- звуки отправки и входящего сообщения
- заготовка входящего звонка со звуком
- запись голосового сообщения через `MediaRecorder`
- FCM service + notification channels

### Backend
- `/api/auth/register`
- `/api/auth/login`
- `/api/auth/me`
- `/api/users/search`
- `/api/chats`
- `/api/chats/:id/messages`
- `/api/messages/direct`
- `/api/upload`
- `/api/calls/offer`, `/api/calls/answer`, `/api/calls/ice`
- Socket.IO для realtime-событий: сообщения, typing, входящий звонок
- локальная JSON-персистентность для dev-режима

## Что обязательно нужно поднять на сервере для реального общения

### Минимум для запуска dev-версии
1. Node.js 20+
2. Папка для `uploads/`
3. PM2 или systemd для backend
4. Nginx reverse proxy
5. HTTPS (Let's Encrypt)

### Для production
1. PostgreSQL - аккаунты, чаты, сообщения, устройства
2. Redis - presence, очереди, кеш, delivery-state
3. S3/MinIO - фото, видео, голосовые, кружочки
4. coturn - TURN/STUN для WebRTC звонков
5. Firebase Cloud Messaging - push-уведомления Android
6. FFmpeg - постобработка голосовых и кружочков

## Быстрый запуск backend локально

```bash
cd backend
cp .env.example .env
npm install
npm run dev
```

Backend поднимется на `http://localhost:4000`.

## Android
Открой папку `android/` в Android Studio, добавь свой `google-services.json` в `android/app/`, затем:

1. Sync Gradle
2. Выставь `BASE_URL` в `ApiClient.kt`
3. Запусти на реальном устройстве (для микрофона/камеры/уведомлений)

## Production-порядок внедрения

1. Поднять backend на VPS
2. Подключить PostgreSQL/Redis/MinIO/coturn
3. Выдать домен и HTTPS
4. Добавить `google-services.json` и Firebase-проект
5. Собрать Android APK/AAB
6. Подключить реальный WebRTC стек (LiveKit / native WebRTC)

## Важно

Это **сильная стартовая кодовая база**, а не уже вылизанный App Store / Play Market релиз.

- Android UI и архитектура собраны
- backend живой и runnable для dev
- WebRTC signaling есть
- полноценный media relay для звонков требует coturn + доработки call-flow
- кружочки и advanced media moderation потребуют дополнительной шлифовки
