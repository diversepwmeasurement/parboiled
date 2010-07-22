/*
 * Copyright (C) 2009-2010 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.matchers;

import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.support.Var;

import java.util.List;

public class VarFramingMatcher implements Matcher {

    private Matcher inner;
    private final Var[] variables;

    @SuppressWarnings({"unchecked"})
    public VarFramingMatcher(@NotNull Rule inner, @NotNull Var[] variables) {
        this.inner = (Matcher) inner;
        this.variables = variables;
    }

    public boolean match(@NotNull MatcherContext context) {
        for (Var var : variables) {
            var.enterFrame();
        }

        boolean matched = inner.match(context);

        for (Var var : variables) {
            var.exitFrame();
        }

        return matched;
    }

    // GraphNode

    @NotNull
    public List<Matcher> getChildren() {
        return inner.getChildren();
    }

    // Rule

    @SuppressWarnings({"unchecked"})
    public Rule label(String label) {
        inner = (Matcher) inner.label(label);
        return this;
    }

    @SuppressWarnings({"unchecked"})
    public Rule suppressNode() {
        inner = (Matcher) inner.suppressNode();
        return this;
    }

    @SuppressWarnings({"unchecked"})
    public Rule suppressSubnodes() {
        inner = (Matcher) inner.suppressSubnodes();
        return this;
    }

    @SuppressWarnings({"unchecked"})
    public Rule skipNode() {
        inner = (Matcher) inner.skipNode();
        return this;
    }

    // Matcher

    public String getLabel() {return inner.getLabel();}

    public boolean isNodeSuppressed() {return inner.isNodeSuppressed();}

    public boolean areSubnodesSuppressed() {return inner.areSubnodesSuppressed();}

    public boolean isNodeSkipped() {return inner.isNodeSkipped();}

    public MatcherContext getSubContext(MatcherContext context) {
        MatcherContext subContext = inner.getSubContext(context);
        subContext.setMatcher(this); // we need to inject ourselves here otherwise we get cut out
        return subContext;
    }

    public <R> R accept(@NotNull MatcherVisitor<R> visitor) {return inner.accept(visitor);}

    @Override
    public String toString() { return inner.toString(); }

    /**
     * Retrieves the innermost Matcher that is not a VarFramingMatcher.
     *
     * @param matcher the matcher to unwrap
     * @return the given instance if it is not a VarFramingMatcher, otherwise the innermost Matcher
     */
    @SuppressWarnings({"unchecked"})
    public static  Matcher unwrap(Matcher matcher) {
        if (matcher instanceof VarFramingMatcher) {
            VarFramingMatcher varFramingMatcher = (VarFramingMatcher) matcher;
            return unwrap(varFramingMatcher.inner);
        }
        return matcher;
    }

}