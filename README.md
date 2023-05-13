
# Template Engine

The Template Engine is a Java library designed to replace properties in a given template with their corresponding values from a Java Bean object, either in string or JSON format. This library provides a simple and easy-to-use solution for anyone who needs to create dynamic templates with variable values that can be changed at runtime. It is especially useful for developers who need to generate text or JSON documents with dynamic content, such as emails, reports, or web pages. By using the Template Engine, developers can save time and effort by automating the process of replacing values in templates and focus on the core functionality of their application.

Here's a table of contents in markdown for your document:

## Table of Contents

- [Template Engine](#template-engine)
  - [Table of Contents](#table-of-contents)
  - [Usage](#usage)
    - [Configuration](#configuration)
    - [Basic Usage](#basic-usage)
    - [JSON Serialization](#json-serialization)
    - [Handling Exceptions](#handling-exceptions)
    - [Building from Source](#building-from-source)
  - [Conclusion](#conclusion)
  - [Dependencies](#dependencies)
  - [Reporting Bugs](#reporting-bugs)
  - [Submitting Feature Requests](#submitting-feature-requests)
  - [Contributing Code Changes](#contributing-code-changes)
  - [License](#license)
  - [Authors](#authors)

## Usage

### Configuration

To use the library, you first need to include the `template-engine` dependency in your project's build file. This can be done in Maven by adding the following to your `pom.xml` file:

```xml
<dependency>
    <groupId>io.github.moraesdelima</groupId>
    <artifactId>template-engine</artifactId>
    <version>1.1.1</version>
</dependency>
```

### Basic Usage

Once you have added the `template-engine` dependency to your project, you can use the `TemplateEngine` class to replace properties in a template. Here is an example of how to use it with a string serialization:

```java
TemplateEngine engine = new TemplateEngine();
String template = "Hello, ${name}!";
MyBean bean = new MyBean("John");
String result = engine.process(template, bean);
System.out.println(result);
```

This code will produce the following output:

```
Hello, John!
```

### JSON Serialization

You can also use the `TemplateEngine` class to replace properties in a template using JSON serialization. Here is an example of how to use it:

```java
TemplateEngine engine = new TemplateEngine();
String template = "Hello, ${name}!";
MyBean bean = new MyBean("John");
String result = engine.process(template, bean, JSON_SERIALIZATION);
System.out.println(result);
```

This code will produce the following output:

```
Hello, "John"!
```

Note that in this case, the name property is replaced with its JSON equivalent `"John"` (enclosed in double quotes) and not with its string representation `John` (without double quotes).

Another example of JSON Serialization could be that

```java
TemplateEngine engine = new TemplateEngine();
String template = "{ \"user\": ${user} }";
MyBean bean = new MyBean(new User("John"));
String result = engine.process(template, bean, JSON_SERIALIZATION);
System.out.println(result);
```

The output of this code would be:

```json
{ "user": {"name":"John"} }
```

### Handling Exceptions

The `engine.process(String, Object)` and `engine.process(String, Object, int)` methods can throw two types of exceptions:

- `GetPropertyException`: if the value of a property cannot be obtained from the Java Bean object
- `SerializePropertyException`: if an error occurs during serialization of a property value

Both exceptions have two informative methods:

- `getProperty()`: returns the Java Bean property that caused the error
- `getBeanClass()`: returns the Java Bean object where the error occurred

To handle these exceptions, you can use a try-catch block as follows:

```java
try {
    String result = engine.process(template, bean);
    System.out.println(result);
} catch (GetPropertyException | SerializePropertyException ex) {
    System.err.println("Error processing template: " + ex.getMessage());
    System.err.println("Property: " + ex.getProperty());
    System.err.println("Bean class: " + ex.getBeanClass().getSimpleName());
}
```

### Building from Source

To build the `template-engine` project from source, you will need:

- JDK 11 or later
- Apache Maven 3.x

To build the project, run the following command from the project root directory:

```bash
mvn package
```

This will create a JAR file in the `target` directory.

## Conclusion

The `template-engine` library provides a convenient way to replace properties in a template with their respective values from a Java Bean object, either in string or JSON format. By following the instructions outlined in this document, you should be able to easily configure and use the library in your own projects.

## Dependencies

The project uses the following dependencies:

| Dependency | Version | Scope | Note |
|-------------|--------|--------|------------|
| org.projectlombok:lombok | 1.18.26 | provided | The dependency is used only during compilation and should not be included in the final package. |
| com.google.code.gson:gson | 2.10.1 | compile | The dependency is used only to convert objects to JSON format and should be included in the final package. |
| junit:junit | 4.13.2 | test | The dependency is used only during unit tests. |
| org.junit.jupiter:junit-jupiter-api | 5.7.2 | test | The dependency is used only during unit tests. |
| org.mockito:mockito-core | 4.2.0 | test | The dependency is used only during unit tests. |
| org.mockito:mockito-junit-jupiter | 4.2.0 | test | The dependency is used only during unit tests. |

To contribute to the project, there are several ways you can get involved:

## Reporting Bugs

If you encounter any bugs or issues while using the project, you can report them by opening an issue on the project's GitHub repository. To do this, follow these steps:

1. Go to the project's GitHub repository.
2. Click on the "Issues" tab.
3. Click on the "New issue" button.
4. Describe the bug or issue in detail and provide any relevant information, such as error messages or screenshots.
5. Submit the issue.

The project maintainers will review the issue and work on resolving it as soon as possible.

## Submitting Feature Requests

If you have an idea for a new feature or enhancement to the project, you can submit a feature request by opening an issue on the project's GitHub repository. To do this, follow these steps:

1. Go to the project's GitHub repository.
2. Click on the "Issues" tab.
3. Click on the "New issue" button.
4. Describe the feature or enhancement you would like to see added in detail.
5. Submit the issue.

The project maintainers will review the feature request and consider it for future development.

## Contributing Code Changes

If you would like to contribute code changes to the project, you can do so by forking the project's GitHub repository and submitting a pull request with your changes. To do this, follow these steps:

1. Fork the project's GitHub repository.
2. Clone the forked repository to your local machine.
3. Make the necessary code changes.
4. Test your changes to ensure they work as intended.
5. Commit your changes and push them to your forked repository.
6. Submit a pull request to the project's GitHub repository.

The project maintainers will review your pull request and work with you to merge your changes into the main project codebase.

Thank you for considering contributing to the project!

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Authors

This project was created by Luiz Moraes de Lima Neto. You can contact me at <moraesdelima@gmail.com>.
