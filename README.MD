# ASMInjector 💉🔍

ASMInjector is a powerful Java application that allows you to inject code into existing JAR files using the ASM (
Abstract Syntax Tree Manipulation) bytecode manipulation library. 🧰 It provides a user-friendly graphical interface to
load a JAR file, select a class and method, and embed your custom code into the selected method. 🚀

## Features ✨

- 🗃️ Load and explore JAR files
- 📂 Browse and select classes and methods within the JAR
- 💻 Inject custom code into any method of your choice
- ⚙ Option to run the injected code in a separate thread
- 🔄 Generate a new JAR file with the embedded code
- 🖥️ Cross-platform GUI built with Swing

## Getting Started 🚀

### Prerequisites

- Java Development Kit (JDK) 8 or later

### Building from Source

1. Clone the repository:

```bash
git clone https://github.com/DedInc/ASMInjector.git
```

2. Navigate to the project directory:

```bash
cd ASMInjector
```

3. Build the project using Gradle:

```bash
./gradlew shadowJar
```

### Running the Application

After building the project, you can run the ASMInjector application with the following command:

```bash
cd build/libs
java -jar ASMInjector-1.0-all.jar
```

## Usage 🕹️

1. Click the "Load JAR" button and select the JAR file you want to modify.
2. Select the class and method you want to inject code into from the respective combo boxes.
3. Enter the fully qualified name of your injection class and the name of the method to be injected.
4. (Optional) Check the "Run in Thread" checkbox if you want the injected code to run in a separate thread.
5. Click the "Embed Code" button to generate a new JAR file with the injected code.
6. The path to the modified JAR file will be displayed in a message dialog.

## Contributing 🤝

Contributions are welcome! If you have any ideas, bug reports, or feature requests, please open an issue or submit a
pull request.

## License 📄

This project is licensed under the [MIT License](LICENSE).

## Acknowledgments 🙏

- The ASM bytecode manipulation library
- The developers of the Swing GUI toolkit
- The open-source community for their invaluable contributions