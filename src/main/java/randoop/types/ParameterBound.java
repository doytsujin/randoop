package randoop.types;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a type bound on a type variable occurring as a type parameter in
 * a class, interface, method or constructor. (See JLS section 8.1.2)
 * In Java, a type bound is either a type variable, a class type, an interface
 * type, or an intersection type of class and interface bounds.
 * This class represents a bound as concretely as possible based on the values
 * returned by {@link java.lang.reflect.TypeVariable#getBounds()}.
 */
public abstract class ParameterBound {

  public abstract ParameterBound apply(Substitution<ReferenceType> substitution);

  /**
   * Determines if this is an upper bound for the argument type.
   *
   * @param argType  the concrete argument type
   * @param subst  the substitution
   * @return true if this bound is satisfied by the concrete type when the
   *         substitution is used on the bound, false otherwise
   */
  public abstract boolean isSatisfiedBy(GeneralType argType, Substitution<ReferenceType> subst);

  /**
   * Determines if this object is an upper bound for the argument type using the most stringent
   * relaxation of the criterion used in {@link #isSatisfiedBy(GeneralType, Substitution)} allowed
   * when not using a substitution. The most relaxed form is simply checking assignability of raw
   * types.
   *
   * @param argType  the argument type
   * @return true, if the type satisfies the
   */
  public abstract boolean isSatisfiedBy(GeneralType argType);

  /**
   * Indicates whether this bound is a subtype of the given general type.
   *
   * @param otherType  the general type
   * @return true if this bound is a subtype of the given type
   */
  public abstract boolean isSubtypeOf(GeneralType otherType);

  /**
   * Creates a bound from the array of bounds of a {@code java.lang.reflect.TypeVariable}.
   * <p>
   * The bounds may be either be a single type variable, or a class/interface type followed by a
   * conjunction of interface types.
   * See JLS section 8.1.2.
   *
   * @param bounds  the type bounds
   * @return the {@code ParameterBound} for the given types
   */
  static ParameterBound forTypes(Type[] bounds) {
    if (bounds == null) {
      throw new IllegalArgumentException("bounds must be non null");
    }

    if (bounds.length == 1) {
      return ParameterBound.forType(bounds[0]);
    } else {
      List<ParameterBound> boundList = new ArrayList<>();
      for (Type type : bounds) {
        boundList.add(ParameterBound.forType(type));
      }
      return new IntersectionTypeBound(boundList);
    }
  }

  /**
   * Creates a {@code ParameterBound} object from a single
   * {@code java.lang.reflect.Type}.
   * Tests for types that are represented by {@code Class} objects, or
   * {@code java.lang.reflect.ParameterizedType} objects.
   *
   * @param type  the type for type bound
   * @return a type bound that ensures the given type is satisfied as an upper
   *         bound
   * @throws IllegalArgumentException if a parameterized type is given but the
   *         rawtype is not a Class object
   */
  private static ParameterBound forType(Type type) {
    return new LazyParameterBound(type);
  }

  /**
   * Constructs a parameter bound given a {@link ReferenceType}.
   *
   * @param type  the {@link ReferenceType}
   * @return a {@link ReferenceBound} if the type is a {@link ClassOrInterfaceType} or
   *         a {@link TypeVariable}
   */
  public static ParameterBound forType(ReferenceType type) {
    if (type instanceof ArrayType) {
      throw new IllegalArgumentException(
          "type may only be class, interface, or type variable, got " + type);
    }
    return new ReferenceBound(type);
  }
}
