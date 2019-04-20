import scala.reflect.runtime.{universe => ru}
import ru._
import extensions.MapExtension._

/**
  * Register Type to Alias
  * @param container
  * @param typeTag$TSource
  * @tparam TSource
  */
class RegisterConfig[TSource: TypeTag](container: Container){
  def as[TTrait >: TSource : TypeTag] = {
    container.typeTable += typeOf[TTrait] -> typeOf[TSource]
    container
  }
}

/**
  * Register instance with an Alias
  * @param container
  * @param instance
  * @param typeTag$TSource
  * @tparam TSource
  */
class InstanceConfig[TSource: TypeTag](container: Container, instance: TSource){
  def as[TTrait >: TSource : TypeTag] = {
    container.typeTable += typeOf[TTrait] -> typeOf[TSource]
    container.instances += typeOf[TTrait] -> instance
    container
  }
}

/**
  * Container configuration builder
  * @param container
  */
class ContainerConfig(container: Container) {
  def register[TSource: TypeTag] = new RegisterConfig(container)

  def instance[TSource: TypeTag](instance: TSource) = new InstanceConfig(container, instance)
}

class Container {
  var typeTable = Map[Type, Type]()
  var instances = Map[Type, Any]()

  /**
    * Container configuration instance
    * @return
    */
  def config() = new ContainerConfig(this)

  /**
    * Exposed method to resolve a type to an instance
    * @tparam TSource
    * @return
    */
  def resolve[TSource: TypeTag] = resolveDynamic(typeOf[TSource]).asInstanceOf[TSource]

  /**
    * Helper method to dynamically resolve a type
    * @param requestedType
    * @return
    */
  private def resolveDynamic(requestedType: Type) = {
    val resultType = typeTable.getOrElseWithCompare(requestedType, requestedType, (x, y) => x =:= y)

    val instance = instances.getOrElseWithCompare(resultType, createInstance(resultType), (x, y) => x =:= y)

    instances += resultType -> instance

    instance
  }

  /**
    * Creates an instance of type (and it's constructor parameter recursively)
    * @param requestedType
    * @return
    */
  private def createInstance(requestedType: Type): Any = {
    val mirror: ru.Mirror = ru.runtimeMirror(getClass.getClassLoader)
    val clsSym: ru.ClassSymbol = requestedType.typeSymbol.asClass
    val clsMirror: ru.ClassMirror = mirror.reflectClass(clsSym)
    val constructorSym: ru.MethodSymbol = requestedType.decl(ru.termNames.CONSTRUCTOR).asMethod
    val constructorParameterTypes = constructorSym.paramLists.head.map(x => x.typeSignature)
    // Recursive step
    val params = constructorParameterTypes.map(x => resolveDynamic(x))
    val constructorMirror: ru.MethodMirror = clsMirror.reflectConstructor(constructorSym)
    // "Splat" operator needed
    val instance = constructorMirror(params : _*)
    instance
  }
}
