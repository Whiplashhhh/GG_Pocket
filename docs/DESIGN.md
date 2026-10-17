# GG Pocket — design system

Ce document fixe l'identité visuelle de l'appli. Il précède le code : tout écran doit pouvoir se justifier par une règle d'ici.
Les tokens vivent dans `frontend/src/styles/tokens.css`, les composants de base dans `frontend/src/components/ui/`.
Une galerie des composants est disponible en local sur `/dev/galerie` (absente des builds de prod).

## 1. Point de départ : la salle, pas un kit

Ce qu'on voit en entrant au mur de Gravelines Grimp, et ce que l'appli doit rappeler :

- **Le mur et sa grille d'inserts.** Des panneaux percés de trous de vis (les inserts) à intervalles réguliers, sur lesquels on boulonne les prises.
  → motif de fond discret « grille d'inserts » (points en quinconce) pour les zones d'accueil et les états vides.
- **Les prises en résine**, de toutes les couleurs, avec leur trou de boulon au centre.
  → la pastille de couleur de prise n'est pas un rond parfait : c'est une petite prise dessinée, avec son boulon.
- **L'étiquette scotchée au pied de la voie** : un bout de papier, un nom écrit au feutre, une cotation.
  → l'**étiquette de voie**, composant signature (§5), en papier, avec le nom écrit à la main.
- **Les numéros de corde peints**, gros, étroits, lisibles de loin.
  → les numéros de corde en Archivo très condensé et très gras.
- **La magnésie** : un blanc cassé poudreux, jamais un blanc d'écran.
  → le fond clair est « magnésie », pas `#FFFFFF`.
- **Le logo du club** : silhouette orange, typo pinceau noire, sommet olive ; deux « G », l'un olive, l'autre orange.
  → orange et olive sont les deux couleurs d'accent, le noir du logo devient l'encre.

Le reste de l'interface reste calme : fond uni, filets fins, typographie disciplinée. L'audace est concentrée sur l'étiquette de voie,
la barre d'onglets à l'encre et le moment d'enregistrement d'une séance.

## 2. Couleurs

Trois systèmes qui ne se mélangent pas :

1. **Neutres** (fond, surfaces, encre, filets) : portent 90 % de l'écran.
2. **Couleurs du club** (orange, olive) : marque, action principale, réussite.
3. **Couleurs de prises** (`HoldColor`, configurables) : réservées à l'identification des voies. Jamais utilisées pour autre chose.

### Thème clair « magnésie »

| Token | Hex | Rôle | Contraste |
|---|---|---|---|
| `--color-bg` · magnésie | `#F4F1EA` | Fond de l'appli | — |
| `--color-surface` · papier | `#FFFDF8` | Feuilles (sheets), champs, zones posées | — |
| `--color-surface-sunken` · béton | `#EAE5DA` | Zones en retrait, squelettes de chargement | — |
| `--color-ink` · résine noire | `#17140F` | Texte principal, barre d'onglets | 16,3:1 sur magnésie |
| `--color-ink-muted` · corde usée | `#5E574C` | Texte secondaire | 6,3:1 sur magnésie |
| `--color-rule` · trait de craie | `#D9D1C1` | Filets, séparateurs (décoratifs) | — |
| `--color-brand` · orange GG | `#F9561A` | Surfaces de marque, bouton principal, FAB | encre dessus : 5,6:1 |
| `--color-brand-ink` · orange brique | `#B23E0B` | Texte et icônes orange sur fond clair, anneau de focus | 5,2:1 sur magnésie |
| `--color-olive` · sommet | `#C3C04C` | Fond du badge de rôle, décor | encre dessus : 9,6:1 |
| `--color-success` · vert relais | `#4A5A0C` | Réussite, présence, « topée » | 6,8:1 sur magnésie |
| `--color-danger` · sang de doigt | `#A3261B` | Erreurs, actions destructives | 6,6:1 sur magnésie |

L'orange GG sur magnésie ne fait que 2,9:1 : il ne porte **jamais** de texte, il sert de surface (avec de l'encre dessus).

### Thème sombre « salle fermée »

Le club utilise beaucoup son logo sur fond noir : le thème sombre est un citoyen de première classe, pas une inversion.

