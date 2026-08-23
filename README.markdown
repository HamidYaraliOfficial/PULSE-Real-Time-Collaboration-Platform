# PULSE — Real-Time Collaboration Platform

**One workspace for chat, voice & video, documents, tasks, and calendar.**
A Slack + Discord + Notion + Microsoft Teams + Linear‑style platform, built with a strictly separated **TypeScript (Next.js) web client** and **Kotlin (Spring Boot) backend**.

> This README is written in three languages. Jump to the section you need:
> **[English](#-english)** · **[فارسی](#-فارسی)** · **[中文](#-中文)**

---

<a id="-english"></a>
## 🇬🇧 English

### Overview

PULSE is a full-stack, multi-tenant collaboration operating system. Organizations contain Workspaces, Workspaces contain Teams, Channels, Projects and Documents, and every user can belong to multiple Workspaces with a role-based permission model (`OWNER`, `ADMIN`, `MANAGER`, `MEMBER`, `GUEST`).

### ✨ Feature Highlights

| Area | What's included |
|---|---|
| **Workspace & RBAC** | Organizations → Workspaces → Teams → Channels → Projects → Tasks, 5-tier roles, membership & invites |
| **Real-time chat** | Public/private channels, DMs, threads, replies, reactions, mentions (`@user`, `@here`, `@everyone`), pin, edit, delete, typing indicators, live presence, unread counters, full-text search — all pushed live over WebSocket (STOMP) |
| **Presence** | Online / Away / Busy / Do Not Disturb / Offline, backed by Redis with automatic timeout to Offline, broadcast to every connected client |
| **Kanban & Projects** | Backlog → To Do → In Progress → Review → Testing → Done, drag-and-drop that live-syncs to every teammate's board, priorities, labels, due dates, comments, dependencies |
| **Calendar & Meetings** | Month view, events/meetings/deadlines/reminders, attendees & RSVP, meeting notes with action items |
| **Configurable Business Hours** | Each workspace enters its **own** opening hours per day of the week (or marks a day fully closed) — the UI form is completely blank/user-driven, nothing is hardcoded. The backend then computes, live, whether the workspace is open right now and a running countdown to the next open/close transition. |
| **Collaborative Documents** | Notion-style block editor, live multi-user editing over WebSocket, auto-save, full version history with restore, favorites |
| **Notifications** | Mentions, assignments, comments, meetings, document changes, deadlines — delivered instantly to a per-user WebSocket queue |
| **Dashboard** | My tasks, unread messages, active projects, upcoming meetings, online teammates, notification count — one unified view |
| **Team Hub** | Every member, their role, title, and live presence, with one-click invite |
| **AI Assistant** | Permission-aware: it only ever reasons over context your own request already had access to (e.g. a channel you're in). Wired to summarize conversations/meetings, extract action items, suggest tasks, summarize documents, rewrite, translate, draft a project-status update, and suggest smart replies, by calling the Anthropic Messages API. |
| **Command Palette** | `Ctrl/Cmd + K` for instant search & navigation |
| **Task Automation** | Instant rules (notify on mention, notify assignee, notify project on document change) plus a scheduled background worker for "remind one day before deadline" |
| **Security** | JWT access + rotating refresh tokens, BCrypt password hashing, per-workspace RBAC enforced server-side, audit log on sensitive actions (invites, workspace/team creation, permission-relevant events) |
| **Theming** | 5 built-in themes — **Windows 11 Light**, **Windows 11 Dark**, **Windows Default**, **Red**, **Blue** — all Fluent-style design tokens, switchable live from Settings |
| **Localization** | English, فارسی (Persian), 中文 (Chinese) — Persian renders fully **RTL**, English/Chinese render **LTR**, switchable live |

### 🧱 Tech Stack

| Layer | Technology |
|---|---|
| Web client | Next.js 14, React 18, TypeScript, Tailwind CSS, Radix UI, Framer Motion, Zustand |
| Backend | Kotlin, Spring Boot 3 (Web, Security, Data JPA, WebSocket), PostgreSQL, Flyway |
| Real-time | STOMP over WebSocket (SockJS), Redis (presence, pub/sub) |
| Auth | JWT (access + refresh), BCrypt |
| AI | Anthropic Messages API (optional — the app runs fully without it) |
| Infra | Docker & Docker Compose |

### 📁 Project Structure

```
pulse-platform/
├── apps/
│   ├── backend/                  Kotlin + Spring Boot API
│   │   └── src/main/kotlin/com/pulse/
│   │       ├── domain/            JPA entities
│   │       ├── repository/        Spring Data repositories
│   │       ├── dto/                Request/response DTOs
│   │       ├── service/            Business logic
│   │       ├── controller/         REST endpoints
│   │       ├── ws/                 STOMP WebSocket handlers
│   │       ├── security/           JWT + STOMP auth
│   │       ├── scheduler/          Background workers
│   │       └── config/             Security, WebSocket, Redis, OpenAPI
│   └── web/                       Next.js + TypeScript client
│       └── src/
│           ├── app/                 App Router pages
│           ├── components/          UI, chat, kanban, calendar, documents, dashboard
│           ├── hooks/                WebSocket client, presence heartbeat
│           ├── lib/                  API client, i18n, utils
│           └── store/                Zustand state (auth, workspace, UI)
├── docker-compose.yml
└── .env.example
```

### ✅ Prerequisites

- **Docker** & **Docker Compose** (recommended path), **or**
- **JDK 17+** and **Gradle 8.x** for the backend, plus **Node.js 20+** and **npm** for the web client, and a local **PostgreSQL 16** + **Redis 7**

### 🚀 Installation

Extract the downloaded project archive to a folder on your machine, then `cd` into it.

#### Option A — Docker Compose (recommended, one command)

```bash
cp .env.example .env
docker compose up --build
```

This starts PostgreSQL, Redis, the Spring Boot API (`:8080`), and the Next.js web client (`:3000`). Flyway migrations run automatically on backend startup. Open **http://localhost:3000**, register the first account, and you're in.

#### Option B — Run each service manually

**1. Database services**

```bash
docker run -d --name pulse-postgres -e POSTGRES_DB=pulse -e POSTGRES_USER=pulse -e POSTGRES_PASSWORD=pulse -p 5432:5432 postgres:16-alpine
docker run -d --name pulse-redis -p 6379:6379 redis:7-alpine
```

(Or point the backend at any PostgreSQL/Redis instance you already have.)

**2. Backend**

```bash
cd apps/backend
# If you don't already have the Gradle wrapper jar, generate it once with a local Gradle install:
gradle wrapper --gradle-version 8.8
./gradlew bootRun
```

The API starts on **http://localhost:8080** (Swagger UI at `/swagger-ui.html`).

**3. Web client**

```bash
cd apps/web
npm install
cp .env.example .env.local
npm run dev
```

The app starts on **http://localhost:3000**.

**4. Install required libraries (if you're not using the commands above verbatim)**

```bash
# Backend dependencies are declared in apps/backend/build.gradle.kts and
# resolve automatically on first `gradle`/`./gradlew` invocation — no
# manual install step needed.

# Frontend dependencies:
cd apps/web && npm install
```

### ⚙️ Environment Variables

| Variable | Where | Description |
|---|---|---|
| `DB_NAME`, `DB_USER`, `DB_PASSWORD` | backend | PostgreSQL connection |
| `JWT_SECRET` | backend | Secret used to sign JWTs — **change this in production** |
| `CORS_ORIGINS` | backend | Comma-separated list of allowed web origins |
| `AI_API_KEY`, `AI_MODEL` | backend | Optional. Leave `AI_API_KEY` empty to run without AI features |
| `NEXT_PUBLIC_API_URL` | web | Base URL of the backend REST API |
| `NEXT_PUBLIC_WS_URL` | web | WebSocket (SockJS) endpoint, typically `<api-url>/ws` |

### 🕒 Business Hours (fully user-configurable)

Under **Settings → Business Hours** (also shown on the Calendar page), each workspace enters its **own** schedule — open/close time per day of the week, or "closed all day" — nothing ships hardcoded. Once saved, a live badge shows **Open now** or **Closed now**, plus a running countdown ("closes in 2h 14m" / "opens in 6h 40m") computed server-side and refreshed every second on the client.

### 🧪 Testing

```bash
# Backend unit tests
cd apps/backend && ./gradlew test

# Frontend
cd apps/web && npm run test
```

Included backend tests cover the business-hours open/closed/countdown logic and authentication failure paths as a starting point — extend them as you build out further features.

### 🗺️ Scope & Roadmap

Everything listed under **Feature Highlights** above is real, working code connected end-to-end (REST + WebSocket + PostgreSQL/Redis) — there are no mocked buttons or fake API responses. A project this size does have a natural next-phase list; these are intentionally **not** included yet so the scaffold you received stays honest about what's wired up:

- **Voice/video calls & screen sharing** — the data model and signaling boundary are in place (Kotlin backend handles auth/room-management/signaling, per the spec's architecture), but the WebRTC client + SFU media routing itself is a separate build-out.
- **Android app** — not included in this delivery; the REST/WebSocket API is designed to support a Jetpack Compose client using the same endpoints.
- **Workspace-wide AI search (RAG)** — the AI assistant works great over content you hand it (a conversation, a document, a task list); indexing the entire workspace into a vector store for open-ended semantic search is a follow-up project.
- **Third-party integrations** (GitHub, GitLab, Google Calendar/Drive, Dropbox, Jira) — the `integrations` table and webhook-secret model exist; the individual OAuth flows and webhook handlers per provider are not implemented yet.
- **OAuth login / 2FA enforcement** — the schema has the fields; only email+password auth is wired up today.
- **Admin analytics dashboard** — the audit log and core data are in place to build this on top of.

### 📄 License

MIT — use this as a learning reference or as the foundation for your own product.

---

<a id="-فارسی"></a>
## 🇮🇷 فارسی

<div dir="rtl">

### معرفی

پالس (PULSE) یک سیستم‌عامل همکاری تیمی به‌صورت فول‌استک و چندمستأجری (Multi-Tenant) است. هر سازمان شامل چند Workspace است، هر Workspace شامل تیم‌ها، کانال‌ها، پروژه‌ها و اسناد است، و هر کاربر می‌تواند عضو چند Workspace باشد؛ همه‌چیز با یک مدل نقش پنج‌سطحی (`OWNER`، `ADMIN`، `MANAGER`، `MEMBER`، `GUEST`) کنترل می‌شود.

### ✨ ویژگی‌های کلیدی

| بخش | شرح |
|---|---|
| **فضای کاری و کنترل دسترسی** | Organization ← Workspace ← Team ← Channel ← Project ← Task، پنج سطح نقش، عضویت و دعوت اعضا |
| **گفتگوی بلادرنگ** | کانال عمومی/خصوصی، پیام خصوصی، موضوع گفتگو (Thread)، پاسخ، ری‌اکشن، منشن (`@user@`, `@here@`, `@everyone@`)، سنجاق‌کردن، ویرایش، حذف، نشانگر تایپ، وضعیت آنلاین زنده، شمارنده پیام‌های خوانده‌نشده، جستجوی متنی کامل — همه از طریق WebSocket (STOMP) به‌صورت آنی |
| **وضعیت حضور** | آنلاین / غایب / مشغول / مزاحم نشوید / آفلاین، مبتنی بر Redis با قطع خودکار به‌حالت آفلاین و ارسال زنده برای همه کلاینت‌ها |
| **برد وظایف و پروژه‌ها** | بک‌لاگ ← برای انجام ← در حال انجام ← بازبینی ← تست ← انجام‌شده، جابه‌جایی Drag & Drop که به‌صورت زنده برای همه اعضا همگام می‌شود، اولویت، برچسب، مهلت، نظر، وابستگی |
| **تقویم و جلسات** | نمای ماهانه، رویداد/جلسه/مهلت/یادآوری، شرکت‌کنندگان و پاسخ دعوت (RSVP)، یادداشت جلسه همراه با اقدام‌ها |
| **ساعات کاری قابل‌تنظیم** | هر Workspace ساعات باز بودن **خودش** را برای هر روز هفته وارد می‌کند (یا آن روز را کاملاً تعطیل علامت می‌زند) — فرم کاملاً خالی و متکی به ورودی کاربر است و هیچ مقداری از پیش تعیین‌شده نیست. سپس بک‌اند به‌صورت زنده محاسبه می‌کند که هم‌اکنون باز است یا خیر و چه مدت تا تغییر وضعیت بعدی باقی مانده است. |
| **اسناد مشترک** | ویرایشگر بلوکی شبیه Notion، ویرایش هم‌زمان چندکاربره از طریق WebSocket، ذخیره خودکار، تاریخچه کامل نسخه‌ها با قابلیت بازگردانی، موردعلاقه‌ها |
| **اعلان‌ها** | منشن، تخصیص وظیفه، نظر، جلسه، تغییر سند، مهلت — همگی به‌صورت آنی از طریق صف اختصاصی هر کاربر در WebSocket ارسال می‌شوند |
| **داشبورد** | وظایف من، پیام‌های خوانده‌نشده، پروژه‌های فعال، جلسات پیش‌رو، اعضای آنلاین، تعداد اعلان — همه در یک نمای یکپارچه |
| **مرکز تیم** | همه اعضا، نقش، سمت و وضعیت حضور زنده هرکدام، همراه با دعوت با یک کلیک |
| **دستیار هوش مصنوعی** | آگاه به سطح دسترسی: فقط روی محتوایی استدلال می‌کند که همان درخواست کاربر از قبل به آن دسترسی داشته (مثلاً کانالی که عضو آن است). برای خلاصه‌سازی گفتگو/جلسه، استخراج اقدام‌ها، پیشنهاد وظیفه، خلاصه‌سازی سند، بازنویسی، ترجمه، تهیه گزارش وضعیت پروژه و پیشنهاد پاسخ هوشمند، از طریق فراخوانی Anthropic Messages API متصل شده است. |
| **پنل فرمان** | با `Ctrl/Cmd + K` جستجو و پیمایش آنی در دسترس است |
| **اتوماسیون وظایف** | قوانین آنی (اطلاع‌رسانی هنگام منشن، اطلاع‌رسانی به مسئول وظیفه، اطلاع‌رسانی تیم پروژه هنگام تغییر سند) به‌همراه یک Worker زمان‌بندی‌شده برای «یادآوری یک روز پیش از مهلت» |
| **امنیت** | JWT همراه با Refresh Token چرخشی، هش رمز عبور با BCrypt، کنترل دسترسی مبتنی‌بر نقش در سمت سرور برای هر Workspace، ثبت گزارش رخداد (Audit Log) برای عملیات حساس (دعوت، ساخت Workspace/Team و رخدادهای مرتبط با Permission) |
| **پوسته‌بندی** | ۵ پوسته آماده — **ویندوز ۱۱ روشن**، **ویندوز ۱۱ تاریک**، **پیش‌فرض ویندوز**، **قرمز**، **آبی** — همگی با زبان طراحی Fluent، قابل تغییر آنی از تنظیمات |
| **بومی‌سازی** | انگلیسی، فارسی، چینی — فارسی به‌طور کامل **راست‌چین (RTL)** و انگلیسی/چینی **چپ‌چین (LTR)** نمایش داده می‌شوند و به‌صورت آنی قابل تغییرند |

### 🧱 پشته فناوری

| لایه | فناوری |
|---|---|
| کلاینت وب | Next.js 14، React 18، TypeScript، Tailwind CSS، Radix UI، Framer Motion، Zustand |
| بک‌اند | Kotlin، Spring Boot 3 (Web، Security، Data JPA، WebSocket)، PostgreSQL، Flyway |
| ارتباط بلادرنگ | STOMP روی WebSocket (SockJS)، Redis (حضور، Pub/Sub) |
| احراز هویت | JWT (Access + Refresh)، BCrypt |
| هوش مصنوعی | Anthropic Messages API (اختیاری — برنامه بدون آن هم کامل اجرا می‌شود) |
| زیرساخت | Docker و Docker Compose |

### 📁 ساختار پروژه

```
pulse-platform/
├── apps/
│   ├── backend/                  سرویس Kotlin + Spring Boot
│   │   └── src/main/kotlin/com/pulse/
│   │       ├── domain/            موجودیت‌های JPA
│   │       ├── repository/        ریپازیتوری‌های Spring Data
│   │       ├── dto/                مدل‌های درخواست/پاسخ
│   │       ├── service/            منطق اصلی برنامه
│   │       ├── controller/         نقاط پایانی REST
│   │       ├── ws/                 مدیریت WebSocket با STOMP
│   │       ├── security/           احراز هویت JWT و STOMP
│   │       ├── scheduler/          Workerهای پس‌زمینه
│   │       └── config/             تنظیمات Security، WebSocket، Redis، OpenAPI
│   └── web/                       کلاینت Next.js + TypeScript
│       └── src/
│           ├── app/                 صفحات App Router
│           ├── components/          کامپوننت‌های UI، چت، کانبان، تقویم، اسناد، داشبورد
│           ├── hooks/                کلاینت WebSocket، Heartbeat وضعیت حضور
│           ├── lib/                  کلاینت API، بومی‌سازی، ابزارها
│           └── store/                مدیریت وضعیت با Zustand (احراز هویت، Workspace، رابط کاربری)
├── docker-compose.yml
└── .env.example
```

### ✅ پیش‌نیازها

- **Docker** و **Docker Compose** (روش پیشنهادی)، **یا**
- **JDK نسخه ۱۷ به بالا** و **Gradle نسخه ۸.x** برای بک‌اند، به‌همراه **Node.js نسخه ۲۰ به بالا** و **npm** برای کلاینت وب، و یک نمونه محلی از **PostgreSQL 16** و **Redis 7**

### 🚀 نصب و راه‌اندازی

پوشه فشرده پروژه دانلودشده را در مسیر مدنظر خود Extract کنید و سپس وارد آن پوشه شوید.

#### روش الف — Docker Compose (پیشنهادی، فقط یک دستور)

```bash
cp .env.example .env
docker compose up --build
```

این دستور PostgreSQL، Redis، API نوشته‌شده با Spring Boot (روی پورت `۸۰۸۰`) و کلاینت وب Next.js (روی پورت `۳۰۰۰`) را اجرا می‌کند. مایگریشن‌های Flyway هنگام بالا آمدن بک‌اند به‌صورت خودکار اجرا می‌شوند. آدرس **http://localhost:3000** را باز کنید، اولین حساب کاربری را بسازید و وارد شوید.

#### روش ب — اجرای دستی هر سرویس

**۱. سرویس‌های پایگاه‌داده**

```bash
docker run -d --name pulse-postgres -e POSTGRES_DB=pulse -e POSTGRES_USER=pulse -e POSTGRES_PASSWORD=pulse -p 5432:5432 postgres:16-alpine
docker run -d --name pulse-redis -p 6379:6379 redis:7-alpine
```

(یا بک‌اند را به هر نمونه PostgreSQL/Redis که از پیش دارید متصل کنید.)

**۲. بک‌اند**

```bash
cd apps/backend
# اگر فایل wrapper گریدل را از پیش ندارید، یک‌بار با نصب محلی Gradle آن را بسازید:
gradle wrapper --gradle-version 8.8
./gradlew bootRun
```

API روی آدرس **http://localhost:8080** بالا می‌آید (مستندات Swagger در مسیر `/swagger-ui.html`).

**۳. کلاینت وب**

```bash
cd apps/web
npm install
cp .env.example .env.local
npm run dev
```

برنامه روی آدرس **http://localhost:3000** بالا می‌آید.

**۴. نصب کتابخانه‌های موردنیاز (در صورتی‌که دستورات بالا را عیناً اجرا نمی‌کنید)**

```bash
# وابستگی‌های بک‌اند در فایل apps/backend/build.gradle.kts تعریف شده‌اند و
# با اولین اجرای gradle یا ./gradlew به‌صورت خودکار دانلود و نصب می‌شوند —
# نیازی به نصب دستی نیست.

# وابستگی‌های فرانت‌اند:
cd apps/web && npm install
```

### ⚙️ متغیرهای محیطی

| متغیر | محل استفاده | توضیح |
|---|---|---|
| `DB_NAME`, `DB_USER`, `DB_PASSWORD` | بک‌اند | اطلاعات اتصال به PostgreSQL |
| `JWT_SECRET` | بک‌اند | کلید امضای JWT — **حتماً در محیط Production تغییر دهید** |
| `CORS_ORIGINS` | بک‌اند | فهرست دامنه‌های وب مجاز، جدا شده با کاما |
| `AI_API_KEY`, `AI_MODEL` | بک‌اند | اختیاری. برای اجرای برنامه بدون قابلیت‌های هوش مصنوعی، `AI_API_KEY` را خالی بگذارید |
| `NEXT_PUBLIC_API_URL` | وب | آدرس پایه API بک‌اند |
| `NEXT_PUBLIC_WS_URL` | وب | آدرس WebSocket (SockJS)، معمولاً برابر با `<api-url>/ws` |

### 🕒 ساعات کاری (کاملاً قابل تنظیم توسط کاربر)

از مسیر **تنظیمات ← ساعات کاری** (که در صفحه تقویم نیز نمایش داده می‌شود)، هر Workspace برنامه **خودش** را وارد می‌کند — ساعت باز و بسته شدن برای هر روز هفته، یا گزینه «تمام روز تعطیل» — هیچ مقداری از پیش در کد قرار داده نشده است. پس از ذخیره، یک نشان زنده وضعیت **هم‌اکنون باز است** یا **هم‌اکنون بسته است** را نمایش می‌دهد، به‌همراه شمارش معکوس زنده (مثلاً «۲ ساعت و ۱۴ دقیقه تا بسته شدن» یا «۶ ساعت و ۴۰ دقیقه تا باز شدن») که در سمت سرور محاسبه و هر ثانیه در کلاینت به‌روزرسانی می‌شود.

### 🧪 تست‌ها

```bash
# تست‌های واحد بک‌اند
cd apps/backend && ./gradlew test

# فرانت‌اند
cd apps/web && npm run test
```

تست‌های موجود در بک‌اند، منطق باز/بسته‌بودن و شمارش معکوس ساعات کاری و مسیرهای خطای احراز هویت را به‌عنوان نقطه شروع پوشش می‌دهند — با پیشرفت پروژه می‌توانید آن‌ها را گسترش دهید.

### 🗺️ محدوده فعلی و نقشه راه

هر آنچه در بخش «ویژگی‌های کلیدی» بالا فهرست شد، کدی واقعی و کاملاً متصل (REST + WebSocket + PostgreSQL/Redis) است — هیچ دکمه یا پاسخ API ساختگی وجود ندارد. طبیعی است پروژه‌ای در این مقیاس فهرستی برای فاز بعدی داشته باشد؛ موارد زیر عمداً هنوز اضافه نشده‌اند تا توضیح این فایل درباره‌ی آنچه واقعاً پیاده‌سازی شده صادقانه باقی بماند:

- **تماس صوتی/تصویری و اشتراک‌گذاری صفحه** — مدل داده و مرز Signaling آماده است (بک‌اند Kotlin مطابق معماری خواسته‌شده مسئول احراز هویت/مدیریت اتاق/Signaling است)، اما پیاده‌سازی کلاینت WebRTC و مسیریابی رسانه از طریق SFU، توسعه‌ای جداگانه نیاز دارد.
- **اپلیکیشن اندروید** — در این تحویل گنجانده نشده است؛ API طراحی‌شده به‌گونه‌ای است که یک کلاینت Jetpack Compose بتواند از همان Endpointها استفاده کند.
- **جستجوی هوشمند در کل Workspace (RAG)** — دستیار هوش مصنوعی روی محتوایی که در اختیارش می‌گذارید (یک گفتگو، یک سند، یک فهرست وظیفه) عالی کار می‌کند؛ ایندکس‌کردن کل Workspace در یک پایگاه برداری برای جستجوی معنایی باز، پروژه‌ای مکمل و بعدی است.
- **یکپارچه‌سازی با ابزارهای شخص‌ثالث** (GitHub، GitLab، تقویم/درایو گوگل، Dropbox، Jira) — جدول `integrations` و مدل Webhook Secret آماده است؛ فرایند OAuth و مدیریت Webhook هر ارائه‌دهنده هنوز پیاده‌سازی نشده است.
- **ورود با OAuth / الزام 2FA** — فیلدهای موردنیاز در Schema وجود دارد؛ در حال حاضر فقط احراز هویت با ایمیل و رمز عبور فعال است.
- **داشبورد تحلیلی مدیریتی** — Audit Log و داده‌های اصلی موردنیاز برای ساخت این بخش آماده است.

### 📄 مجوز

MIT — می‌توانید از این پروژه به‌عنوان مرجع آموزشی یا پایه محصول خودتان استفاده کنید.

</div>

---

<a id="-中文"></a>
## 🇨🇳 中文

### 项目概述

PULSE 是一个全栈、多租户的团队协作操作系统。一个组织(Organization)下包含多个工作空间(Workspace),每个工作空间下包含团队(Team)、频道(Channel)、项目(Project)和文档(Document),每个用户可以同时属于多个工作空间,并通过五级角色模型(`OWNER`、`ADMIN`、`MANAGER`、`MEMBER`、`GUEST`)进行权限控制。

### ✨ 核心功能

| 模块 | 说明 |
|---|---|
| **工作空间与权限控制** | 组织 → 工作空间 → 团队 → 频道 → 项目 → 任务,五级角色,成员管理与邀请 |
| **实时聊天** | 公开/私密频道、私信、话题(Thread)、回复、表情反应、提及(`@user`、`@here`、`@everyone`)、置顶、编辑、删除、正在输入提示、实时在线状态、未读计数、全文搜索 —— 全部通过 WebSocket(STOMP)实时推送 |
| **在线状态** | 在线 / 离开 / 忙碌 / 请勿打扰 / 离线,基于 Redis 实现,断线后自动超时为离线,并实时广播给所有已连接客户端 |
| **看板与项目管理** | 待规划 → 待办 → 进行中 → 评审 → 测试 → 已完成,拖拽操作会实时同步到团队每个人的看板,支持优先级、标签、截止日期、评论、依赖关系 |
| **日历与会议** | 月视图、活动/会议/截止日期/提醒、参与者与出席回复(RSVP)、会议记录与行动项 |
| **可自定义的营业时间** | 每个工作空间自行录入**每周各天**的营业时间(或将某天设为全天休息)——表单完全为空白状态,数据全部来自用户输入,没有任何硬编码。保存后,后端会实时计算工作空间当前是否处于营业状态,并给出距离下一次状态变化的倒计时。 |
| **协同文档** | 类似 Notion 的块编辑器,通过 WebSocket 实现多人实时协同编辑,自动保存,完整的版本历史与恢复功能,收藏夹 |
| **通知中心** | 提及、任务分配、评论、会议、文档变更、截止日期——均通过用户专属的 WebSocket 队列即时送达 |
| **仪表盘** | 我的任务、未读消息、进行中的项目、即将开始的会议、在线成员、通知数量——统一视图一目了然 |
| **团队中心** | 展示所有成员、角色、职位及实时在线状态,一键邀请新成员 |
| **AI 助手** | 具备权限感知能力:只会基于当前请求本就有权访问的内容进行推理(例如你所在的频道)。已接入 Anthropic Messages API,支持对话/会议摘要、提取行动项、任务建议、文档摘要、改写、翻译、生成项目状态报告以及智能回复建议。 |
| **命令面板** | 按 `Ctrl/Cmd + K` 即可快速搜索与跳转 |
| **任务自动化** | 即时规则(被提及时通知、分配任务时通知负责人、文档变更时通知项目成员),以及一个用于"截止日期前一天提醒"的定时后台任务 |
| **安全性** | JWT 访问令牌 + 可轮换的刷新令牌、BCrypt 密码哈希、服务端强制执行的按工作空间权限控制、针对敏感操作(邀请、创建工作空间/团队及权限相关事件)的审计日志 |
| **主题** | 内置 5 种主题——**Windows 11 浅色**、**Windows 11 深色**、**Windows 默认**、**红色**、**蓝色**——均采用 Fluent 风格设计规范,可在设置中实时切换 |
| **多语言** | 英文、فارسی(波斯语)、中文——波斯语完整支持**从右到左(RTL)**排版,英文/中文为**从左到右(LTR)**排版,均可实时切换 |

### 🧱 技术栈

| 层级 | 技术 |
|---|---|
| Web 客户端 | Next.js 14、React 18、TypeScript、Tailwind CSS、Radix UI、Framer Motion、Zustand |
| 后端 | Kotlin、Spring Boot 3(Web、Security、Data JPA、WebSocket)、PostgreSQL、Flyway |
| 实时通信 | 基于 SockJS 的 STOMP over WebSocket、Redis(在线状态、发布/订阅) |
| 认证 | JWT(访问令牌 + 刷新令牌)、BCrypt |
| AI | Anthropic Messages API(可选——未配置时应用其余功能仍可完整运行) |
| 基础设施 | Docker 与 Docker Compose |

### 📁 项目结构

```
pulse-platform/
├── apps/
│   ├── backend/                  Kotlin + Spring Boot 后端服务
│   │   └── src/main/kotlin/com/pulse/
│   │       ├── domain/            JPA 实体
│   │       ├── repository/        Spring Data 数据仓库
│   │       ├── dto/                请求/响应数据模型
│   │       ├── service/            业务逻辑
│   │       ├── controller/         REST 接口
│   │       ├── ws/                 STOMP WebSocket 处理器
│   │       ├── security/           JWT 与 STOMP 认证
│   │       ├── scheduler/          后台定时任务
│   │       └── config/             Security、WebSocket、Redis、OpenAPI 配置
│   └── web/                       Next.js + TypeScript 客户端
│       └── src/
│           ├── app/                 App Router 页面
│           ├── components/          UI、聊天、看板、日历、文档、仪表盘组件
│           ├── hooks/                WebSocket 客户端、在线状态心跳
│           ├── lib/                  API 客户端、多语言、工具函数
│           └── store/                Zustand 状态管理(认证、工作空间、界面设置)
├── docker-compose.yml
└── .env.example
```

### ✅ 环境要求

- **Docker** 与 **Docker Compose**(推荐方式),**或**
- 后端需要 **JDK 17 及以上**版本与 **Gradle 8.x**,Web 客户端需要 **Node.js 20 及以上**版本与 **npm**,并需自备本地 **PostgreSQL 16** 与 **Redis 7**

### 🚀 安装步骤

将下载的项目压缩包解压到本地目录,然后进入该目录。

#### 方式一——Docker Compose(推荐,仅需一条命令)

```bash
cp .env.example .env
docker compose up --build
```

该命令会启动 PostgreSQL、Redis、Spring Boot 编写的 API 服务(端口 `8080`)以及 Next.js Web 客户端(端口 `3000`)。后端启动时会自动执行 Flyway 数据库迁移。打开 **http://localhost:3000**,注册第一个账户即可开始使用。

#### 方式二——手动分别启动各服务

**1. 数据库服务**

```bash
docker run -d --name pulse-postgres -e POSTGRES_DB=pulse -e POSTGRES_USER=pulse -e POSTGRES_PASSWORD=pulse -p 5432:5432 postgres:16-alpine
docker run -d --name pulse-redis -p 6379:6379 redis:7-alpine
```

(或者将后端指向你已有的任意 PostgreSQL/Redis 实例。)

**2. 后端**

```bash
cd apps/backend
# 如果尚未包含 Gradle wrapper 文件,可通过本地已安装的 Gradle 生成一次:
gradle wrapper --gradle-version 8.8
./gradlew bootRun
```

API 服务将运行在 **http://localhost:8080**(Swagger 文档位于 `/swagger-ui.html`)。

**3. Web 客户端**

```bash
cd apps/web
npm install
cp .env.example .env.local
npm run dev
```

应用将运行在 **http://localhost:3000**。

**4. 安装所需依赖库(若未直接使用上述命令)**

```bash
# 后端依赖已在 apps/backend/build.gradle.kts 中声明,
# 首次执行 gradle 或 ./gradlew 时会自动下载安装,无需手动操作。

# 前端依赖安装:
cd apps/web && npm install
```

### ⚙️ 环境变量

| 变量 | 所属 | 说明 |
|---|---|---|
| `DB_NAME`, `DB_USER`, `DB_PASSWORD` | 后端 | PostgreSQL 连接信息 |
| `JWT_SECRET` | 后端 | 用于签发 JWT 的密钥——**生产环境务必修改** |
| `CORS_ORIGINS` | 后端 | 允许访问的前端源地址,多个用逗号分隔 |
| `AI_API_KEY`, `AI_MODEL` | 后端 | 可选。若不需要 AI 功能,将 `AI_API_KEY` 留空即可 |
| `NEXT_PUBLIC_API_URL` | 前端 | 后端 REST API 的基础地址 |
| `NEXT_PUBLIC_WS_URL` | 前端 | WebSocket(SockJS)地址,通常为 `<api-url>/ws` |

### 🕒 营业时间(完全由用户自行配置)

在**设置 → 营业时间**页面(日历页面中也会展示),每个工作空间录入**自己的**排班——每周各天的开始/结束时间,或选择"全天休息"——代码中不包含任何预设数值。保存后,页面会实时显示**当前营业**或**当前休息**的状态徽标,并展示由服务端计算、客户端每秒刷新一次的倒计时(例如"距打烊还有 2 小时 14 分钟"或"距营业还有 6 小时 40 分钟")。

### 🧪 测试

```bash
# 后端单元测试
cd apps/backend && ./gradlew test

# 前端测试
cd apps/web && npm run test
```

已包含的后端测试覆盖了营业时间的营业/休息状态判断、倒计时逻辑以及认证失败场景,作为起点;你可以随着功能扩展继续补充测试用例。

### 🗺️ 当前范围与后续规划

上文"核心功能"中列出的一切均为真实可用、端到端连通(REST + WebSocket + PostgreSQL/Redis)的代码——不存在任何虚假按钮或伪造的接口响应。像这样规模的项目自然会有下一阶段的规划清单;以下内容是有意暂未包含的,以确保本说明对"实际已实现内容"的描述始终真实准确:

- **语音/视频通话与屏幕共享**——数据模型与信令边界已就绪(按照需求中的架构,Kotlin 后端负责认证、房间管理与信令),但 WebRTC 客户端本身以及基于 SFU 的媒体路由需要单独开发。
- **Android 应用**——本次交付未包含;REST/WebSocket 接口的设计已支持未来使用相同接口构建 Jetpack Compose 客户端。
- **全工作空间范围的 AI 搜索(RAG)**——AI 助手在处理你主动提供的内容(一段对话、一份文档、一份任务列表)时效果很好;若要将整个工作空间内容索引进向量数据库以支持开放式语义搜索,则需要作为后续项目单独开发。
- **第三方集成**(GitHub、GitLab、Google 日历/云端硬盘、Dropbox、Jira)——`integrations` 数据表与 Webhook 密钥机制已经就绪;各服务商具体的 OAuth 授权流程与 Webhook 处理逻辑尚未实现。
- **OAuth 登录 / 强制启用双因素认证(2FA)**——数据库结构已包含相关字段;目前仅接入了邮箱+密码的登录方式。
- **管理员数据分析仪表盘**——审计日志与核心数据均已就绪,可在此基础上继续构建。

### 📄 许可协议

MIT 许可——你可以将本项目作为学习参考,或作为自己产品的基础进行二次开发。
