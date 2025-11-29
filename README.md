# ShellUsingJavaOptimized  
A clean, modular, and scalable rewrite of the original **BuildShellUsingJava** project.

The initial version of this shell was implemented in a **single 700+ line `Main.java` file**, making it difficult to maintain, scale, and extend.  
This optimized version refactors the entire architecture into **well-organized modules**, improving readability, maintainability, and future extensibility.

---

## 🚀 Features

- Fully functional **custom shell** written in Java  
- Modular architecture with separate packages for:
  - Commands
  - History management
  - Pipeline execution
  - Enums / core utilities
- Support for **Windows system commands**, **external processes**, and **built-in shell commands**
- Easy to compile and run on any system with Java installed
- Cleaner code structure for contributors and learners

---

## 🧩 Architecture Overview

### **Core Components**
| Module | Responsibility |
|--------|----------------|
| **core/** | Entry point logic, shell environment, handlers |
| **commands/** | Built-in commands implementation (`echo`, `type`, `pwd`, `cd`, etc.) |
| **pipeline/** | Pipeline executor for commands connected via `|` |
| **history/** | History file management (read/write/append) |
| **enums/** | Shell enums such as command types |
| **bin/** | Compiled `.class` output goes here |

---

## 🛠️ Build & Run Instructions

These commands are also included in `CommandList.txt`.

### **1️⃣ Compile all Java files**
```powershell
javac -d bin (Get-ChildItem -Recurse -Filter *.java).FullName
```
### **2️⃣ Run the custom shell **
```powershell
java -cp bin shell.Main
```
You will now enter an interactive shell where you can run Windows commands, shell commands, and custom built-ins.

🧨 Supported Commands

The categorized list of commands tested and supported by this shell are included inside the repository as CommandList.txt.

📦 Original Project

This repository is an optimized rewrite of:

🔗 BuildShellUsingJava
https://github.com/smritii73/BuildShellUsingJava

That project contained a single massive Main.java file.
This version transforms it into a clean, maintainable, modular architecture.

## Screenshot
<img width="1456" height="626" alt="Screenshot 2025-11-29 143317" src="https://github.com/user-attachments/assets/fec1c046-090b-46cf-9e78-466408ec91d8" />

