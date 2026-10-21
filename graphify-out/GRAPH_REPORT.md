# Graph Report - GG_Pocket  (2026-10-21)

## Corpus Check
- 373 files · ~171,059 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2515 nodes · 6303 edges · 134 communities (85 shown, 49 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 527 edges (avg confidence: 0.85)
- Token cost: 604,260 input · 0 output

## Community Hubs (Navigation)
- Account Forms Views (RegisterView.vue)
- Fake Club E2E Mock
- Member Services & Repos (MemberRepository)
- Audit Log Backend
- Registration Code Admin API
- Header-Only CSRF Handler
- Rate Limited Exception
- Database Settings Check
- Generated API Types
- Member Services & Repos (Failures)
- Profile & Devices UI
- Member Repository & Status (MemberStatus)
- Auth Store & App Shell (auth.ts)
- Admin API Client & Audit UI
- Profile & Theme UI
- REST Controllers
- OpenAPI Request DTOs (io.swagger.v3.oas.annotations.)
- Deployment Environment
- Member Entity
- Role-Based Integration Tests
- Account Forms Views (OnboardingView.vue)
- Guard Unit Tests
- Member Principal Controllers
- Auth Controller Imports
- ArchUnit Rules
- Error Codes
- Error Handling ITs
- Router & UI Gallery
- Registration Codes Backend
- Registration Codes UI
- Device Detection
- Me Endpoint ITs
- Members List UI
- Auth Store & App Shell (AppHeader.vue)
- Auth Flow ITs
- Rate Limiting & Avatars
- Member Repository & Status (list)
- Security Filter Chain (CurrentMemberFilter.java)
- Member Admin ITs
- Member Admin Service
- Demo Seed & First Admin (RegistrationCodeRepository)
- Session ITs
- App Config & Metadata
- Spring Configuration
- OpenAPI Request DTOs (ProfileUpdateRequest.java)
- Session Store
- Current Member Filter Tests
- API Exception Handler
- Security Filter Chain (SecurityConfig.java)
- Demo Seed & First Admin (DemoSeeder.java)
- Device Service
- Application Services
- Client Address
- Image Sanitizer
- Password Reset Token
- Problem Details Responses
- Security Error Handler
- Error Categories
- Registration Code Tests
- Spring Boot Entry
- Member Governance Tests
- Timestamped Entity
- Deployment Guards
- Nickname & Age Tests
- Install Prompt & Shell
- Password Policy Tests
- Demo Seed Guard Tests
- Rate Limit ITs
- Member Detail & Formatting
- Auth Store & App Shell (LoginView.vue)
- Avatar & Hold Color
- Vite Config
- Changelog Tooling (changelog.py)
- CSRF Middleware
- Prettier Config
- Bootstrap Script
- GitHub Setup Script
- Backup Now Script
- Root TS Config
- Phase 2 Pipeline Docs (10-app-role.sh)
- Backup Health Script
- Backup Loop Script
- Restore Data Script
- Maven Artifact
- Frontend Tooling
- Frontend Dev Dependencies
- Deployment Operations Docs
- Frontend Core Deps & Components
- Frontend npm Scripts
- Deploy Scripts
- App TS Config
- Node TS Config
- Frontend Runtime Deps
- Deploy Shell Library
- Vitest TS Config
- Role Enum
- Restore Test Script
- Roles & Governance Spec
- Edge & Spring Config
- Data Model Spec
- Backend Security Design
- OpenAPI Contract Decisions
- Theme Design
- New Session UX Spec
- Prod Config
- Backend Domain Layout
- Ops Overview
- Test Infrastructure Docs
- Stack & State Spec
- Club Settings & Management
- Product Identity
- Phase 1 Screenshots
- Phase 2 Pipeline Docs (Plan de la phase 2 — Environne)
- Compose Stack & Config
- Phase 0 Decisions (KICKOFF_PROMPT.md (spécificati)
- Phase 1 Plan & Release (Plan de la phase 1 — Comptes e)
- Auth Spec
- CI Workflow Jobs
- Project Docs Hub
- Route Label Domain
- Phase 0 Screenshots
- Oblyk Topo References
- Business Rules Spec (Moteur de badges déterministe )
- Safety Rules Spec
- Architecture & PWA Docs
- Business Rules Spec (Jour de grimpe (deux séances l)
- Phase 0 Decisions (DECISIONS.md (journal des déci)
- Phase 1 Plan & Release (Phase 1 — Comptes et rôles)
- Changelog Tooling (CHANGELOG (Notes de version))

## God Nodes (most connected - your core abstractions)
1. `Member` - 78 edges
2. `Role` - 69 edges
3. `DECISIONS.md (journal des décisions)` - 62 edges
4. `ErrorCode` - 57 edges
5. `MemberRepository` - 57 edges
6. `BusinessException` - 53 edges
7. `vue-i18n` - 49 edges
8. `vue` - 40 edges
9. `RegistrationCode` - 37 edges
10. `IntegrationTest` - 32 edges

## Surprising Connections (you probably didn't know these)
- `Edge Caddy proxy with Let's Encrypt TLS` --implements--> `edge/compose.yml (Caddy edge proxy)`  [INFERRED]
  docs/DEPLOYMENT.md → deploy/edge/compose.yml
- `Pas de TODO dans le code (une ligne dans ROADMAP.md)` --references--> `KICKOFF_PROMPT.md (spécification produit, source de vérité)`  [INFERRED]
  CONTRIBUTING.md → docs/KICKOFF_PROMPT.md
- `index.html (PWA shell)` --conceptually_related_to--> `Optional dev basic auth (level 2 closure)`  [AMBIGUOUS]
  frontend/index.html → docs/DEPLOYMENT.md
- `Release workflow` --implements--> `Release process (prepare-release PR, tag, prod/X.Y.Z, approval)`  [INFERRED]
  .github/workflows/release.yml → docs/DEPLOYMENT.md
- `D-065 — Release : PR ouverte par le robot, CI relancée à la main` --references--> `CHANGELOG (Notes de version)`  [INFERRED]
  docs/DECISIONS.md → CHANGELOG.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Règles de comptage partagées (jour de grimpe, classements, badges, fuseau Europe/Paris)** — docs_kickoff_prompt_climbing_day, docs_kickoff_prompt_monthly_ranking_freeze, docs_kickoff_prompt_badge_engine, docs_kickoff_prompt_europe_paris_timezone, docs_kickoff_prompt_ascent [EXTRACTED 1.00]
- **Compose base plus per-environment overlays** — deploy_compose, deploy_compose_dev, deploy_compose_prod, deploy_compose_ci [EXTRACTED 1.00]
- **Oblyk topo reference screens for the GG Pocket topo** — docs_references_oblyk_topo_sort_menu, docs_references_oblyk2_topo_route_list, docs_references_oblyk3_topo_3d_wall, docs_references_oblyk4_route_detail, docs_references_oblyk [EXTRACTED 1.00]
- **Topologie serveur (edge, web, api, db, backup)** — deploy_scripts_backup, docs_decisions_d_060, docs_decisions_d_071 [EXTRACTED 1.00]
- **Réglages du compte : appareils, installation PWA, thème, données** — docs_captures_phase_1_mobile_light_appareils_sessions_actives, docs_captures_phase_1_mobile_light_actu_invitation_invitation_installation_pwa, docs_captures_phase_1_mobile_light_profil_choix_theme, docs_captures_phase_1_mobile_light_accueil_tes_donnees [EXTRACTED 1.00]
- **Release pipeline (prepare PR, publish, approved prod deploy)** — _github_workflows_prepare_release_prepare, changelog_py, _github_workflows_release_publish, _github_workflows_release_deploy, docs_deployment_github_environment_approval [EXTRACTED 1.00]
- **Étiquette de voie signature (RouteLabel, HoldSwatch, Kalam, Archivo)** — docs_kickoff_prompt_route_label, docs_design_routelabel, docs_design_holdswatch, docs_design_kalam, docs_design_archivo [EXTRACTED 1.00]
- **Demo seed guardrails across environments** — backend_src_main_resources_application_demo_seed, demoseeder, deploy_compose_dev, deploy_compose_ci, deploy_compose_prod, instancemarkerguard [INFERRED 0.85]
- **Espace gestion de la direction : membres, rôles, journal d'audit, codes** — docs_captures_phase_1_desktop_dark_journal_espace_gestion, docs_captures_phase_1_desktop_dark_membres_gestion_membres, docs_captures_phase_1_desktop_dark_fiche_membre_changement_role, docs_captures_phase_1_desktop_dark_journal_journal_audit, docs_captures_phase_1_mobile_light_codes_code_inscription [INFERRED 0.85]
- **Parcours d'inscription : code du club, fiche, premiers pas** — docs_captures_phase_1_mobile_light_accueil_inscription_par_code, docs_captures_phase_1_mobile_light_codes_code_inscription, docs_captures_phase_1_mobile_light_inscription_fiche_formulaire_inscription, docs_captures_phase_1_mobile_light_premiers_pas_photo_onboarding [INFERRED 0.85]
- **Phase 0 screens captured in light and dark themes** — docs_captures_phase_0_actu_dark_screen, docs_captures_phase_0_actu_light_screen, docs_captures_phase_0_labels_dark_screen, docs_captures_phase_0_labels_light_screen, docs_captures_phase_0_profil_dark_screen, docs_captures_phase_0_profil_light_screen, theme_switcher [INFERRED 0.85]
- **Flux d'authentification par session serveur** — docs_kickoff_prompt_long_session, docs_architecture_auth_flow, docs_architecture_securityconfig, docs_architecture_currentmemberfilter, docs_kickoff_prompt_spa_csrf, docs_architecture_same_origin_caddy [INFERRED 0.85]
- **Garde-fous empêchant le dev/seed de toucher la prod** — docs_decisions_d_035, docs_decisions_d_064, docs_decisions_d_068, docs_decisions_d_071, docs_decisions_demo_seed [INFERRED 0.85]
- **Enquête bug iPhone profil (0.1.1)** — docs_decisions_d_073, docs_decisions_profil_url_filter, docs_decisions_hash_only_asset_names, docs_decisions_load_failure_screen, docs_decisions_stale_chunk, changelog_0_1_1 [EXTRACTED 1.00]
- **Pipeline de release et déploiement** — docs_decisions_d_065, docs_decisions_d_062, docs_decisions_d_066, docs_decisions_d_032, docs_decisions_gg_deploy, github_scripts_setup_github, docs_roadmap_phase_2 [INFERRED 0.85]

## Communities (134 total, 49 thin omitted)

### Community 1 - "Account Forms Views (RegisterView.vue)"
Cohesion: 0.04
Nodes (64): ApiError, ErrorCode, FieldViolation, ProblemDetails, ServerErrorCode, ClientResult, Message, Field (+56 more)

### Community 8 - "Fake Club E2E Mock"
Cohesion: 0.08
Nodes (19): FakeClub, FakeMember, FakeSession, Role, Schemas, MetaOverride, Role, Capture (+11 more)

### Community 0 - "Member Services & Repos (MemberRepository)"
Cohesion: 0.08
Nodes (18): AuditLog, AuditTargetType, PasswordChangeService, PasswordResetService, ResetLink, RegistrationService, PasswordPolicy, RegistrationCodeFormat (+10 more)

### Community 10 - "Audit Log Backend"
Cohesion: 0.08
Nodes (14): AuditEntryView, AuditPage, AuditQueryService, Cursor, AuditAction, AuditEntry, AuditEntryRepository, AuditCursorTest (+6 more)

### Community 106 - "Generated API Types"
Cohesion: 0.33
Nodes (5): components, $defs, operations, paths, webhooks

### Community 11 - "Profile & Devices UI"
Cohesion: 0.07
Nodes (35): Device, PhotoError, squareCrop, useCheckRegistrationCode(), errorMessageKey(), useChangePassword(), useCloseDevice(), useCloseOtherDevices() (+27 more)

### Community 114 - "Member Repository & Status (MemberStatus)"
Cohesion: 0.50
Nodes (3): MemberStatus, ACTIVE, DISABLED

### Community 12 - "Auth Store & App Shell (auth.ts)"
Cohesion: 0.07
Nodes (33): AuthState, CurrentMember, LoginRequest, RegisterRequest, Role, forgetMemberData(), useLogin(), useLogout() (+25 more)

### Community 13 - "Admin API Client & Audit UI"
Cohesion: 0.07
Nodes (34): AuditEntry, MemberDetail, MemberStatus, Role, Meta, useAuditLog(), useChangeRole(), useChangeStatus() (+26 more)

### Community 14 - "Profile & Theme UI"
Cohesion: 0.07
Nodes (31): ThemePreference, install(), formatDate(), applyThemePreference(), isThemePreference(), { copy, copied, isSupported: canCopy }, emit, IOS_STEPS (+23 more)

### Community 15 - "REST Controllers"
Cohesion: 0.16
Nodes (6): AuditController, RegistrationCodeAdminController, MediaController, MetaController, ErrorProbeController, ProbeRequest

### Community 16 - "OpenAPI Request DTOs (io.swagger.v3.oas.annotations.)"
Cohesion: 0.11
Nodes (15): AuditEntryResponse, AuditPageResponse, PasswordResetLinkResponse, RoleChangeRequest, StatusChangeRequest, AuthStateResponse, DeviceResponse, LoginRequest (+7 more)

### Community 18 - "Deployment Environment"
Cohesion: 0.09
Nodes (10): AppMetadata, AppMetadataService, Git, Media, DeploymentEnvironment, AppMetadataServiceTest, InstanceMarkerGuardTest, DEV (+2 more)

### Community 2 - "Role-Based Integration Tests"
Cohesion: 0.08
Nodes (6): IntegrationTest, MemberFixtures, MetaControllerIT, OpenApiContractIT, AuthorizationMatrixIT, Endpoint

### Community 20 - "Account Forms Views (OnboardingView.vue)"
Cohesion: 0.08
Nodes (31): MyProfile, ProfileUpdate, FieldErrors, ProfileDetails, ProfileForm, Step, parseYear(), toForm() (+23 more)

### Community 23 - "Member Principal Controllers"
Cohesion: 0.11
Nodes (8): MemberAdminController, MemberDetailResponse, PermissionsResponse, MemberSummaryResponse, MeController, MediaUrls, MyProfileResponse, MemberPrincipal

### Community 24 - "Auth Controller Imports"
Cohesion: 0.17
Nodes (5): AuthController, MySecurityController, SessionEstablisher, LoginService, CurrentMemberView

### Community 26 - "ArchUnit Rules"
Cohesion: 0.09
Nodes (3): ArchitectureTest, ArchCondition<>, ArchCondition<>

### Community 27 - "Error Codes"
Cohesion: 0.07
Nodes (29): ErrorCode, ACCESS_DENIED, ACCOUNT_DISABLED, BAD_CREDENTIALS, CSRF_TOKEN_INVALID, CURRENT_PASSWORD_WRONG, EMAIL_TAKEN, IMAGE_TOO_LARGE (+21 more)

### Community 29 - "Error Handling ITs"
Cohesion: 0.14
Nodes (3): ClientAddressTest, ApiErrorsIT, ApiExceptionHandlerTest

### Community 3 - "Router & UI Gallery"
Cohesion: 0.05
Nodes (41): DefineLocaleMessage, MessageSchema, HoldColor, RouteStatus, RouteMeta, isRetryable(), createQueryClient(), listenForInstallPrompt() (+33 more)

### Community 30 - "Registration Codes Backend"
Cohesion: 0.15
Nodes (9): CodeSettings, RegistrationCodeAdminService, RegistrationCodeView, RegistrationCode, State, EXHAUSTED, EXPIRED, INACTIVE (+1 more)

### Community 32 - "Registration Codes UI"
Cohesion: 0.11
Nodes (21): RegistrationCode, RegistrationCodeSettings, CodeForm, clubDayBefore(), codeSettings(), endOfClubDay(), toCodeForm(), save() (+13 more)

### Community 33 - "Device Detection"
Cohesion: 0.11
Nodes (18): Browser, DeviceDescription, Platform, DeviceDescriptionTest, CHROME, EDGE, FIREFOX, OPERA (+10 more)

### Community 36 - "Members List UI"
Cohesion: 0.12
Nodes (18): MemberSummary, MemberSortKey, SortDirection, useMembers(), compare(), filterMembers(), normalize(), sortMembers() (+10 more)

### Community 37 - "Auth Store & App Shell (AppHeader.vue)"
Cohesion: 0.13
Nodes (18): Role, ManagementSection, useAuthState(), useCurrentMember(), isAtLeast(), canManage, member, { t } (+10 more)

### Community 4 - "Rate Limiting & Avatars"
Cohesion: 0.06
Nodes (13): AvatarService, TransactionSynchronization, MediaContent, MediaService, MediaFile, MediaUsage, MediaFileRepository, MediaStorage (+5 more)

### Community 45 - "Security Filter Chain (CurrentMemberFilter.java)"
Cohesion: 0.15
Nodes (3): CurrentMemberFilter, SessionCookieConfig, MemberAccessService

### Community 47 - "Member Admin Service"
Cohesion: 0.26
Nodes (4): MemberAdminService, MemberDetailView, MemberSummaryView, Permissions

### Community 48 - "Demo Seed & First Admin (RegistrationCodeRepository)"
Cohesion: 0.22
Nodes (5): RegistrationCodeRepository, FirstStartAdminCodeIT, DemoSeederDeployedDevIT, DemoSeederIT, TestcontainersConfiguration

### Community 51 - "Spring Configuration"
Cohesion: 0.19
Nodes (4): PasswordPolicyConfig, JpaConfig, OpenApiConfig, TimeConfig

### Community 53 - "OpenAPI Request DTOs (ProfileUpdateRequest.java)"
Cohesion: 0.23
Nodes (6): RegisterRequest, Registration, ProfileUpdateRequest, ProfileChange, MemberDetails, MemberIdentity

### Community 54 - "Session Store"
Cohesion: 0.18
Nodes (3): SessionAttributes, StoredSession, MemberSessionStore

### Community 6 - "Demo Seed & First Admin (DemoSeeder.java)"
Cohesion: 0.08
Nodes (10): FirstStartAdminCode, InstanceMarkerGuard, GgProperties, InstanceMarker, InstanceMarkerRepository, DemoAccount, DemoSeeder, DemoSeedGuard (+2 more)

### Community 63 - "Device Service"
Cohesion: 0.20
Nodes (3): DeviceService, DeviceView, TransactionSynchronization

### Community 64 - "Application Services"
Cohesion: 0.18
Nodes (3): MyProfileView, MemberGovernance, Role

### Community 7 - "Image Sanitizer"
Cohesion: 0.08
Nodes (7): CleanImage, ImageSanitizer, ImageType, ImageSanitizerTest, JPEG, PNG, WEBP

### Community 75 - "Error Categories"
Cohesion: 0.17
Nodes (9): ErrorCategory, CONFLICT, FORBIDDEN, INTERNAL, INVALID, NOT_FOUND, PROTOCOL, TOO_MANY_REQUESTS (+1 more)

### Community 9 - "Install Prompt & Shell"
Cohesion: 0.08
Nodes (37): BrowserFacts, installMethod, InviteHistory, ScreenLoadFailure, BeforeInstallPromptEvent, goHome(), repair(), retry() (+29 more)

### Community 17 - "Member Detail & Formatting"
Cohesion: 0.07
Nodes (35): formatDateTime(), formatRelative(), done(), failed(), makeResetLink(), saveRole(), setStatus(), DAY (+27 more)

### Community 28 - "Auth Store & App Shell (LoginView.vue)"
Cohesion: 0.07
Nodes (26): safeReturnPath(), submit(), describedBy, id, model, props, field, model (+18 more)

### Community 44 - "Avatar & Hold Color"
Cohesion: 0.19
Nodes (15): avatarColor(), initialsOf(), channels(), contrastRatio(), darken(), relativeLuminance(), toHex(), boltRing (+7 more)

### Community 80 - "Vite Config"
Cohesion: 0.22
Nodes (8): createViteConfig(), appVersion, pwaOptions, @intlify/unplugin-vue-i18n, @tailwindcss/vite, vite, vite-plugin-pwa, @vitejs/plugin-vue

### Community 85 - "Changelog Tooling (changelog.py)"
Cohesion: 0.29
Nodes (4): notes(), release(), section_body(), sections()

### Community 90 - "CSRF Middleware"
Cohesion: 0.33
Nodes (7): csrfMiddleware(), readCookie(), headerAfter(), CSRF_COOKIE, CSRF_HEADER, SAFE_METHODS, openapi-fetch

### Community 100 - "Prettier Config"
Cohesion: 0.29
Nodes (6): plugins, printWidth, $schema, semi, singleQuote, tailwindStylesheet

### Community 110 - "Bootstrap Script"
Cohesion: 0.60
Nodes (3): say(), bootstrap.sh script, write_env()

### Community 113 - "GitHub Setup Script"
Cohesion: 0.60
Nodes (3): ruleset(), say(), setup-github.sh script

### Community 31 - "Frontend Tooling"
Cohesion: 0.09
Nodes (25): engines, node, name, packageManager, private, type, eslint, eslint-plugin-vue (+17 more)

### Community 35 - "Frontend Dev Dependencies"
Cohesion: 0.08
Nodes (26): devDependencies, eslint, eslint-plugin-vue, eslint-plugin-vuejs-accessibility, @intlify/unplugin-vue-i18n, jsdom, openapi-typescript, @playwright/test (+18 more)

### Community 41 - "Deployment Operations Docs"
Cohesion: 0.16
Nodes (19): Deploy dev workflow, Deploy dev: build job (push dev-<sha> images), Deploy dev: deploy job (SSH, approval), Release: deploy to prod job (approval required), bootstrap.sh server setup script, deploy.sh (pull, start, healthcheck, smoke test, auto rollback), setup-github.sh GitHub setup script, index.html (PWA shell) (+11 more)

### Community 5 - "Frontend Core Deps & Components"
Cohesion: 0.05
Nodes (34): { t }, open, store, { t }, { data: auth, isError, refetch }, install, inviting, route (+26 more)

### Community 61 - "Frontend npm Scripts"
Cohesion: 0.13
Nodes (15): scripts, api:types, build, dev, e2e, e2e:stack, format, format:check (+7 more)

### Community 67 - "Deploy Scripts"
Cohesion: 0.20
Nodes (6): prune(), backup.sh script, GG_IMAGE_TAG, roll_back(), deploy.sh script, rollback.sh script

### Community 71 - "App TS Config"
Cohesion: 0.15
Nodes (12): compilerOptions, noFallthroughCasesInSwitch, noImplicitOverride, noUncheckedIndexedAccess, paths, resolveJsonModule, strict, tsBuildInfoFile (+4 more)

### Community 72 - "Node TS Config"
Cohesion: 0.15
Nodes (12): compilerOptions, allowImportingTsExtensions, lib, module, moduleResolution, noEmit, strict, tsBuildInfoFile (+4 more)

### Community 77 - "Frontend Runtime Deps"
Cohesion: 0.17
Nodes (12): dependencies, @fontsource/kalam, @fontsource-variable/archivo, lucide-vue-next, openapi-fetch, pinia, reka-ui, @tanstack/vue-query (+4 more)

### Community 79 - "Deploy Shell Library"
Cohesion: 0.22
Nodes (4): check_env(), check_tag(), die(), lib.sh script

### Community 91 - "Vitest TS Config"
Cohesion: 0.22
Nodes (8): compilerOptions, lib, tsBuildInfoFile, types, exclude, extends, include, ./tsconfig.app.json

### Community 93 - "Role Enum"
Cohesion: 0.25
Nodes (4): ADMIN, BOARD, CLIMBER, SETTER

### Community 95 - "Restore Test Script"
Cohesion: 0.29
Nodes (6): GG_ENV, GG_IMAGE_TAG, ci-restore-test.sh script, sql(), GG_IMAGE_TAG, restore.sh script

### Community 105 - "Roles & Governance Spec"
Cohesion: 0.40
Nodes (5): MemberGovernance (matrice « qui gère qui », règle pure), ROLE_ADMIN > ROLE_BOARD > ROLE_SETTER > ROLE_CLIMBER, Composants de base (GgButton, GgIconButton, RoleBadge, UserAvatar, EmptyState, SegmentedControl, SkeletonBlock) sur Reka UI, Journal d'audit (AuditLog), Rôles hiérarchiques (Grimpeur, Responsable, Direction, Admin)

### Community 108 - "Edge & Spring Config"
Cohesion: 0.40
Nodes (5): application.yml (shared Spring config), forward-headers-strategy native (behind Caddy), Multipart limits 10MB photos (spec §14), Spring Session JDBC (12h timeout, tables by Flyway V3), Edge Caddy proxy with Let's Encrypt TLS

### Community 111 - "Data Model Spec"
Cohesion: 0.40
Nodes (5): Modèle de données cible (diagramme ER), Code d'inscription du club (RegistrationCode), Badge d'ancienneté (Tout frais → GG fossile), Session (séance), User (membre)

### Community 112 - "Backend Security Design"
Cohesion: 0.40
Nodes (5): ErrorCode / BusinessException / ProblemDetailsSecurityErrorHandler, Origine unique : Caddy sert le front et proxifie /api, SecurityConfig (fermé par défaut, liste blanche, CSRF XSRF-TOKEN), Erreurs Problem Details (RFC 9457) avec code métier, CSRF adapté SPA (jeton cookie + en-tête)

### Community 115 - "OpenAPI Contract Decisions"
Cohesion: 0.50
Nodes (4): Environnement Claude Code sans Java/Docker (D-030, limite PID Playwright), Contrat OpenAPI commité (frontend/src/api/openapi.json, OpenApiContractIT), CI ci.yml (tests, OpenAPI, e2e, gitleaks, scan d'images), Types API générés depuis l'OpenAPI

### Community 116 - "Theme Design"
Cohesion: 0.50
Nodes (3): Thème auto/clair/sombre via data-theme (theme-init.js), Thème sombre « salle fermée », Thème clair « magnésie »

### Community 25 - "Phase 1 Screenshots"
Cohesion: 0.08
Nodes (29): Changement de rôle d'un membre (select + Changer le rôle), Espace gestion (navigation latérale Voies / Membres / Journal / Codes), Journal d'audit (qui a fait quoi, quand, lecture seule), Bandeau d'environnement dev (branche + commit), Gestion des membres (recherche, tri, statut actif/désactivé), Inscription par code du club (J'ai un code du club), Page Tes données (ce qu'on garde et pourquoi), Invitation à installer la PWA (feuille du bas, Plus tard) (+21 more)

### Community 39 - "Phase 2 Pipeline Docs (Plan de la phase 2 — Environne)"
Cohesion: 0.15
Nodes (13): Plan de la phase 2 — Environnements et pipeline, Service backup (pg_dump + photos), Frontal Caddy gg-pocket-edge, Content Security Policy sans script en ligne, Seed de démonstration, Dependabot, Porte SSH gg-deploy, Limiteur de débit en mémoire (+5 more)

### Community 40 - "Compose Stack & Config"
Cohesion: 0.16
Nodes (21): CI job: Deploy config (shellcheck, compose, Caddy), CI job: Docker stack (images, Trivy, e2e, backup/restore), gg.demo-seed config (allowed, password), DemoSeeder, compose.yml (one environment stack), api service (Spring Boot), backup service (profile backup), compose.ci.yml (gg-pocket-ci) (+13 more)

### Community 52 - "Phase 0 Decisions (KICKOFF_PROMPT.md (spécificati)"
Cohesion: 0.18
Nodes (10): KICKOFF_PROMPT.md (spécification produit, source de vérité), Table instance_marker, Contrat OpenAPI commité (OpenApiContractIT), Problem Details (RFC 9457) + ErrorCode, PROGRESS.md (journal des sessions), Session 2026-10-17 — Phase 0, session 1, Session 2026-10-17 — Phase 0, session 2, Session 2026-10-18 — Phase 0, session 3 (+2 more)

### Community 58 - "Phase 1 Plan & Release (Plan de la phase 1 — Comptes e)"
Cohesion: 0.19
Nodes (15): V2__create_member_and_media.sql, V3__create_spring_session.sql, V4__create_registration_and_reset.sql, V5__create_audit_log.sql, Journal d'audit (audit_log), Domaine admin, Domaine auth, Domaine member (+7 more)

### Community 60 - "Auth Spec"
Cohesion: 0.14
Nodes (11): Flux d'authentification (login, session JDBC, cookie glissant), CurrentMemberFilter (relit rôle et statut à chaque requête), Barre d'onglets à l'encre, Session longue serveur 180 jours glissants (Spring Session JDBC), Écran « Mes appareils », Brouillon IndexedDB et file d'attente hors ligne, Passkeys (WebAuthn), phase ultérieure, Lien de réinitialisation sans e-mail (PasswordResetToken, 24 h, usage unique) (+3 more)

### Community 62 - "CI Workflow Jobs"
Cohesion: 0.19
Nodes (14): CI workflow, CI job: Backend (mvnw verify, ArchUnit, OpenAPI contract), CI job: E2E Playwright (iPhone 13 + Desktop Chrome), CI job: Frontend (lint, format, api types, typecheck, test, build), CI job: gitleaks secret scan, Prepare release workflow, Prepare release: open release PR job, Release workflow (+6 more)

### Community 66 - "Project Docs Hub"
Cohesion: 0.18
Nodes (9): Definition of Done, Conventional Commits en anglais, PR en français, squash, vue-i18n précompilé, clés typées (compatible CSP), Motif bg-inserts (grille d'inserts), Branches feature/fix/chore et prod/X.Y.Z, Code d'inscription admin de premier démarrage (24 h, usage unique), Glossaire UI/code (Voie=Route, Corde=Cord, Séance=Session, Ascension=Ascent…), CLAUDE.md (manuel d'opération) (+1 more)

### Community 68 - "Route Label Domain"
Cohesion: 0.19
Nodes (13): Police Archivo (variable, interface et chiffres), HoldSwatch (prise dessinée avec boulon), Police Kalam (nom des voies uniquement), RouteLabel.vue (étiquette de voie), Cord (corde 1 à 29), Favorite (coup de cœur, 2 actifs max), Grade (cotation 4a à 7c, rang numérique), HoldColor (couleurs de prises) (+5 more)

### Community 76 - "Phase 0 Screenshots"
Cohesion: 0.21
Nodes (11): Bottom tab navigation (Actu, Topo, A faire, Planning, Podium), Dev environment banner (branch name), GG brand palette (orange, olive/lime green, black, cream), Theme selector (Auto / Clair / Sombre), Actu screen (dark) - empty feed state, Actu screen (light) - empty feed state, Profil screen (dark) - placeholder with theme selector, Profil screen (light) - placeholder with theme selector (+3 more)

### Community 88 - "Oblyk Topo References"
Cohesion: 0.31
Nodes (9): Oblyk (existing climbing topo app used by the club), Dismantled route state (demontee), Route label (relais number, hold colors, handwritten name, grade, Derniere chance / Demontee tags), Route labels gallery (dark), Route labels gallery (light), Reference: Oblyk topo route list sorted by grade (points, relais, likes, ascents), Reference: Oblyk 3D wall map of Gravelines Grimp sectors, Reference: Oblyk route detail sheet (photo, info, commentaires, videos, styles) (+1 more)

### Community 89 - "Business Rules Spec (Moteur de badges déterministe )"
Cohesion: 0.25
Nodes (8): Icônes Lucide + icônes métier dessinées, pas d'emoji, Fil d'actu (cartes de séance façon Strava, réactions), Ascent (ascension / essai), Moteur de badges déterministe et recalculable, Friendship (amitié), Parcours Nouvelle séance (saisie en 30 s), RouteCheck (« je sais la faire »), Écran À faire (En mouli / En tête)

### Community 94 - "Safety Rules Spec"
Cohesion: 0.29
Nodes (7): GgProperties (préfixe gg, variables GG_*), InstanceMarkerGuard (table instance_marker), Seed de démonstration avec garde-fous cumulés, Environnements local / dev / prod, MediaFile (fichiers servis après contrôle d'accès), Traitement des photos (EXIF/GPS supprimés, ré-encodage, 1600 px), Comptes de test par rôle (GG_ALLOW_DEMO_SEED, DEMO-2026)

### Community 96 - "Architecture & PWA Docs"
Cohesion: 0.32
Nodes (6): Invitation à installer (Android, iOS, navigateur intégré), PWA (manifeste, service worker prompt, network-first), Notes de version « Quoi de neuf ? » depuis CHANGELOG.md, Pipeline de release (prepare-release.yml, release.yml, approbation production), Fichier VERSION unique et SemVer strict, Willem (développeur, commanditaire)

### Community 99 - "Business Rules Spec (Jour de grimpe (deux séances l)"
Cohesion: 0.29
Nodes (6): Bean Clock (heure injectable pour les tests), Fuseau métier Europe/Paris (semaines ISO, mois), Clôture mensuelle des classements le 8 à 04:00 (MonthlyRankingSnapshot), Écran Planning (calendrier mensuel), Podium (classements mensuels), Vie privée et RGPD (page « Tes données », export, suppression, mineurs)

### Community 21 - "Phase 0 Decisions (DECISIONS.md (journal des déci)"
Cohesion: 0.09
Nodes (8): DECISIONS.md (journal des décisions), Phase 3 — Topo, Phase 4 — Séances, Phase 5 — Social, Phase 6 — Podium et statistiques, Phase 7 — Badges, Phase 8 — Finitions et lancement, Phase 9 — Après lancement

### Community 43 - "Phase 1 Plan & Release (Phase 1 — Comptes et rôles)"
Cohesion: 0.14
Nodes (8): Cookie __Host-GG_SESSION, Copie des cookies Safari à l'installation iOS, Lien de réinitialisation 24 h à usage unique, Matrice de rôles (direction / admin), Spring Session JDBC, Version 0.1.0 — 2026-10-21, Session 2026-10-20 — Phase 1 comptes et rôles, Phase 1 — Comptes et rôles

### Community 65 - "Changelog Tooling (CHANGELOG (Notes de version))"
Cohesion: 0.25
Nodes (11): Keep a Changelog, SemVer, Fichiers construits nommés par empreinte (assets/[hash].js), LoadFailureScreen, Filtre iPhone sur les URL contenant « profil », router.onError / stale-chunk.ts, Fichier VERSION, CHANGELOG (Notes de version) (+3 more)

## Ambiguous Edges - Review These
- `Barre de navigation fixe superposée au contenu en capture pleine page` → `Capture: mon profil et réglages (mobile, clair)`  [AMBIGUOUS]
  docs/captures/phase-1/mobile-light-profil.png · relation: references
- `index.html (PWA shell)` → `Optional dev basic auth (level 2 closure)`  [AMBIGUOUS]
  frontend/index.html · relation: conceptually_related_to

## Knowledge Gaps
- **173 isolated node(s):** `GG_ENV`, `GG_IMAGE_TAG`, `GG_IMAGE_TAG`, `fr.gravelinesgrimp:gg-pocket-api`, `MEMBER_DISABLED` (+168 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 800 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **49 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DEPLOYMENT.md (deploy & operate guide)` connect `Deployment Operations Docs` to `Compose Stack & Config`, `Edge & Spring Config`, `CI Workflow Jobs`, `Prod Config`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **What connects `GG_ENV`, `GG_IMAGE_TAG`, `GG_IMAGE_TAG` to the rest of the system?**
  _173 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Account Forms Views (RegisterView.vue)` be split into smaller, more focused modules?**
  _Cohesion score 0.042283950617283954 - nodes in this community are weakly interconnected._
- **Why does `index.html (PWA shell)` connect `Deployment Operations Docs` to `Router & UI Gallery`?**
  _High betweenness centrality (0.087) - this node is a cross-community bridge._
- **Should `Fake Club E2E Mock` be split into smaller, more focused modules?**
  _Cohesion score 0.07890070921985816 - nodes in this community are weakly interconnected._

### Low-confidence Hints
_AMBIGUOUS edges — the extractor was unsure. Verify before acting on these._

- **What is the exact relationship between `Barre de navigation fixe superposée au contenu en capture pleine page` and `Capture: mon profil et réglages (mobile, clair)`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **What is the exact relationship between `index.html (PWA shell)` and `Optional dev basic auth (level 2 closure)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._