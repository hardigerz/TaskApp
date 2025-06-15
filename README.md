# TaskApp

A simple Task Management Android app built with Kotlin, MVVM, Hilt, Room and Material 3.

---

## 📖 Overview

TaskApp lets users:

- Create, view, edit, and delete tasks  
- Mark tasks as **Pending** or **Completed**  
- Filter the list by **All**, **Pending**, or **Completed**  
- Persist tasks locally using Room  
- Observe data changes with Kotlin Flow / StateFlow  
- Enjoy a Material Design 3 UI (ConstraintLayout, ViewBinding, Material Components)

---

## 🚀 Build & Run

1. **Clone the repository**  
   ```bash
   git clone https://github.com/your-org/TaskApp.git
   cd TaskApp

2. Open in Android Studio

    - File → Open → select the project’s root directory.

3. Let Gradle sync & download dependencies

    - Ensure you have an Android SDK matching compileSdk = 35.

4. Run on device or emulator

    - Select your target and click Run ▶.


🧪 Unit Tests
**From the project root, run:**
```bash
# Run all local unit tests (ViewModel & repository)
./gradlew test
# (Optional) Run instrumentation tests on a connected device/emulator
./gradlew connectedAndroidTest

```
## 🧪 Test Highlights

- **`MainDispatcherRule`**  
  Swaps out `Dispatchers.Main` for a `TestDispatcher` so all `viewModelScope.launch {…}` blocks run under your test scheduler.
- **kotlinx-coroutines-test**  
  Controls coroutine timing with `runTest {…}` and `advanceUntilIdle()`.
- **Turbine**  
  (If you use it) makes it easy to assert on `Flow`/`StateFlow` emissions in your ViewModel tests.
- **FakeTaskRepository**  
  An in-memory, `MutableStateFlow`-backed repo that always emits a **new** `List` on insert/update/delete, so your UI/ViewModel sees every change.
- **Separation of concerns**  
  ViewModels contain only business logic; no Android SDK calls, making them fully unit-testable.

---

---

## 💡 Trade-Offs & Assumptions

- **Simple boolean status**  
  We kept `Task.isCompleted: Boolean` rather than a full sealed‐class or enum to reduce boilerplate.
- **Local-only storage**  
  No remote sync or user accounts—purely local CRUD with Room.
- **Minimal error handling**  
  We show simple validation errors (e.g. “Title required”) but no network retry logic.
- **Material DayNight theme**  
  We rely on the platform’s DayNight switch; there’s no in-app toggle.
- **Basic animations**  
  We use simple fades and disable RecyclerView item animations to avoid flicker. No shared-element transitions.

---

## 🚀 Running the Workflow Manually
You can trigger the workflow manually from the GitHub Actions tab:

- **Go to your repository.**
- **Click on the Actions tab.**
- **Select the latest workflow run.**
- **Download the generated APK & AAB files from the artifacts section.**

## 📂 Output Files

After a successful build, you'll find the APK & AAB files in:
```bash
app/build/outputs/apk/debug/  (Debug APK)
app/build/outputs/apk/release/ (Release APK)
app/build/outputs/bundle/release/ (AAB File)
```

📂 Project Structure
```bash
app/
 ├─ src/main/java/com/testhar/taskapp
 │    ├─ data/            # Room entities & DAO
 │    ├─ domain/          # Model & Repository interface
 │    ├─ ui/
 │    │   ├─ activity/    # MainActivity, AddEditTaskActivity
 │    │   ├─ adapter/     # RecyclerView adapters
 │    │   └─ viewmodel/   # TaskViewModel
 │    └─ common/          # Extensions, dialogs/snackbar helpers
 ├─ src/test/             # Unit tests (ViewModel, fake repo)
 └─ src/androidTest/      # Instrumentation tests (Room, UI)
```



