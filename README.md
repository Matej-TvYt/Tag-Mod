# Tag Mod

A multiplayer Minecraft tag minigame built for Fabric where players compete in fast-paced rounds using the roles: **Chaser**, **Runner**, and the new **Freezer** role in Freeze Mode.

---

## How to begin

* Requires **2+ players**
* Freeze Mode requires **3+ players**
* All players must have the mod installed

### Start the game

```text
/tagstart
```

### Stop the game

```text
/tagstop
```

---

# Gameplay

## Classic Tag Mode

* One random player becomes the **Chaser**
* All other players become **Runners**
* If the Chaser hits a Runner:

  * the Runner becomes the new Chaser
  * the old Chaser becomes a Runner

The goal is to avoid becoming the Chaser.

---

# Freeze Mode

Enable Freeze Mode using:

```text
/tag config freezeMode enable
```

Disable using:

```text
/tag config freezeMode disable
```

### Freeze Mechanics

* One player is randomly selected as the **Freezer**
* Freezers glow with an aqua outline
* Runners glow normally
* When a Freezer hits a Runner:

  * the Runner becomes frozen
  * an ice cage appears around the player
  * the frozen player cannot move

### Rescue System

* Runners can rescue frozen teammates
* Breaking the ice cage unfreezes the player
* Unfreezed players return to the Runner role

### Conversion System

* If a frozen player is not rescued within 60 seconds:

  * they automatically become a new Freezer

### Round Ending

* When all Runners are frozen:

  * a 10-second countdown appears on everyone's HUD
  * all ice cages break automatically
  * the round ends
  * the game displays the longest surviving player

Example:

```text
ProGamer68 survived the longest: 4m 12s
```

---

# Features

* Classic Tag gameplay
* Freeze Mode gamemode
* Ice cage freezing system
* Runner rescue mechanics
* Automatic Freezer conversion
* HUD/actionbar role messages
* Red glowing outline for Chasers
* Aqua glowing outline for Freezers
* Survival time tracking
* Round-end statistics
* Countdown system
* Sound effects
* Multiplayer & LAN support
* World border configuration
* Command-based controls

---

# Commands

## Main Commands

```text
/tagstart
/tagstop
```

## Freeze Mode

```text
/tag config freezeMode enable
/tag config freezeMode disable
```

## World Border

```text
/tag config worldBorder <size>
/tag config worldBorder normal
```

---

# Requirements

* Minecraft **1.21.1**
* Fabric Loader
* Fabric API

---

# Important

* All players must install the mod
* Works only in multiplayer/LAN
* Minimum:

  * 2 players for Classic Mode
  * 3 players for Freeze Mode

---

# License

This project is licensed under the MIT License.
