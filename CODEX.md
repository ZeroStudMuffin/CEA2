### 🔄 Project Awareness & Context
- **You have full internet access. It is enabled in the codex environment**
- **Always read `PLANNING.md`** at the start of a new conversation to understand the project's architecture, goals, style, and constraints.
- **Check `TASK.md`** before starting a new task. If the task isn’t listed, add it with a brief description and today's date.
- **Use consistent naming conventions, file structure, and architecture patterns** as described in `PLANNING.md`.
- **Use venv_linux** (the virtual environment) whenever executing Python commands, including for unit tests.

### 🧱 Code Structure & Modularity
- **Never create a file longer than 500 lines of code.** If a file approaches this limit, refactor by splitting it into modules or helper files.
- **Organize code into clearly separated modules**, grouped by feature or responsibility.
  For agents this looks like:
    - `agent.py` - Main agent definition and execution logic 
    - `tools.py` - Tool functions used by the agent 
    - `prompts.py` - System prompts
- **Use clear, consistent imports** (prefer relative imports within packages).
- **Use clear, consistent imports** (prefer relative imports within packages).
- **Use python_dotenv and load_env()** for environment variables.

### 🧪 Testing & Reliability
- **Always create Pytest unit tests for new features** (functions, classes, routes, etc).
- **After updating any logic**, check whether existing unit tests need to be updated. If so, do it.
- **Tests should live in a `/tests` folder** mirroring the main app structure.
  - Include at least:
    - 1 test for expected use
    - 1 edge case
    - 1 failure case

### 📱 Android SDK Setup
To run Gradle tasks or tests, install the Android SDK using the bundled
command-line tools archive stored in Git LFS:

1. Decode the Gradle wrapper:
   ```bash
   python scripts/decode_gradle_wrapper.py
   ```
2. Pull the LFS files to retrieve `commandlinetools-linux-13114758_latest.zip`.
   If no Git remote is set (some environments clone without one), add it before pulling:
   ```bash
git remote add origin https://github.com/ZeroStudMuffin/CEA2.git
git lfs pull
```
3. Extract the command line tools and add them to `PATH` so the installer can
   use `sdkmanager`:
   ```bash
   unzip commandlinetools-linux-13114758_latest.zip -d android-tools
   export PATH="$PWD/android-tools/cmdline-tools/bin:$PATH"
   ```
4. Run the SDK installer script:
   ```bash
   chmod +x scripts/install_android_sdk.sh
   ./scripts/install_android_sdk.sh
   ```
5. Create `local.properties` in the project root with the path to the installed
   SDK, for example:
   ```
   sdk.dir=/opt/android-sdk
   ```
6. Set `ANDROID_HOME` and ensure `$ANDROID_HOME/platform-tools` is on your
   `PATH` if not added automatically.

### ✅ Task Completion
- **Mark completed tasks in `TASK.md`** immediately after finishing them.
- Add new sub-tasks or TODOs discovered during development to `TASK.md` under a “Discovered During Work” section.

### 📎 Style & Conventions
- **Use Python** as the primary language.
- **Follow PEP8**, use type hints, and format with `black`.
- **Use `pydantic` for data validation**.
- Use `FastAPI` for APIs and `SQLAlchemy` or `SQLModel` for ORM if applicable.
- Write **docstrings for every function** using the Google style:
  ```python
  def example():
      """
      Brief summary.

      Args:
          param1 (type): Description.

      Returns:
          type: Description.
      """
  ```

### 📚 Documentation & Explainability
- **Update `README.md`** when new features are added, dependencies change, or setup steps are modified.
- **Record each feature in `AppFeatures.txt`** with a short explanation of how it works and any limitations whenever new functionality is introduced.
- **Comment non-obvious code** and ensure everything is understandable to a mid-level developer.
- When writing complex logic, **add an inline `# Reason:` comment** explaining the why, not just the what.

### 🧠 AI Behavior Rules
- **Never assume missing context. Ask questions if uncertain.**
- **Never hallucinate libraries or functions** – only use known, verified Python packages.
- **Always confirm file paths and module names** exist before referencing them in code or tests.
- **Never delete or overwrite existing code** unless explicitly instructed to or if part of a task from `TASK.md`.
- **Search https://developer.android.com/develop subdomains for relevant information before executing any task** this is a main resource.
