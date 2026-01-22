# ASMInjector

A Java Swing utility that wraps the ObjectWeb ASM library to inject bytecode hooks into existing JAR files. 

It allows you to select a specific class and method within a target JAR and insert a static method call at the entry point of that method. This is useful for debugging, hot-patching, or quick instrumentation without decompiling.

## How it works

The tool parses the target JAR to build a class tree. Once a target method is selected, it uses `MethodVisitor` to inject an `INVOKESTATIC` instruction at the beginning of the method body.

**Note:** This tool only injects the *method call*. It does not inject the payload class itself. You must ensure the class containing your hook method is available on the application's classpath at runtime, otherwise the modified JAR will throw `NoClassDefFoundError`.

## Usage

1. Launch the application.
2. **Load JAR**: Select the target application.
3. **Select Target**: Navigate the tree to find the class and method you want to hook.
4. **Payload Configuration**:
   - **Injection Class**: Fully qualified name of your payload class (e.g., `com.example.MyHook`).
   - **Method Name**: The static method to invoke (e.g., `onEntry`).
5. **Run in Thread**: Wraps the injection call in a new Thread to avoid blocking the main execution flow of the target app.
6. **Embed Code**: Outputs a new JAR (usually suffixed with `_modified.jar`).

## Build

Requires JDK 8 or higher.

```bash
git clone https://github.com/DedInc/ASMInjector.git
cd ASMInjector
./gradlew shadowJar
```

The artifact will be located in `build/libs/ASMInjector-1.0-all.jar`.
