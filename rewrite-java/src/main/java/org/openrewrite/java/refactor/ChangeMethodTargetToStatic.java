/*
 * Copyright 2020 the original authors.
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
package org.openrewrite.java.refactor;

import org.openrewrite.java.tree.Flag;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.openrewrite.Formatting.EMPTY;
import static org.openrewrite.Tree.randomId;

public class ChangeMethodTargetToStatic extends ScopedJavaRefactorVisitor {
    private final String clazz;

    public ChangeMethodTargetToStatic(J.MethodInvocation scope, String clazz) {
        super(scope.getId());
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return "core.ChangeMethodTargetToStatic{to=" + clazz + "}";
    }

    @Override
    public J visitMethodInvocation(J.MethodInvocation method) {
        if (isScope()) {
            var classType = JavaType.Class.build(clazz);
            J.MethodInvocation m = method.withSelect(
                    J.Ident.build(randomId(), classType.getClassName(), classType,
                            method.getSelect() == null ? EMPTY : method.getSelect().getFormatting()));

            maybeAddImport(clazz);

            JavaType.Method transformedType = null;
            if (method.getType() != null) {
                maybeRemoveImport(method.getType().getDeclaringType());
                transformedType = method.getType().withDeclaringType(classType);
                if (!method.getType().hasFlags(Flag.Static)) {
                    Set<Flag> flags = new LinkedHashSet<>(method.getType().getFlags());
                    flags.add(Flag.Static);
                    transformedType = transformedType.withFlags(flags);
                }
            }

            return m.withType(transformedType);
        }

        return super.visitMethodInvocation(method);
    }
}
