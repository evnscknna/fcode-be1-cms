# Course Management System

A console Java application for administrators to manage courses, instructors,
students and enrollments. **All data is stored in CSV files** under `data/` -
there is no database and no third-party framework.

## Running

### NetBeans
Open the folder as a project (it is a *Java Application* / Ant project) and press
**Run**. The main class is `cms.Main`.

### Command line (Ant)
```
ant clean run     # compile and start the app
ant jar           # build dist/CourseManagementSystem.jar
```

### Command line (plain JDK)
```
javac -encoding UTF-8 -d build/classes $(find src -name "*.java")
java -cp build/classes cms.Main
```

Requires JDK 8+. Data files are read/written relative to the working directory,
so run from the project root.

## Data

An **example dataset ships in `data/`** so every feature has something to show
from the first run:

| | |
|---|---|
| Login | `admin` / `admin123` |
| Instructors | 3 |
| Courses | 6 across `2025-FALL`, `2026-SPRING`, `2026-FALL`; two with prerequisite chains (`CS101 -> CS102 -> CS201`) |
| Students | 6 |
| Enrollments | 21 rows - completed (with grades), currently enrolled, and one withdrawn-then-retaken |
| Full courses | `CS102`, `CS201`, `MATH101` at 100% capacity; `MATH101` also has 2 students waiting |

Delete `data/` to start clean - on the next run a small starter set (one admin,
a few instructors/courses/students) is created automatically.

## Architecture (MVC)

```
cms.view         console I/O only - menus, prompts, tables (no logic, no files)
cms.controller   menu flow - reads via the view, calls services
cms.model.dto    plain data holders (Course, Student, Instructor, Enrollment, ...)
cms.model.dao    CSV persistence, one DAO per entity (findAll/getById/insert/update/delete)
cms.model.service  business rules - enrollment rules, waiting list, schedules,
                   statistics, progress reports, undo/redo history
cms.util         CSV parsing, validation, hand-written merge sort & searches
```

Flow: `view -> controller -> service -> dao -> CSV`, returning DTOs back up.
Controllers never touch DAOs or files; the view never touches services.

Errors use plain JDK exceptions, thrown where the problem is found:
`IllegalArgumentException` for bad input / broken rule / not found,
`IllegalStateException` for "not allowed right now" (nothing to undo, not
enrolled), `java.io.UncheckedIOException` for a CSV read/write failure, and
`NoSuchElementException` for end of input. Each menu action catches the first
three, prints the message, and carries on; end of input saves and quits.
A course being full is not an error - `enroll` just returns `false`.

## Feature map

**Basic** - CRUD for courses/students/instructors; search (case-insensitive
substring, linear); sort (merge sort by several keys); enroll / withdraw; course
details with the enrolled-student roster.

**Medium** - FIFO waiting list for full courses; student weekly schedule;
duplicate-enrollment prevention; schedule-conflict detection (interval overlap);
per-course and aggregate enrollment statistics; instructor teaching schedule.

**Hard** - undo / redo of enrollment operations via a Command-pattern history;
automatic waiting-list processing when capacity increases; registration-rule
validation (prerequisites, per-semester credit ceiling, duplicate courses,
prerequisite-cycle detection with DFS); student progress reports (completed /
in-progress / remaining credits, GPA, per-semester breakdown) from enrollment
history.

## Notes

- Every mutation is written straight back to its CSV file (write to a temp file,
  then rename). "Save & exit" re-persists everything as a safety net.
- Automatic promotions triggered by a capacity increase or a completion are not
  undoable enrollment actions, so they clear the undo/redo history.
- Passwords are stored and compared as plain text, by design for this project.
- Almost no comments: the code is meant to read on its own. A one-liner appears
  only where a reader would otherwise stumble (merge-sort stability, the DFS
  colour scheme, why a DAO write goes temp-file-then-rename, a few return-value
  contracts, EOF handling).