| Token | Hex | Rôle | Contraste |
|---|---|---|---|
| `--color-bg` · nuit de salle | `#12110E` | Fond | — |
| `--color-surface` · tapis | `#1E1C18` | Feuilles, champs | — |
| `--color-surface-sunken` | `#29261F` | Retraits, squelettes | — |
| `--color-ink` · magnésie | `#F3EFE6` | Texte principal | 16,5:1 |
| `--color-ink-muted` | `#ADA594` | Texte secondaire | 7,7:1 sur fond, 7,0:1 sur tapis |
| `--color-rule` | `#3A352D` | Filets | — |
| `--color-brand` · orange GG | `#F9561A` | Surfaces de marque | — |
| `--color-brand-ink` | `#FF7340` | Texte orange | 7,0:1 |
| `--color-olive` | `#C3C04C` | Décor, badge | — |
| `--color-success` · vert relais | `#C9C653` | Réussite (l'olive du logo, lisible sur noir) | 10,5:1 |
| `--color-danger` | `#FF8A7A` | Erreurs | 8,4:1 |

Dans le thème sombre, l'étiquette de voie reste en papier (légèrement tamisé, `#E9E2D0`) : comme une étiquette éclairée dans une salle sombre.
C'est voulu, c'est elle qu'on doit repérer.

### Couleurs de prises

Valeurs par défaut du seed, ajustables par l'admin : jaune `#F5C518`, orange `#F28C28`, rouge `#D7263D`, rose `#F06EAA`, violet `#7B4FB8`,
bleu `#1F5FBF`, bleu clair `#5BC0EB`, vert `#2E9E44`, vert clair `#9BD34A`, noir `#1A1A1A`, blanc `#F7F7F2`, gris `#9A9A9A`,
marron `#7A4B2A`, multicolore (motif à quartiers). La couleur n'est jamais la seule information : le nom de la couleur est toujours
dans le libellé accessible et visible au moins dans la fiche voie.

## 3. Typographie

Deux familles, auto-hébergées via `@fontsource`.

| Famille | Rôle | Pourquoi |
|---|---|---|
| **Archivo** (variable : graisse 100–900, largeur 62–125) | Toute l'interface, les chiffres, les cotations, les numéros de corde | Grotesque robuste, chiffres tabulaires (`tnum`), accents français complets. L'axe de largeur donne une seule famille pour le texte courant (100), les titres un peu élargis (112) et les numéros de corde « peints » très condensés (62). |
| **Kalam** (700) | Le nom des voies, et seulement lui | Écriture au feutre, lisible : c'est le nom écrit à la main sur l'étiquette au pied du mur. |

Kalam n'est jamais utilisée pour un bouton, un titre d'écran ou un message : c'est la signature de l'étiquette, elle perd son sens si elle est partout.

### Échelle

Base 16 px, unités `rem` pour respecter la taille de texte réglée par l'utilisateur.

| Token | Taille / interligne | Usage |
|---|---|---|
| `text-micro` | 12 / 16 | Mentions minuscules (jamais pour une info indispensable) |
| `text-sm` | 14 / 20 | Métadonnées, secteur, dates |
| `text-base` | 16 / 24 | Corps de texte, boutons, champs |
| `text-lg` | 18 / 26 | Noms dans les listes, titres de section |
| `text-xl` | 22 / 28 | Titres d'écran |
| `text-2xl` | 28 / 32 | Gros chiffres (compteurs, podium) |
| `text-3xl` | 36 / 40 | Accueil, moment de validation |

Titres : Archivo 750, largeur 112, casse de phrase. Pas de petites capitales espacées au-dessus des titres.
Chiffres : `font-variant-numeric: tabular-nums` partout où des nombres s'alignent ou changent.

## 4. Mise en page

- **Mobile d'abord, 360 px de référence.** Gouttières de 16 px, grille de 4 px pour tous les espacements.
- **Pas de cartes partout.** Les listes vont d'un bord à l'autre, séparées par des filets fins. Une surface « papier » n'apparaît que pour ce
  qui est posé au-dessus du reste (feuilles, champs, étiquettes). Pas d'ombre grise générique : une seule ombre (`--shadow-sheet`, teintée
  encre) pour les feuilles et menus, et l'ombre « papier » propre à l'étiquette.
- **Rayons.** 4 px pour les champs et boutons, 2 px pour l'étiquette (papier découpé), 20 px pour le haut des feuilles, cercle pour les avatars.
  Pas de gros arrondis génériques.
- **Barre d'onglets à l'encre** en bas, dans les deux thèmes : elle rappelle le noir du logo et ancre l'écran. Onglet actif : icône et libellé
  orange, et un petit trait orange (le « bout de scotch ») au-dessus.
- **Cibles tactiles ≥ 44 px**, 48 px pour les actions de la saisie de séance (mains pleines de magnésie).
- **Bouton « Nouvelle séance »** : bouton flottant orange étendu (icône + texte), en bas à droite, au-dessus de la barre d'onglets, sur Actu et Planning.
- **Gestion (≥ 1024 px)** : navigation latérale fixe, contenu en tableau, largeur de lecture limitée pour les formulaires.

### Wireframes des écrans clés (360 px)

