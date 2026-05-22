# Tag Mod

A fast-paced multiplayer Minecraft minigame for Fabric inspired by classic Tag — now featuring multiple gamemodes, freeze mechanics, sounds, glowing role effects, survival tracking, and customizable gameplay settings.

Play with friends in LAN or multiplayer servers and survive as long as possible before getting caught.

---

# Gamemodes

## Classic Tag Mode

One random player becomes the **Chaser** while everyone else becomes **Runners**.

* If the Chaser hits a Runner:

  * the Runner becomes the new Chaser
  * the old Chaser becomes a Runner

The goal is simple:
**don't get tagged.**

---

## Freeze Mode

A new survival-style Tag gamemode.

### How it works

* One player becomes the **Freezer**
* Freezers can freeze Runners on hit
* Frozen players become trapped inside ice cages
* Runners can rescue teammates by breaking the ice
* If a frozen player is not rescued within 60 seconds:

  * they become a new Freezer

When every Runner has been frozen, the round enters an ending countdown and displays the longest surviving player.

---

# Features

* Classic Tag gameplay
* Freeze Mode gamemode
* Multiplayer & LAN support
* HUD/actionbar role messages
* Red glowing outline for Chasers
* Aqua glowing outline for Freezers
* Ice cage freezing system
* Survival timer tracking
* Round-end statistics
* Sound effects & countdown audio
* Configurable world border support
* Automatic role syncing
* Command-based controls

---

# Commands

## Main Commands

* `/tagstart`
* `/tagstop`

## Freeze Mode

* `/tag config freezeMode enable`
* `/tag config freezeMode disable`

## World Border

* `/tag config worldBorder <size>`
* `/tag config worldBorder normal`

---

# Requirements

* Minecraft Java Edition `1.21.1`
* Fabric Loader
* Fabric API

---

# Important

* All players must have the mod installed
* Requires:

  * 2+ players for Classic Mode
  * 3+ players for Freeze Mode

---

# License

Licensed under the MIT License.
