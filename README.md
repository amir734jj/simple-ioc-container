# simple-ioc-container
Simple IoC container in Scala using reflection.

Basically, to resolve a type:

1) Code looks up trait alias to find the concrete class for the trait or else assumes type is a class
2) Lookup instances table to find an instance already created to registered
3) Creates an instance of type (and it's constructor parameter recursively)

#### Example
Given this class structure:
```scala
trait TestTrait
class TestClass extends TestTrait
class TestRecClass(x : TestTrait)
```

Build and configure the container:
```scala
val container = new Container()
  .config()
  .register[TestClass]
  .as[TestTrait]

```

Instantiate the class:
```scala
val instance = container.resolve[TestRecClass]
```
