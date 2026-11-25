# Sabay Tayo?

**Sabay Tayo?** is a JavaFX 21 branching visual novel set on a rainy evening bus commute in the Philippines. It tracks how your dialogue choices shape **Trust**, **Attraction**, **Respect**, **Annoyance**, and **Momentum**, branching across a 12-scene Two-Lane dialogue graph into five possible endings.

## Features
- **Character & Pronoun Customization (`StartView`):** Set your name and choose pronouns (`He/Him`, `She/Her`, `They/Them`) for both the player and your seatmate.
- **12-Scene Two-Lane Dialogue Graph (`Encounter`, `Scene`):** Branches at the sudden brake incident (`S03` -> `S04A`/`S04B`/`S04C`), splits into a Warm Lane (`S05A` -> `S06A`) or Cold Lane (`S05B` -> `S06B`) based on Momentum, merges at `S07`, and resolves into `S08` (`LOVERS`, `BLOCKED`, or `STANDARD`).
- **5 Relationship Outcomes (`OutcomeClassifier`):** `BlockedOutcome`, `LoverOutcome`, `BestFriendOutcome`, `RivalOutcome`, and `AcquaintanceOutcome`.
- **Visual Novel Stage & Audio (`EncounterView`, `EndingView`, `StageCue`, `GameApplication`):** Per-beat expressions, Event CG angle cuts, conductor aisle cameo, animated rain/streetlight atmosphere, warm/cold adaptive BGM, rain-bus ambient loop, Foley/UI SFX, and outcome-specific Ending CGs revealed before the final results & badge card.

## Project Structure
- `src/main/java/com/sabaytayo/` — Core OOP models, scene graph, JavaFX views, and custom exceptions
- `src/main/resources/assets/` — Character sprites (`MC/`, `FEM-C/`, `CONDUCTOR/`), backgrounds (`BACKGROUNDS/`), Event CGs (`CG/`), and audio (`AUDIO/`)
- `src/test/java/com/sabaytayo/` — Unit tests for dialogue beats, scene graph reachability, stats, pronouns, outcome classification, and stage cues