**Actu**

```
┌────────────────────────────────────┐
│ [Gravelines GRIMP]   ⚙  Marie  (MH)│  header magnésie : logo, Gestion (si ouvreur+), pseudo + badge, avatar
│                         Grimpeur GG│
├────────────────────────────────────┤
│ · · · · · · · · · · · · · · · · · ·│  en-tête du club sur grille d'inserts
│  Gravelines Grimp                  │
│  87 grimpeurs accrochés à l'appli ›│  → annuaire
├────────────────────────────────────┤
│ 2 demandes d'amis      Voir        │  (si demandes)
├────────────────────────────────────┤
│ (JD) Julie D. · il y a 2 h         │  carte de séance = bloc pleine largeur, pas une carte flottante
│ 7 voies · 6 topées · 3 en tête     │
│ max 6b · ressenti 8/10             │
│ [12 ●● Bye Bye Morice  6b] tête ✓  │  étiquettes de voie format sm
│ [ 3 ●  Iron Man       6a+] mouli ✓ │
│ « Les avant-bras en béton. »       │
│ 🔥 3  💪 1   ·  2 commentaires     │
├────────────────────────────────────┤
│                 [＋ Nouvelle séance]│  FAB orange étendu
├────────────────────────────────────┤
│ Actu  Topo  À faire Planning Podium│  barre à l'encre
└────────────────────────────────────┘
```

**Nouvelle séance** (feuille plein écran)

```
┌────────────────────────────────────┐
│ ✕  Nouvelle séance     Aujourd'hui▾│
├────────────────────────────────────┤
│ [🔍 corde, nom, couleur, cotation ]│
│ Récemment grimpées                 │
│ [12 ●● Bye Bye Morice  6b]   ＋    │
│ [ 3 ●  Iron Man       6a+]   ＋    │
├────────────────────────────────────┤
│ 1. [12 ●● Bye Bye Morice  6b]  ⋮   │  ⋮ = dupliquer, monter, descendre, supprimer
│ ┌ Mouli ┬ Tête ┐  Topée [oui|non]  │  boutons segmentés 48 px
│ Enchaînée [oui|non]  Chutes [− 0 +]│
│ Premier essai ✓   Ressenti ○○○○○○○ │
├────────────────────────────────────┤
│ Ressenti global, commentaire, photo│
│ [      Enregistrer la séance      ]│  bouton principal pleine largeur, collé en bas
└────────────────────────────────────┘
```

**Topo**

```
┌────────────────────────────────────┐
│ header                             │
├────────────────────────────────────┤
│ Topo           58 voies            │
│ [Cotation ▾] [↑↓] [Filtres (2)]    │  barre de tri et filtres collante
├────────────────────────────────────┤
│ ▢ [12 ●● Bye Bye Morice   6b]      │  vignette photo + étiquette md
│   Négatif · Conti Rési · ♥5 ✓tête  │
├────────────────────────────────────┤
│ ▢ [15 ●  Tonton Talon    6a+] ⚑    │  ⚑ dernière chance
│   Grand dévers · ♥2 · 8 réal.      │
├────────────────────────────────────┤
│ Voies démontées (archives)        ›│
└────────────────────────────────────┘
```

**Fiche voie** (feuille qui glisse du bas)

```
┌────────────────────────────────────┐
│ ───                              ✕ │
│ [ photo de la voie, 4:3          ] │
│  ┌───────────────────────────────┐ │
│  │12 ●● Bye Bye Morice        6b │ │  étiquette lg, légèrement inclinée, scotch en haut
│  └───────────────────────────────┘ │
│ Négatif et ses 2 dièdres · corde 12│
│ Ouverte le 16 mai par Sam          │
│ Conti · Rési · Physique · Complexe │
│ ┃ Le conseil de l'ouvreur          │  bloc encadré d'un trait orange
│ ┃ « Garde du jus pour la sortie. » │
│ 24 ascensions · 9 grimpeurs · 40 % │
│ en tête                            │
│ [♥ Coup de cœur]                   │
│ [✓ Je sais la faire en mouli]      │
│ [  Je sais la faire en tête ]      │
│ Commentaires (3)                   │
└────────────────────────────────────┘
```

## 5. L'étiquette de voie (composant signature)

`RouteLabel.vue`, utilisée partout où une voie apparaît : topo, saisie de séance, fil, À faire, gestion.

```
   ▓▓ (scotch, tailles md/lg)
 ┌─────┬──────┬────────────────────────┬──────┐
 │ 12  │ ●●   │ Bye Bye Morice         │  6b  │
 └─────┴──────┴────────────────────────┴──────┘
  corde  prises  nom (Kalam, au feutre)  cotation
```

