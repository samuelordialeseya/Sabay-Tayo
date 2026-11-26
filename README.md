# Sabay Tayo?

**Sabay Tayo?** is a JavaFX 21 branching visual novel set on a rainy evening bus commute in the Philippines. It tracks how your dialogue choices shape **Trust**, **Attraction**, **Respect**, **Annoyance**, and **Momentum**, branching across a 12-scene Two-Lane dialogue graph into five possible endings.

## Features
- **Character & Pronoun Customization (`StartView`):** Set your name and choose pronouns (`He/Him`, `She/Her`, `They/Them`) for both the player and your seatmate.
- **12-Scene Two-Lane Dialogue Graph (`Encounter`, `Scene`):** Branches at the sudden brake incident (`S03` -> `S04A`/`S04B`/`S04C`), splits into a Warm Lane (`S05A` -> `S06A`) or Cold Lane (`S05B` -> `S06B`) based on Momentum, merges at `S07`, and resolves into `S08` (`LOVERS`, `BLOCKED`, or `STANDARD`).
- **5 Relationship Outcomes (`OutcomeClassifier`):** `BlockedOutcome`, `LoverOutcome`, `BestFriendOutcome`, `RivalOutcome`, and `AcquaintanceOutcome`.
- **Visual Novel Stage & Audio (`EncounterView`, `EndingView`, `StageCue`, `GameApplication`):** Per-beat expressions, Event CG angle cuts, conductor aisle cameo, animated rain/streetlight atmosphere, dedicated Main Menu, warm/cold dialogue, Sudden Brake, and per-ending BGM tracks, rain-bus ambient loop, Foley/UI SFX, and outcome-specific Ending CGs revealed before the final results & badge card.

## Project Structure
- `src/main/java/com/sabaytayo/` — Core OOP models, scene graph, JavaFX views, and custom exceptions
- `src/main/resources/assets/` — Character sprites (`MC/`, `FEM-C/`, `CONDUCTOR/`), backgrounds (`BACKGROUNDS/`), Event CGs (`CG/`), and audio (`AUDIO/`)
- `src/test/java/com/sabaytayo/` — Unit tests for dialogue beats, scene graph reachability, stats, pronouns, outcome classification, and stage cues

## Audio Credits
Menu, sudden brake, and ending BGM tracks (`bgm_main_menu.mp3`, `bgm_sudden_brake.mp3`, `bgm_ending_*.mp3`) include music by Kevin MacLeod ([incompetech.com](https://incompetech.com)), licensed under Creative Commons: By Attribution 4.0 License (`Late Night Radio`, `I Can Feel it Coming`, `Clear Air`, `Montauk Point`, `Past Sadness`, `Loopster`, `Immersed`).

## How to Run (Temporary Guide)

### Requirements
- **Java 21 (JDK 21)**
- **Maven** (or **VS Code** / **IntelliJ IDEA** with Java & Maven extensions)

### Windows (PowerShell / CMD)
1. Clone and open the project folder:
   ```powershell
   git clone https://github.com/samuelordialeseya/Sabay-Tayo.git
   cd Sabay-Tayo
   ```
2. Run with Maven:
   ```powershell
   mvn javafx:run
   ```
   *(If you don't have `mvn` in PATH, open the folder in VS Code or IntelliJ, go to the **Maven** panel -> `SabayTayoFX` -> `Plugins` -> `javafx` -> `javafx:run`, or run `src/main/java/com/sabaytayo/Main.java`.)*

### macOS (Terminal)
1. Make sure Java 21 and Maven are installed (e.g. via Homebrew):
   ```bash
   brew install openjdk@21 maven
   ```
2. Clone and run the project:
   ```bash
   git clone https://github.com/samuelordialeseya/Sabay-Tayo.git
   cd Sabay-Tayo
   mvn javafx:run
   ```
   *(Or open the project in VS Code / IntelliJ and run `javafx:run` from the Maven panel or `Main.java`.)*
