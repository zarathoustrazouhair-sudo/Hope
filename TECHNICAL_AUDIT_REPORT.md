# 📋 RAPPORT D'AUDIT TECHNIQUE : STABILITÉ & CONFORMITÉ (PHASES 1 & 2)

**Auteur :** Jules (Architecte Senior Android)
**Date :** 12 Février 2026
**Projet :** SyndicApp (Phase 2 - Data & Domain Validée)
**Version :** 1.0.0-alpha

---

## 1. 📅 RÉCAPITULATIF CHRONOLOGIQUE

Ce rapport clôture les **Phases 1 (Infrastructure)** et **2 (Data & Domain)** du développement de l'application SyndicApp. L'objectif était de bâtir un socle solide, Offline-First, et strictement compatible avec l'API 23 (Android 6.0).

### **Phase 1 : Infrastructure (Validée)**
*   **Structure Projet :** Initialisation d'un projet **Single Module** (`:app`) avec Kotlin DSL et Version Catalog (`libs.versions.toml`).
*   **Configuration Gradle :** Définition stricte des dépendances (Hilt, Room, WorkManager, Supabase) et activation du **Core Library Desugaring**.
*   **Sécurité :** Mise en place de l'injection des secrets Supabase via `local.properties` et `BuildConfig` (Environment Variables pour CI/CD).
*   **CI/CD :** Workflow GitHub Actions (`build.yml`) fonctionnel pour la compilation Debug APK.

### **Phase 2 : Data & Domain (Validée)**
*   **Base de Données SQL (Supabase) :** Script `setup_database.sql` complet incluant les tables `profiles`, `incidents`, `residence_config`, `transactions` et les politiques de sécurité RLS.
*   **Base de Données Locale (Room) :** Implémentation des Entités (`UserEntity`, `IncidentEntity`, `TransactionEntity`) et des DAOs avec requêtes complexes pour les KPIs financiers.
*   **Repositories (Domain Logic) :** Implémentation de `IncidentRepository` (Sync Offline-First), `UserRepository` (Upsert logic), et `TransactionRepository` (Moteur financier).
*   **WorkManager (Background Sync) :**
    *   `UploadIncidentWorker` : Upload immédiat avec politique de Retry exponentielle.
    *   `SyncIncidentsWorker` : Synchronisation périodique (Download).
    *   `MonthlyDebitWorker` : Génération automatique des cotisations mensuelles (Idempotent).

---

## 2. 📱 ANALYSE DE COMPATIBILITÉ (ANDROID 6.0 / API 23)

L'application respecte **strictement** la contrainte `minSdk = 23`. Aucune API nécessitant Android 7+ (API 24+) n'est utilisée sans alternative ou polyfill.

### **Mécanismes de Rétro-compatibilité :**
1.  **Core Library Desugaring :**
    *   **Problème :** Les classes `java.time` (`Instant`, `LocalDate`, `ZoneId`) natives d'Android nécessitent API 26+.
    *   **Solution :** Activation de `isCoreLibraryDesugaringEnabled = true` dans `build.gradle.kts`. Cela permet d'utiliser `java.time` sur API 23+ via la librairie `com.android.tools:desugar_jdk_libs`.
    *   **Vérification :** Le code utilise `java.time.Instant` et `java.util.Date.from(Instant)` sans crash sur les vieux terminaux.

2.  **WorkManager :**
    *   Utilisation de la version `2.9.0` qui gère nativement la compatibilité API 14+. Le scheduling des tâches de fond est garanti sur Android 6.0 (via `AlarmManager` ou `JobScheduler` selon la disponibilité).

3.  **Jetpack Compose & Material 3 :**
    *   Bien que Material 3 soit récent, la librairie est compatible API 21+. L'usage de composants UI (à venir en Phase 3) est donc sécurisé pour API 23.

---

## 3. 💰 AUDIT DU MOTEUR COMPTABLE (LOGIQUE DE DONNÉES)

Le cœur financier de l'application repose sur une architecture "Event Sourcing" simplifiée via la table `transactions`. Il n'y a **aucun stockage de solde**, tout est recalculé à la volée pour garantir l'intégrité.

