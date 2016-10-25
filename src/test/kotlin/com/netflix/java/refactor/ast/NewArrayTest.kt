package com.netflix.java.refactor.ast

import com.netflix.java.refactor.parse.Parser
import com.netflix.java.refactor.test.AstTest
import org.junit.Assert.*
import org.junit.Test

abstract class NewArrayTest(parser: Parser): AstTest(parser) {
    
    @Test
    fun newArray() {
        val a = parse("""
            public class A {
                int[] n = new int[0];
            }
        """)
        
        val newArr = a.fields()[0].initializer as Tr.NewArray
        assertNull(newArr.initializer)
        assertTrue(newArr.type is Type.Array)
        assertTrue(newArr.type.asArray()?.elemType is Type.Primitive)
        assertEquals(1, newArr.dimensions.size)
        assertTrue(newArr.dimensions[0].size is Tr.Literal)
    }

    @Test
    fun newArrayWithInitializers() {
        val a = parse("""
            public class A {
                int[] n = new int[] { 0, 1, 2 };
            }
        """)

        val newArr = a.fields()[0].initializer as Tr.NewArray
        assertTrue(newArr.dimensions[0].size is Tr.Empty)
        assertTrue(newArr.type is Type.Array)
        assertTrue(newArr.type.asArray()?.elemType is Type.Primitive)
        assertEquals(3, newArr.initializer?.elements?.size)
    }

    @Test
    fun formatWithDimensions() {
        val a = parse("""
            public class A {
                int[][] n = new int [ 0 ] [ 1 ];
            }
        """)

        val newArr = a.fields()[0].initializer as Tr.NewArray
        assertEquals("new int [ 0 ] [ 1 ]", newArr.print())
    }

    @Test
    fun formatWithInitializers() {
        val a = parse("""
            public class A {
                int[] m = new int[] { 0 };
                int[][] n = new int [ ] [ ] { m, m, m };
            }
        """)

        val newArr = a.typeDecls[0].fields()[1].initializer as Tr.NewArray
        assertEquals("new int [ ] [ ] { m, m, m }", newArr.print())
    }
}