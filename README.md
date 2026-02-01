# Joise-Shell

Joise-Shell is my minimal Unix-like command shell implemented in Java. It provides a small, focused set of features for running programs, a few builtins, basic quoting rules, and simple I/O redirection.

---

## Status — implemented features

Base
- Print a prompt
- REPL (read–eval–print loop)
- Handle invalid commands
- exit builtin
- echo builtin
- type builtin
- Locate executables on PATH
- Run external programs

Navigation
- pwd builtin
- cd: absolute paths
- cd: relative paths
- cd: home directory (`cd` with no args)

Quoting
- Single quotes
- Double quotes
- Backslash outside quotes
- Backslash within single quotes
- Backslash within double quotes
- Executable names/paths with spaces when quoted

Redirection
- Redirect stdout (`>`)
- Redirect stderr (`2>`)
- Append stdout (`>>`)
- Append stderr (`2>>`)

---

## Quick overview

Start Joise-Shell and you get a prompt. The shell parses simple commands, handles the builtins above, searches the PATH for external programs, and supports the listed quoting and redirection behaviors.

This implementation focuses on being small, predictable, and easy to inspect — not a full POSIX-compliant shell.

---

## Requirements

- JDK 17+ 
- Maven

---

## Build & run

1. Build:
    - mvn clean package
2. Run:
    - java -jar target/joise-shell.jar
---

---

## Contact

Repository owner: [SalmaneKhalili](https://github.com/SalmaneKhalili)