### **Calcul des Soldes :**
*   **Solde Résident :**
    *   **Formule :** `SUM(COTISATIONS) + SUM(PAIEMENTS)` pour un `userId` donné.
    *   **Logique :** Les `COTISATIONS` sont stockées en négatif (ex: -250.00). Les `PAIEMENTS` en positif (+250.00). Une somme à 0 signifie que le résident est à jour. Une somme négative signifie un retard.
*   **Solde Global (Trésorerie Réelle) :**
    *   **Formule :** `SUM(PAIEMENTS) + SUM(DEPENSES)`.
    *   **Logique :** On ignore les `COTISATIONS` (qui sont virtuelles/dettes). On ne compte que l'argent réellement entré (`PAIEMENT`) et sorti (`DEPENSE`). C'est le "Cash Flow".

### **Stratégie d'Idempotence (`MonthlyDebitWorker`) :**
Le worker de débit mensuel est conçu pour être exécuté périodiquement sans jamais doubler un débit.
1.  **Trigger :** Le worker s'exécute (idéalement le 1er du mois).
2.  **Vérification (Check) :** Il appelle `transactionRepository.hasCotisationForMonth(userId, now)`.
3.  **Requête SQL :** `SELECT COUNT(*) FROM transactions WHERE userId = :id AND type = 'COTISATION' AND date >= :startOfMonth AND date <= :endOfMonth`.
4.  **Action :** Si et seulement si `count == 0`, alors une nouvelle transaction de type `COTISATION` est insérée localement.
5.  **Sécurité :** Même si le worker est relancé 50 fois le même jour, la condition `count > 0` bloquera toute nouvelle insertion.

---

## 4. 🛡️ TESTS DE STABILITÉ ET ROBUSTESSE

L'architecture **Offline-First** via Room est le garant de la stabilité.

### **Résilience Réseau :**
*   **Upload :** `UploadIncidentWorker` utilise une `BackoffPolicy.EXPONENTIAL`. En cas d'échec (timeout, serveur down), le WorkManager réessaiera avec des délais croissants (10s, 20s, 40s...), garantissant que la donnée finit par arriver sans vider la batterie.
*   **Download :** `SyncIncidentsWorker` utilise une politique `Retry` simple. En cas d'échec, la synchronisation est reportée, mais l'utilisateur continue de voir ses données locales (Room).

### **Gestion des Conflits (Profils) :**
*   **Problème Identifié :** Conflit potentiel entre la création locale d'un profil et le Trigger SQL `on_auth_user_created`.
*   **Solution Appliquée :** `UserRepositoryImpl` utilise la méthode `upsert` (Update or Insert). Si le trigger a déjà créé la ligne, l'application mettra simplement à jour les champs manquants sans provoquer d'erreur de Clé Primaire (PK Violation).

---

## 5. 🏗️ ÉTAT DES LIEUX DU SEED DATA

Le script `setup_database.sql` contient les instructions précises pour initialiser la matrice des 15 résidents de la résidence "Amandier B".

*   **Mapping AP1-AP15 :** Les 15 résidents (Ayazi Adnan, Dehbi Fatima, etc.) sont présents dans le script `INSERT`.
*   **Structure :** Chaque entrée possède :
    *   Un UUID placeholder (à remplacer par les vrais `auth.uid` en production).
    *   Le numéro d'appartement exact (`apartment_number` = '1' à '15').
    *   Le bâtiment (`building` = 'Amandier B').
    *   Le rôle par défaut (`RESIDENT`, sauf AP8 `SYNDIC`).
*   **Prêt pour l'UI :** La future interface "Matrice" pourra requêter `userDao.getAllUsersSync()` et trier par `apartment_number` pour afficher la grille instantanément.

---

## 6. 🏁 CONCLUSION DE L'ARCHITECTE

L'audit révèle une infrastructure **saine, robuste et conforme**.

*   ✅ **Architecture :** Clean Architecture respectée (Separation of Concerns).
*   ✅ **Data Layer :** Room est correctement configuré comme source unique de vérité.
*   ✅ **Business Logic :** Les règles financières (Runway, Recouvrement, Solde) sont implémentées avec précision mathématique.
*   ✅ **Compatibilité :** Android 6.0 est supporté nativement.

**Recommandation :**
Le système est prêt pour la **Phase 3 : UI / Cockpit**.
La couche de données est capable d'alimenter les ViewModels nécessaires à l'affichage du Dashboard financier et de la Matrice des résidents sans risque de crash ou d'incohérence comptable.

**Validé pour passage en production (Phase 3).**
