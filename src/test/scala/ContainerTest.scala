import org.scalatest.FlatSpec

trait TestTrait
class TestClass extends TestTrait
class TestRecClass(x : TestTrait)

class ContainerTest extends FlatSpec {

  "Given class" should "return instance (no recursion)" in {
    val container = new Container()

    val instance = container.resolve[TestClass]

    assert(instance match {
      case _: TestClass => true
      case null => false
    })
  }

  "Given Trait" should "return instance (no recursion)" in {
    val container = new Container()
      .config()
      .register[TestClass]
      .as[TestTrait]

    val instance = container.resolve[TestTrait]

    assert(instance match {
      case _: TestClass => true
      case null => false
    })
  }

  "Given Trait" should "return instance (with recursion)" in {
    val container = new Container()
      .config()
      .register[TestClass]
      .as[TestTrait]

    val instance = container.resolve[TestRecClass]

    assert(instance match {
      case _: TestRecClass => true
      case null => false
    })
  }
}