
# Template Engine

This project provides a Java library for replacing properties in a given template with their respective values from a Java Bean object, either in string or JSON format.

## Usage

To use this library, include the following dependency in your Maven project:

```xml
<dependency>
    <groupId>io.github.moraesdelima</groupId>
    <artifactId>template-engine</artifactId>
    <version>1.0.0</version>
</dependency>
```

Additionally, since this project is hosted on GitHub Packages, you will need to configure your Maven settings to authenticate with GitHub Packages to download the dependency. You can add the following XML snippet to your Maven settings.xml file, located at ~/.m2/settings.xml:

```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_GITHUB_ACCESS_TOKEN</password>
    </server>
</servers>
```

Replace YOUR_GITHUB_USERNAME with your GitHub username, and YOUR_GITHUB_ACCESS_TOKEN with a personal access token with the read:packages scope, which you can generate in your GitHub account settings.

Then, you can use the `TemplateEngine` class to replace properties in a template. Here is an example of how to use it with a String Serialization, that converts primitive values to your's String representation:

```java
@Data
@AllArgsConstructor
class MyBean {
    private String name;
}

TemplateEngine engine = new TemplateEngine();
String template = "Hello, ${name}!";
MyBean bean = new MyBean("John");
String result = engine.process(template, bean);
System.out.println(result);
```

The output of this code would be:

```bash
Hello, John!
```

You can use the `TemplateEngine` class to replace properties in a template either with a String Serialization or JSON Serialization. Here is an example of how to use it with JSON Serialization, which converts any object to your's JSON representation:

```java
@Data
@AllArgsConstructor
class MyBean {
    private String name;
}

TemplateEngine engine = new TemplateEngine();
String template = "Hello, ${name}!";
MyBean bean = new MyBean("John");
String result = engine.process(template, bean, JSON_SERIALIZATION);
System.out.println(result);
```

The output of this code would be:

```bash
Hello, "John"!
```

Note that the name property is replaced with your Json equivalent "John" (enclosing by double quotes) and not with your String representation John (without double quotes). you can also use the following to serialize an entity like this

```java
@Data
@AllArgsConstructor
class User {
    private String name;
}
@Data
@AllArgsConstructor
class MyBean {
    private User user;
}

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

## Building from source

To build this project from source, you will need:

- JDK 11 or later
- Apache Maven 3.x

To build the project, run the following command from the project root directory:

```bash
mvn package
```

This will create a JAR file in the `target` directory.

Para referenciar as dependências que precisam de atenção, você pode adicionar uma seção na documentação do projeto. Por exemplo:

## Dependências

O projeto utiliza as seguintes dependências:

| Dependência | Versão | Escopo | Observação |
|-------------|--------|--------|------------|
| org.projectlombok:lombok | 1.18.26 | provided | A dependência é utilizada apenas durante a compilação e não deve ser incluída no pacote final. |
| com.google.code.gson:gson | 2.10.1 | compile | A dependência é utilizada apenas para converter objetos em formato JSON e deve ser incluída no pacote final. |
| junit:junit | 4.13.2 | test | A dependência é utilizada apenas durante os testes unitários. |
| org.junit.jupiter:junit-jupiter-api | 5.7.2 | test | A dependência é utilizada apenas durante os testes unitários. |
| org.mockito:mockito-core | 4.2.0 | test | A dependência é utilizada apenas durante os testes unitários. |
| org.mockito:mockito-junit-jupiter | 4.2.0 | test | A dependência é utilizada apenas durante os testes unitários. |

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Authors

This project was created by Luiz Moraes de Lima Neto. You can contact me at <moraesdelima@gmail.com>.
