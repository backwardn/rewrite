/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.tree

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser

open class TypeParameterAndWildcardTest : JavaParser() {
    private val bc = listOf("public interface B {}", "public interface C {}")

    @Test
    fun annotatedTypeParametersOnWildcardBounds() {
        val aSource = """
            import java.util.List;
            public class A {
                List<? extends @NotNull B> checks;
            }
        """.trimIndent()

        val a = parse(aSource, "public class B {}")

        assertEquals(aSource, a.print())
    }

    @Test
    fun annotatedTypeParametersOnReturnTypeExpression() {
        val aSource = """
            import java.util.List;
            public class A {
                public List<
                    @NotNull(groups = Prioritized.P1.class)
                    @javax.validation.Valid
                    B> foo() {
                    return null;
                }
            }
        """.trimIndent()

        val a = parse(aSource, "public class B {}")

        assertEquals(aSource, a.print())
    }

    @Test
    fun extendsAndSuper() {
        val a = parse("""
            import java.util.List;
            public class A {
                public <P  extends B> void foo(List<P> out, List<? super C> in) {}
            }
        """, bc)

        assertEquals("public <P  extends B> void foo(List<P> out, List<? super C> in) {}",
                a.classes[0].methods[0].printTrimmed())
    }

    @Test
    fun multipleExtends() {
        val a = parse("public class A< T extends  B & C > {}", bc)
        assertEquals("public class A< T extends  B & C > {}", a.printTrimmed())
    }

    @Test
    fun wildcardExtends() {
        val a = parse("""
            import java.util.*;
            public class A {
                List< ?  extends  B > bs;
            }
        """, "public class B {}")

        val typeParam = a.classes[0].fields[0].typeExpr as J.ParameterizedType
        assertEquals("List< ?  extends  B >", typeParam.print())
    }

    @Test
    fun emptyWildcard() {
        val a = parse("""
            import java.util.*;
            public class A {
                List< ? > a;
            }
        """)

        val typeParam = a.classes[0].fields[0].typeExpr as J.ParameterizedType
        assertEquals("List< ? >", typeParam.print())
    }
}
