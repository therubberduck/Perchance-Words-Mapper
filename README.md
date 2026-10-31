# Perchance Sci-Fi Entry App

An internal desktop editor for curating the data behind the
[**scifi-planet-name-2**](https://perchance.org/scifi-planet-name-2) Perchance generator.

Perchance generators store their data as plain text, which is tedious to hand-edit directly in the
browser. This tool provides a dedicated GUI for creating, tagging, searching and maintaining the
generator's **entries** (each a component of a planet/place name with a description and a set of tags), then writes
them back out in the exact text format the generator expects. The resulting `entries.txt` is pasted
into the generator's data at <https://perchance.org/ewy8s8fvvm#edit>.

It is a personal, single-user utility — not a general-purpose tool — so file paths and the tag
taxonomy are hard-coded to this one generator.

## Tech stack

- **Kotlin Multiplatform** targeting Desktop (JVM) only
- **Compose Multiplatform** for the UI (Material 3)
- **Koin** for dependency injection (`MainViewModel`)
- **Okio** for file I/O

## Data & file locations

All data lives in a fixed working directory on disk (`baseDir` in `MainViewModel.kt`):

```
E:/ProjectBuilds/PerchanceSciFiEntryApp/
├── entries.txt      # the generator entries (required — app refuses to start without it)
└── templates.txt    # reusable entry templates (created/updated by the app)
```

On startup the app:

1. Builds the full **tag taxonomy** in code (`TagsCreator`) — this is the master list of all valid tags.
2. Requires `entries.txt` to already exist (it fails fast with an error if missing).
3. Loads all entries and all templates, selecting the first of each.

### `entries.txt` / `templates.txt` format

Each record is three lines, matching the Perchance generator's own indentation-based syntax:

```
  Entry Title
    orig=The description text.
    tags=deathw,desert,viking
```

- Title line is indented **exactly two spaces**.
- `orig=` holds the description. Newlines in the description are stored as `<br>` on disk and
  converted back when loaded.
- `tags=` is a comma-separated list of **tag ids** (the short internal ids, not the display titles).

When reading, any tag id not found in the master taxonomy is kept as an "unknown" tag so nothing is
silently lost. A few legacy ids are remapped on load (e.g. `religion`→`mythic`, `rperson`→`mperson`,
`rplace`→`mplace`).

## The tag taxonomy

Tags are defined entirely in `model/TagsCreator.kt` and organized into top-level **categories**, each
with a `parentId`:

| Category | parentId | Contents |
|----------|----------|----------|
| World Type | `world` | Biome/function world types (Desert, Ice, Mining, Prison, War, …) |
| Region | `region` | Broad geographic regions (Africa, Europe, Scandinavia, Caribbean, …) |
| Country | `place` | Individual countries/territories |
| Culture | `culture` / `amindian` | Historical & living cultures and languages (incl. a dedicated American Indian group) |
| Religion | `religion` | World and historical religions |
| Word | `word` | Thematic word pools (Astronomy, Botanical, Nautical, Space-Flight, …) |
| Fiction | `fictional` | Fictional universes (Arthurian, Dune) |
| Misc | `misc` | Structural/grammatical tags (First Name, Surname, Adjective, affix patterns, …) |

Tags also carry a **priority** (an integer), used only for visual grouping within a category — lower
priorities are shown first, separated by thin divider lines, so the most common tags surface at the top.

## The interface

The main screen is split into two halves, plus two full-screen overlays (the Entries picker and the
Templates picker).

### Left half — Edit view

Edits the currently-selected entry.

- **Title field** — single-line; the entry's title.
- **Description field** — multi-line (`orig=`); the entry's description.
- **Tag summary** — a read-only line listing the entry's current tags (or a gray *"No tags"* when empty).
- **Create New** — adds a blank new entry and selects it.
- **Delete** — deletes the current entry, after an *"Are you sure?"* confirmation dialog.
- **All Entries** — opens the full-screen [Entries picker](#entries-picker-overlay).

#### Template section

Templates are saved tag+description presets, so a batch of similar entries can be stamped out quickly.

- **[Template name] button** (white with a black border) — shows the active template; click to open the
  [Templates picker](#templates-picker-overlay) and switch templates.
- **Entry from Template** — creates a new entry pre-filled from the active template's description and
  tags, then selects it.
- **Create Template** — opens a dialog to name and save the *current entry* as a new template.
- **Delete Template** — deletes the active template (with confirmation). Refuses to delete the last
  remaining template.

#### Save section

- **Auto-Save** (checkbox) — when on (the default), entries are saved to `entries.txt` automatically
  whenever you switch entries, create one, or stamp one from a template. A toast shows which entry was saved.
- **Save All** — manually writes every entry to `entries.txt` now.

> **Save filtering:** on every save, entries with a blank title, blank description, or no tags are
> **excluded** from the written file — this keeps half-finished drafts out of the generator data.

### Right half — Tags picker

Toggles tags on the current entry. Clicking any tag adds it (turns **red/bold**) or removes it.

- **Top-level category row** — the seven master categories (World Type, Mythology, Places, Names,
  People, Words, Fiction). Clicking one toggles that category's umbrella tag directly on the entry.
- **Category tabs** — World · Region · Country · Culture · Religion · Word · Fiction · Misc. Selecting a
  tab shows that category's tags below, grouped by priority with divider lines between groups.
- **Misc tab** — shows all misc tags. If the entry carries misc/unknown tags that aren't part of the
  standard misc set (e.g. legacy or hand-added ids), they are shown in a separate section above the
  divider, and the tab label gets a **"(!)"** marker to flag them for cleanup.

Selected tags are kept sorted (alphabetically, with parent/child tags ordered consistently) whenever
the set changes.

### Entries picker (overlay)

A searchable, scrollable list of every loaded entry. Each row shows the title, description and tags;
clicking a row selects that entry and closes the overlay. The list's scroll position and search state
are preserved between openings.

- **Search field** — free-text query (disabled in *Broken Tags* mode).
- **Search mode** radio buttons:
  - **by title** — matches the query against entry titles.
  - **by title or desc** — matches against titles *or* descriptions.
  - **by tags** — matches against the entry's tag titles *or* tag ids.
  - **Broken Tags** — ignores the search text and instead lists only entries containing at least one
    tag that is **not** in the master taxonomy (`TagsCreator`). This surfaces entries with outdated,
    misspelled, or unknown tag ids so they can be fixed — the main data-hygiene tool.
- **X** — closes the overlay.

### Templates picker (overlay)

A scrollable list of saved templates showing each template's name and its tags. Clicking a template
makes it the active template and closes the overlay. **X** closes without changing the selection.

## Build and run

Desktop (JVM) application:

- Windows:
  ```shell
  .\gradlew.bat :composeApp:run
  ```
- macOS/Linux:
  ```shell
  ./gradlew :composeApp:run
  ```

> Note: the app expects `E:/ProjectBuilds/PerchanceSciFiEntryApp/entries.txt` to exist before it will
> start. Adjust `baseDir` in `MainViewModel.kt` if running elsewhere.

## Typical workflow

1. Run the app; it loads the current `entries.txt`.
2. Use **All Entries → Broken Tags** to find and repair entries with invalid tags.
3. Create or edit entries, assigning tags from the right-hand picker (optionally starting from a template).
4. With Auto-Save on, changes are written continuously; otherwise hit **Save All**.
5. Copy the resulting `entries.txt` into the generator data at <https://perchance.org/ewy8s8fvvm#edit>.
