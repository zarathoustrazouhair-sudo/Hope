# Syndic La Mondiale - Application de Gestion de Copropriété

> **Version:** 1.0.0-RC1
> **Architecture:** Clean Architecture + MVVM + Hilt + Room + Jetpack Compose
> **Compatibilité:** Android 6.0 (API 23) - Android 14+

## 🚀 Introduction

L'application **Syndic La Mondiale** est une solution "Offline-First" dédiée à la gestion simplifiée des petites et moyennes copropriétés. Elle centralise la gestion financière, la communication (Blog) et le suivi des incidents techniques.

## 🛠 Installation (APK)

1.  Télécharger le fichier `app-release.apk` généré.
2.  Transférer le fichier sur le smartphone du Syndic.
3.  Autoriser l'installation depuis des sources inconnues si nécessaire.
4.  Installer et lancer l'application.

## ⚙️ Configuration Initiale (Master Setup)

Au premier lancement, l'Assistant de Démarrage (Wizard) guide le Syndic :
1.  **Nom de la Résidence.**
2.  **Code Maître (PIN)** : Ce code sécurise l'accès aux fonctions d'administration (Finance, Blog).
3.  **Paramètres Financiers** : Saisie des charges fixes (Salaire Concierge, Ménage, Eau, Électricité...).
4.  **Initialisation** : L'application génère automatiquement 15 comptes résidents (AP1 à AP15) avec un PIN par défaut (`0000`).

## 🆘 Procédure de "Master Reset"

Si le Syndic commet une erreur critique lors de la configuration initiale (ex: mauvais Code Maître ou nom de résidence incorrect) et ne peut plus accéder à l'interface, voici la procédure de réinitialisation complète :

1.  Aller dans les **Paramètres Android** du téléphone.
2.  Rubrique **Applications**.
3.  Sélectionner **Syndic La Mondiale**.
4.  Aller dans **Stockage**.
5.  Appuyer sur **Effacer les données** (Clear Data) et **Vider le cache**.
6.  Relancer l'application : elle repartira de zéro (Écran de Bienvenue).

## 🔒 Sécurité

-   **PINs Hashés** : Tous les codes sont stockés hashés (SHA-256).
-   **Isolation** : Les données sont stockées localement (Room Database).
-   **Sauvegarde** : Les PDFs (Reçus/Bons) sont générés dans le dossier `Documents/Receipts` du téléphone.

## 📞 Support Technique

Pour toute remontée de bug, merci de fournir le modèle du téléphone et la version d'Android.
