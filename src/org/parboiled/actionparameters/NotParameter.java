/*
 * Copyright (C) 2009 Mathias Doenitz
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

package org.parboiled.actionparameters;

import org.parboiled.ActionResult;
import org.parboiled.MatcherContext;
import org.jetbrains.annotations.NotNull;

/**
 * A special ActionParameter that negates the result of an inner action.
 */
public class NotParameter implements ActionParameter {
    private final Object argument;

    public NotParameter(Object argument) {
        this.argument = argument;
    }

    public Object resolve(@NotNull MatcherContext<?> context) throws Throwable {
        return ActionParameterUtils.resolve(argument, context, ActionResult.class) == ActionResult.CANCEL_MATCH ?
                ActionResult.CONTINUE : ActionResult.CANCEL_MATCH;
    }

    @Override
    public String toString() {
        return "NOT(" + argument + ')';
    }

}