- **Corde** : bloc encre, numéro en Archivo 850 largeur 62, comme les numéros peints au mur.
- **Prises** : une ou deux petites prises dessinées (forme organique, trou de boulon, reflet), dans la couleur de prise. Le blanc a un contour,
  le multicolore est un motif à quartiers.
- **Nom** : Kalam 700, tronqué avec ellipse si besoin (nom complet dans le libellé accessible).
- **Cotation** : Archivo 800 largeur 75, minuscules (« 6a+ »), chiffres tabulaires.
- **Support** : papier (`--label-paper`), rayon 2 px, ombre papier fine. En taille `lg` (fiche voie) : inclinaison de −0,8° et un bout de scotch.
- **Statuts** : dernière chance = ruban orange « Dernière chance » sur le bord droit ; démontée = étiquette délavée et tampon « Démontée ».
- **Tailles** : `sm` (32 px de haut, fil et listes compactes), `md` (44 px, topo, À faire, saisie), `lg` (fiche voie).
- **Accessibilité** : un seul libellé lisible, par exemple « Corde 12, Bye Bye Morice, 6b, prises jaunes et bleues, dernière chance ».

## 6. Composants de base

Tous dans `frontend/src/components/ui/`, construits sur Reka UI quand un comportement accessible est en jeu (dialogue, feuille, onglets,
groupe de boutons), habillés par nos tokens.

| Composant | Rôle |
|---|---|
| `GgButton` | Variantes `primary` (orange, encre dessus), `secondary` (contour encre), `ghost`, `danger` ; tailles 44 / 48 px. |
| `GgIconButton` | Bouton icône 44 px, libellé accessible obligatoire. |
| `HoldSwatch` | Une prise dessinée dans une couleur. |
| `RouteLabel` | L'étiquette de voie. |
| `RoleBadge` | Badge de rôle sous le pseudo (« Grimpeur GG », « Ouvreur·se »…), fond olive, encre dessus. |
| `UserAvatar` | Photo ou initiales sur une couleur dérivée du pseudo. |
| `EmptyState` | Illustration, titre drôle, explication claire, action. |
| `SegmentedControl` | Mouli / Tête, Oui / Non : gros boutons groupés. |
| `InsertGrid` | Le motif de fond « grille d'inserts ». |
| `SkeletonBlock` | Squelette de chargement (pas de spinner plein écran). |

## 7. Icônes

- Icônes génériques d'interface : [Lucide](https://lucide.dev) (trait 2 px, grille 24 px), importées une par une.
- Icônes métier (onglets Topo, À faire, Podium ; prise ; badges en phase 7) : dessinées pour l'appli dans la même grille et le même trait,
  dans `frontend/src/components/icons/`.
- Pas d'emoji comme icône d'interface. Les seuls emoji sont les quatre réactions du fil (🔥 💪 👏 🦍), parce que c'est leur nature.

## 8. Mouvement

- **Un seul moment orchestré** : l'enregistrement d'une séance (validation, puis badges débloqués un par un). Conçu en phase 4 et 7.
- Partout ailleurs : transitions courtes (150–200 ms, `ease-out`) qui expliquent un changement d'état (feuille qui monte, ligne qui disparaît).
- Pas d'animation d'entrée sur chaque bloc.
- `prefers-reduced-motion: reduce` : toutes les transitions passent en fondu instantané, le moment de validation devient statique.

## 9. Écriture

Voir le §11 de la spec : tutoiement, casse de phrase, humour de vestiaire, jamais au détriment de la clarté. Un bouton dit ce qu'il fait.
Tous les textes sont dans `frontend/src/i18n/fr.json`.

## 10. Relecture « est-ce que ça ressemble à n'importe quelle appli sportive ? »

Première version du plan relue avec cette question :

- *Version 1* : fond blanc, cartes blanches arrondies, orange en accent, Inter. → **Générique**, c'est exactement la maquette. Révisé :
  fond magnésie, listes pleine largeur sans cartes, barre d'onglets à l'encre.
- *Version 1* : pastilles de couleur rondes pour les prises (comme Oblyk). → Révisé : petites prises dessinées avec leur boulon.
- *Version 1* : une police manuscrite pour les titres aussi. → Rejeté : ça devient un thème « fun » de plus. Kalam est réservée au nom de voie,
  là où l'écriture à la main existe vraiment dans la salle.
- *Version 1* : contour encre épais sur tous les boutons. → Rejeté : style « néo-brutaliste » à la mode, sans lien avec la salle.
- Ce qui reste propre à GG Pocket : la magnésie, l'étiquette en papier au feutre, les numéros de corde peints, les prises avec boulon,
  la grille d'inserts, les deux « G » olive et orange du logo.
